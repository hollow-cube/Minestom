package net.minestom.server.network;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.ServerFacade;
import net.minestom.server.ServerSettingsProvider;
import net.minestom.server.adventure.bossbar.BossBarManagerProvider;
import net.minestom.server.command.CommandManagerProvider;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.GlobalEventHandlerProvider;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import net.minestom.server.exception.ExceptionHandlerProvider;
import net.minestom.server.gamedata.tags.TagManager;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.BlockManagerProvider;
import net.minestom.server.listener.manager.PacketListenerManagerProvider;
import net.minestom.server.message.Messenger;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.common.KeepAlivePacket;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.minestom.server.network.packet.server.common.TagsPacket;
import net.minestom.server.network.packet.server.configuration.FinishConfigurationPacket;
import net.minestom.server.network.packet.server.configuration.RegistryDataPacket;
import net.minestom.server.network.packet.server.login.LoginSuccessPacket;
import net.minestom.server.network.packet.server.play.StartConfigurationPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.server.recipe.RecipeManagerProvider;
import net.minestom.server.scoreboard.TeamManagerProvider;
import net.minestom.server.thread.ChunkDispatcherProvider;
import net.minestom.server.timer.SchedulerManagerProvider;
import net.minestom.server.utils.StringUtils;
import net.minestom.server.utils.async.AsyncUtils;
import net.minestom.server.utils.debug.DebugUtils;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionTypeManagerProvider;
import net.minestom.server.world.biomes.BiomeManagerProvider;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscUnboundedArrayQueue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;

