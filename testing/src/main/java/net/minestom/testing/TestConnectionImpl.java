package net.minestom.testing;

import net.kyori.adventure.translation.GlobalTranslator;
import net.minestom.server.ServerProcess;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

final class TestConnectionImpl implements TestConnection {
    private final Env env;
    private final ServerProcess process;
    private final PlayerConnectionImpl playerConnection = new PlayerConnectionImpl();

    private final List<IncomingCollector<ServerPacket>> incomingTrackersSync = new CopyOnWriteArrayList<>();

    private final List<IncomingCollector<ServerPacket>> incomingTrackersAsync = new CopyOnWriteArrayList<>();

    TestConnectionImpl(Env env) {
        this.env = env;
        this.process = env.process();
    }

    @Override
    public @NotNull CompletableFuture<Player> connect(@NotNull Instance instance, @NotNull Pos pos) {
        // Use player provider to disable queued chunk sending
        process.connection().setPlayerProvider(TestPlayerImpl::new);

        playerConnection.setConnectionState(ConnectionState.LOGIN);
        var player = process.connection().createPlayer(playerConnection, UUID.randomUUID(), "RandName");
        player.eventNode().addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.setSpawningInstance(instance);
            event.getPlayer().setRespawnPoint(pos);
        });

        // Force the player through the entirety of the login process manually
        process.connection().doConfiguration(player, true);
        process.connection().transitionConfigToPlay(player);
        process.connection().updateWaitingPlayers();
        return CompletableFuture.completedFuture(player);
    }

    @Override
    public @NotNull <T extends ServerPacket> Collector<T> trackIncoming(@NotNull Class<T> type) {
        var tracker = new IncomingCollector<>(type, incomingTrackersSync, incomingTrackersAsync);
        this.incomingTrackersSync.add(IncomingCollector.class.cast(tracker));
        this.incomingTrackersAsync.add(IncomingCollector.class.cast(tracker));
        return tracker;
    }

    @Override
    public @NotNull <T extends ServerPacket> Collector<T> trackIncomingSync(@NotNull Class<T> type) {
        var tracker = new IncomingCollector<>(type, incomingTrackersSync);
        this.incomingTrackersSync.add(IncomingCollector.class.cast(tracker));
        return tracker;
    }

    @Override
    public @NotNull <T extends ServerPacket> Collector<T> trackIncomingAsync(@NotNull Class<T> type) {
        var tracker = new IncomingCollector<>(type, incomingTrackersAsync);
        this.incomingTrackersAsync.add(IncomingCollector.class.cast(tracker));
        return tracker;
    }

    final class PlayerConnectionImpl extends PlayerConnection {
        @Override
        public void sendPacket(@NotNull SendablePacket packet) {
            final var serverPacket = this.extractPacket(packet);
            for (var tracker : incomingTrackersSync) {
                if (tracker.type.isAssignableFrom(serverPacket.getClass())) tracker.packets.add(serverPacket);
            }
        }

        @Override
        public void sendPacketAsync(@NotNull SendablePacket packet) {
            final var serverPacket = this.extractPacket(packet);
            for (var tracker : incomingTrackersAsync) {
                if (tracker.type.isAssignableFrom(serverPacket.getClass())) tracker.packets.add(serverPacket);
            }
        }

        private ServerPacket extractPacket(final SendablePacket packet) {
            if (!(packet instanceof ServerPacket serverPacket)) return SendablePacket.extractServerPacket(getConnectionState(), packet);

            final Player player = getPlayer();
            if (player == null) return serverPacket;

            if (MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION && serverPacket instanceof ComponentHoldingServerPacket) {
                serverPacket = ((ComponentHoldingServerPacket) serverPacket).copyWithOperator(component ->
                        GlobalTranslator.render(component, Objects.requireNonNullElseGet(player.getLocale(), MinestomAdventure::getDefaultLocale)));
            }

            return serverPacket;
        }

        @Override
        public @NotNull SocketAddress getRemoteAddress() {
            return new InetSocketAddress("localhost", 25565);
        }

        @Override
        public void disconnect() {

        }
    }

    static final class IncomingCollector<T extends ServerPacket> implements Collector<T> {
        private final Class<T> type;
        private final List<T> packets = new CopyOnWriteArrayList<>();
        private final List<IncomingCollector<ServerPacket>>[] incomingTrackers;

        @SafeVarargs
        public IncomingCollector(Class<T> type, List<IncomingCollector<ServerPacket>>... incomingTrackers) {
            this.incomingTrackers = incomingTrackers;
            this.type = type;
        }

        @Override
        public @NotNull List<T> collect() {
            for (List<IncomingCollector<ServerPacket>> incomingTracker : incomingTrackers)
                incomingTracker.remove(this);
            return List.copyOf(packets);
        }
    }
}