public final class ConnectionManagerImpl implements ConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManagerImpl.class);

    public final CachedPacket defaultTags;

    private static final long KEEP_ALIVE_DELAY = 10_000;
    private static final long KEEP_ALIVE_KICK = 30_000;
    private static final Component TIMEOUT_TEXT = Component.text("Timeout", NamedTextColor.RED);


    // All players once their Player object has been instantiated.
    private final Map<PlayerConnection, Player> connectionPlayerMap = new ConcurrentHashMap<>();
    // Players waiting to be spawned (post configuration state)
    private final MessagePassingQueue<Player> waitingPlayers = new MpscUnboundedArrayQueue<>(64);
    // Players in configuration state
    private final Set<Player> configurationPlayers = new CopyOnWriteArraySet<>();
    // Players in play state
    private final Set<Player> playPlayers = new CopyOnWriteArraySet<>();

    // The players who need keep alive ticks. This was added because we may not send a keep alive in
    // the time after sending finish configuration but before receiving configuration end (to swap to play).
    // I(mattw) could not come up with a better way to express this besides completely splitting client/server
    // states. Perhaps there will be an improvement in the future.
    private final Set<Player> keepAlivePlayers = new CopyOnWriteArraySet<>();

    private final Set<Player> unmodifiableConfigurationPlayers = Collections.unmodifiableSet(configurationPlayers);
    private final Set<Player> unmodifiablePlayPlayers = Collections.unmodifiableSet(playPlayers);
    private final ExceptionHandlerProvider exceptionHandlerProvider;
    private final ServerSettingsProvider serverSettingsProvider;
    private final BiomeManagerProvider biomeManagerProvider;
    private final DimensionTypeManagerProvider dimensionTypeManagerProvider;
    private final GlobalEventHandlerProvider globalEventHandlerProvider;


    // The uuid provider once a player login
    private volatile UuidProvider uuidProvider = (playerConnection, username) -> UUID.randomUUID();
    // The player provider to have your own Player implementation
    private final PlayerProvider defaultPlayerProvider;
    private volatile PlayerProvider playerProvider;

    public ConnectionManagerImpl(TagManager tagManager, ServerFacade serverFacade) {
        this(tagManager, serverFacade, serverFacade, serverFacade, serverFacade, serverFacade, serverFacade, serverFacade, serverFacade, serverFacade, serverFacade, serverFacade, serverFacade, serverFacade);
    }

    public ConnectionManagerImpl(
            TagManager tagManager,
            ServerSettingsProvider serverSettingsProvider,
            GlobalEventHandlerProvider globalEventHandlerProvider,
            ChunkDispatcherProvider chunkDispatcherProvider,
            ExceptionHandlerProvider exceptionHandlerProvider,
            TeamManagerProvider teamManagerProvider,
            RecipeManagerProvider recipeManagerProvider,
            CommandManagerProvider commandManagerProvider,
            BossBarManagerProvider bossBarManagerProvider,
            SchedulerManagerProvider schedulerManagerProvider,
            PacketListenerManagerProvider packetListenerManagerProvider,
            BiomeManagerProvider biomeManagerProvider,
            DimensionTypeManagerProvider dimensionTypeManagerProvider,
            BlockManagerProvider blockManagerProvider
    ) {
        this.exceptionHandlerProvider = exceptionHandlerProvider;
        this.serverSettingsProvider = serverSettingsProvider;
        this.biomeManagerProvider = biomeManagerProvider;
        this.dimensionTypeManagerProvider = dimensionTypeManagerProvider;
        this.globalEventHandlerProvider = globalEventHandlerProvider;
        this.defaultTags = new CachedPacket(serverSettingsProvider, new TagsPacket(tagManager.getTagMap()));
        defaultPlayerProvider = (uuid, username, connection) -> new Player(globalEventHandlerProvider.getGlobalEventHandler(), serverSettingsProvider.getServerSettings(), chunkDispatcherProvider, exceptionHandlerProvider, () -> this, teamManagerProvider, recipeManagerProvider, commandManagerProvider, bossBarManagerProvider, schedulerManagerProvider, packetListenerManagerProvider, blockManagerProvider, uuid, username, connection);
        playerProvider = defaultPlayerProvider;
    }

    @Override
    public int getOnlinePlayerCount() {
        return playPlayers.size();
    }

    @Override
    public @NotNull Collection<@NotNull Player> getOnlinePlayers() {
        return unmodifiablePlayPlayers;
    }

    @Override
    public @NotNull Collection<@NotNull Player> getConfigPlayers() {
        return unmodifiableConfigurationPlayers;
    }

    @Override
    public Player getPlayer(@NotNull PlayerConnection connection) {
        return connectionPlayerMap.get(connection);
    }

    @Override
    public @Nullable Player getOnlinePlayerByUsername(@NotNull String username) {
        for (Player player : getOnlinePlayers()) {
            if (player.getUsername().equalsIgnoreCase(username))
                return player;
        }
        return null;
    }

    @Override
    public @Nullable Player getOnlinePlayerByUuid(@NotNull UUID uuid) {
        for (Player player : getOnlinePlayers()) {
            if (player.getUuid().equals(uuid))
                return player;
        }
        return null;
    }

    @Override
    public @Nullable Player findOnlinePlayer(@NotNull String username) {
        Player exact = getOnlinePlayerByUsername(username);
        if (exact != null) return exact;
        final String username1 = username.toLowerCase(Locale.ROOT);

        Function<Player, Double> distanceFunction = player -> {
            final String username2 = player.getUsername().toLowerCase(Locale.ROOT);
            return StringUtils.jaroWinklerScore(username1, username2);
        };
        return getOnlinePlayers().stream()
                .min(Comparator.comparingDouble(distanceFunction::apply))
                .filter(player -> distanceFunction.apply(player) > 0)
                .orElse(null);
    }

    @Override
    public void setUuidProvider(@Nullable UuidProvider uuidProvider) {
        this.uuidProvider = uuidProvider != null ? uuidProvider : (playerConnection, username) -> UUID.randomUUID();
    }

    @Override
    public @NotNull UUID getPlayerConnectionUuid(@NotNull PlayerConnection playerConnection, @NotNull String username) {
        return uuidProvider.provide(playerConnection, username);
    }

    @Override
    public void setPlayerProvider(@Nullable PlayerProvider playerProvider) {
        this.playerProvider = playerProvider != null ? playerProvider : defaultPlayerProvider;
    }

    @Override
    @ApiStatus.Internal
    public @NotNull Player createPlayer(@NotNull PlayerConnection connection, @NotNull UUID uuid, @NotNull String username) {
        final Player player = playerProvider.createPlayer(uuid, username, connection);
        this.connectionPlayerMap.put(connection, player);
        var future = transitionLoginToConfig(player);
        if (DebugUtils.INSIDE_TEST) future.join();
        return player;
    }

    @Override
    @ApiStatus.Internal
    public @NotNull CompletableFuture<Void> transitionLoginToConfig(@NotNull Player player) {
        return AsyncUtils.runAsync(exceptionHandlerProvider.getExceptionHandler(), () -> {
            final PlayerConnection playerConnection = player.getPlayerConnection();

            // Compression
            if (playerConnection instanceof PlayerSocketConnection socketConnection) {
                final int threshold = serverSettingsProvider.getServerSettings().getCompressionThreshold();
                if (threshold > 0) socketConnection.startCompression();
            }

            // Call pre login event
            AsyncPlayerPreLoginEvent asyncPlayerPreLoginEvent = new AsyncPlayerPreLoginEvent(player);
            globalEventHandlerProvider.getGlobalEventHandler().call(asyncPlayerPreLoginEvent);
            if (!player.isOnline())
                return; // Player has been kicked

            // Change UUID/Username based on the event
            {
                final String eventUsername = asyncPlayerPreLoginEvent.getUsername();
                final UUID eventUuid = asyncPlayerPreLoginEvent.getPlayerUuid();
                if (!player.getUsername().equals(eventUsername)) {
                    player.setUsernameField(eventUsername);
                }
                if (!player.getUuid().equals(eventUuid)) {
                    player.setUuid(eventUuid);
                }
            }

            // Send login success packet (and switch to configuration phase)
            LoginSuccessPacket loginSuccessPacket = new LoginSuccessPacket(player.getUuid(), player.getUsername(), 0);
            playerConnection.sendPacket(loginSuccessPacket);
        });
    }

    @Override
    @ApiStatus.Internal
    public void transitionPlayToConfig(@NotNull Player player) {
        player.sendPacket(new StartConfigurationPacket());
        configurationPlayers.add(player);
    }

    @Override
    @ApiStatus.Internal
    public void doConfiguration(@NotNull Player player, boolean isFirstConfig) {
        if (isFirstConfig) {
            configurationPlayers.add(player);
            keepAlivePlayers.add(player);
        }

        player.getPlayerConnection().setConnectionState(ConnectionState.CONFIGURATION);
        CompletableFuture<Void> configFuture = AsyncUtils.runAsync(exceptionHandlerProvider.getExceptionHandler(), () -> {
            player.sendPacket(PluginMessagePacket.getBrandPacket(serverSettingsProvider.getServerSettings()));

            var event = new AsyncPlayerConfigurationEvent(player, isFirstConfig);
            globalEventHandlerProvider.getGlobalEventHandler().call(event);

            final Instance spawningInstance = event.getSpawningInstance();
            Check.notNull(spawningInstance, "You need to specify a spawning instance in the AsyncPlayerConfigurationEvent");

            // Registry data (if it should be sent)
            if (event.willSendRegistryData()) {
                var registry = new HashMap<String, NBT>();
                registry.put("minecraft:chat_type", Messenger.chatRegistry());
                registry.put("minecraft:dimension_type", dimensionTypeManagerProvider.getDimensionTypeManager().toNBT());
                registry.put("minecraft:worldgen/biome", biomeManagerProvider.getBiomeManager().toNBT());
                registry.put("minecraft:damage_type", DamageType.getNBT());
                player.sendPacket(new RegistryDataPacket(NBT.Compound(registry)));

                player.sendPacket(defaultTags);
            }

            // Wait for pending resource packs if any
            var packFuture = player.getResourcePackFuture();
            if (packFuture != null) packFuture.join();

            keepAlivePlayers.remove(player);
            player.setPendingOptions(spawningInstance, event.isHardcore());
            player.sendPacket(new FinishConfigurationPacket());
        });
        if (DebugUtils.INSIDE_TEST) configFuture.join();
    }

    @Override
    @ApiStatus.Internal
    public void transitionConfigToPlay(@NotNull Player player) {
        this.waitingPlayers.relaxedOffer(player);
    }

    @Override
    @ApiStatus.Internal
    public synchronized void removePlayer(@NotNull PlayerConnection connection) {
        final Player player = this.connectionPlayerMap.remove(connection);
        if (player == null) return;
        this.configurationPlayers.remove(player);
        this.playPlayers.remove(player);
        this.keepAlivePlayers.remove(player);
    }

    /**
     * Shutdowns the connection manager by kicking all the currently connected players.
     */
    @Override
    public synchronized void shutdown() {
        this.configurationPlayers.clear();
        this.playPlayers.clear();
        this.keepAlivePlayers.clear();
        this.connectionPlayerMap.clear();
    }

    @Override
    public void tick(long tickStart) {
        // Let waiting players into their instances
        updateWaitingPlayers();

        // Send keep alive packets
        handleKeepAlive(keepAlivePlayers, tickStart);

        // Interpret packets for configuration players
        configurationPlayers.forEach(Player::interpretPacketQueue);
    }

    /**
     * Connects waiting players.
     */
    @Override
    @ApiStatus.Internal
    public void updateWaitingPlayers() {
        this.waitingPlayers.drain(player -> {
            player.getPlayerConnection().setConnectionState(ConnectionState.PLAY);
            playPlayers.add(player);
            keepAlivePlayers.add(player);

            // Spawn the player at Player#getRespawnPoint
            CompletableFuture<Void> spawnFuture = player.UNSAFE_init();

            // Required to get the exact moment the player spawns
            if (DebugUtils.INSIDE_TEST) spawnFuture.join();
        });
    }

    /**
     * Updates keep alive by checking the last keep alive packet and send a new one if needed.
     *
     * @param tickStart the time of the update in milliseconds, forwarded to the packet
     */
    private void handleKeepAlive(@NotNull Collection<Player> playerGroup, long tickStart) {
        final KeepAlivePacket keepAlivePacket = new KeepAlivePacket(tickStart);
        for (Player player : playerGroup) {
            final long lastKeepAlive = tickStart - player.getLastKeepAlive();
            if (lastKeepAlive > KEEP_ALIVE_DELAY && player.didAnswerKeepAlive()) {
                player.refreshKeepAlive(tickStart);
                player.sendPacket(keepAlivePacket);
            } else if (lastKeepAlive >= KEEP_ALIVE_KICK) {
                player.kick(TIMEOUT_TEXT);
            }
        }
    }
}
