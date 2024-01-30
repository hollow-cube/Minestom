package net.minestom.server;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.advancements.AdvancementManager;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.adventure.bossbar.BossBarManager;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.exception.ExceptionHandler;
import net.minestom.server.exception.ExceptionHandlerImpl;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.gamedata.tags.TagManager;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.PacketProcessor;
import net.minestom.server.network.socket.Server;
import net.minestom.server.recipe.RecipeManager;
import net.minestom.server.scoreboard.TeamManager;
import net.minestom.server.snapshot.*;
import net.minestom.server.thread.Acquirable;
import net.minestom.server.thread.ThreadDispatcher;
import net.minestom.server.thread.TickSchedulerThread;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.PropertyUtils;
import net.minestom.server.utils.collection.MappedCollection;
import net.minestom.server.world.DimensionTypeManager;
import net.minestom.server.world.biomes.BiomeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

final class ServerProcessImpl implements ServerProcess {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerProcessImpl.class);
    private static final Boolean SHUTDOWN_ON_SIGNAL = PropertyUtils.getBoolean("minestom.shutdown-on-signal", true);

    private final ExceptionHandler exceptionHandler;
    private final ConnectionManager connectionManager;
    private final PacketListenerManager packetListenerManager;
    private final PacketProcessor packetProcessor;
    private final InstanceManager instanceManager;
    private final BlockManager blockManager;
    private final CommandManager commandManager;
    private final RecipeManager recipeManager;
    private final TeamManager teamManager;
    private final GlobalEventHandler globalEventHandler;
    private final SchedulerManager schedulerManager;
    private final BenchmarkManager benchmarkManager;
    private final DimensionTypeManager dimensionTypeManager;
    private final BiomeManager biomeManager;
    private final AdvancementManager advancementManager;
    private final BossBarManager bossBarManager;
    private final TagManager tagManager;
    private final Server server;

    private final ThreadDispatcher<Chunk> dispatcher;
    private final Ticker ticker;

    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicBoolean stopped = new AtomicBoolean();
    private final ServerSettings serverSettings;
    private final Audiences audiences;
    private final MojangAuth mojangAuth;

    public ServerProcessImpl(ServerSettings serverSettings) {
        this(serverSettings, null);
    }

    public ServerProcessImpl(ServerSettings serverSettings, @Nullable ExceptionHandler exceptionHandler) {
        this.serverSettings = serverSettings;
        this.exceptionHandler = Objects.requireNonNullElseGet(exceptionHandler, () -> new ExceptionHandlerImpl(this));
        this.globalEventHandler = new GlobalEventHandler(exceptionHandler);
        this.packetListenerManager = new PacketListenerManager(globalEventHandler, exceptionHandler);
        this.packetProcessor = new PacketProcessor(packetListenerManager);
        this.dispatcher = ThreadDispatcher.singleThread(exceptionHandler);
        this.instanceManager = new InstanceManager(dispatcher, globalEventHandler);
        this.blockManager = new BlockManager();
        this.commandManager = new CommandManager(this);
        this.recipeManager = new RecipeManager();
        this.teamManager = new TeamManager(this);
        this.schedulerManager = new SchedulerManager();
        this.benchmarkManager = new BenchmarkManager(exceptionHandler);
        this.dimensionTypeManager = new DimensionTypeManager();
        this.biomeManager = new BiomeManager();
        this.advancementManager = new AdvancementManager(this);
        this.bossBarManager = new BossBarManager(this);
        this.tagManager = new TagManager();
        this.connectionManager = new ConnectionManager(this, serverSettings, tagManager);
        this.server = new Server(this, packetProcessor);
        this.mojangAuth = new MojangAuth(this, exceptionHandler);
        this.audiences = new Audiences(this, connectionManager, commandManager);

        this.ticker = new TickerImpl();
    }

    @Override
    public ServerSettings getServerSetting() {
        return serverSettings;
    }

    @Override
    public @NotNull ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    @Override
    public @NotNull InstanceManager getInstanceManager() {
        return instanceManager;
    }

    @Override
    public @NotNull BlockManager getBlockManager() {
        return blockManager;
    }

    @Override
    public @NotNull CommandManager getCommandManager() {
        return commandManager;
    }

    @Override
    public @NotNull RecipeManager getRecipeManager() {
        return recipeManager;
    }

    @Override
    public @NotNull TeamManager getTeamManager() {
        return teamManager;
    }

    @Override
    public @NotNull GlobalEventHandler getGlobalEventHandler() {
        return globalEventHandler;
    }

    @Override
    public @NotNull SchedulerManager getSchedulerManager() {
        return schedulerManager;
    }

    @Override
    public @NotNull BenchmarkManager getBenchmarkManager() {
        return benchmarkManager;
    }

    @Override
    public @NotNull DimensionTypeManager getDimensionTypeManager() {
        return dimensionTypeManager;
    }

    @Override
    public @NotNull BiomeManager getBiomeManager() {
        return biomeManager;
    }

    @Override
    public @NotNull AdvancementManager getAdvancementManager() {
        return advancementManager;
    }

    @Override
    public @NotNull BossBarManager getBossBarManager() {
        return bossBarManager;
    }

    @Override
    public @NotNull TagManager getTagManager() {
        return tagManager;
    }

    @Override
    public @NotNull ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    @Override
    public @NotNull PacketListenerManager getPacketListenerManager() {
        return packetListenerManager;
    }

    @Override
    public @NotNull PacketProcessor getPacketProcessor() {
        return packetProcessor;
    }

    @Override
    public @NotNull Server getServer() {
        return server;
    }

    @Override
    public @NotNull ThreadDispatcher<Chunk> dispatcher() {
        return dispatcher;
    }

    @Override
    public @NotNull Ticker ticker() {
        return ticker;
    }

    @Override
    public void start(@NotNull SocketAddress socketAddress) {
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("Server already started");
        }

        LOGGER.info("Starting " + serverSettings.getBrandName() + " server.");

        // Init server
        try {
            server.init(socketAddress);
        } catch (IOException e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        }

        // Start server
        server.start();

        LOGGER.info(serverSettings.getBrandName() + " server started successfully.");

        // Stop the server on SIGINT
        if (SHUTDOWN_ON_SIGNAL) Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        new TickSchedulerThread(this).start();
    }

    @Override
    public void stop() {
        if (!stopped.compareAndSet(false, true))
            return;
        LOGGER.info("Stopping " + serverSettings.getBrandName() + " server.");
        schedulerManager.shutdown();
        connectionManager.shutdown();
        server.stop();
        LOGGER.info("Shutting down all thread pools.");
        benchmarkManager.disable();
        dispatcher.shutdown();
        LOGGER.info(serverSettings.getBrandName() + " server stopped successfully.");
    }

    @Override
    public boolean isAlive() {
        return started.get() && !stopped.get();
    }

    @Override
    public Audiences getAudiences() {
        return audiences;
    }

    @Override
    public @NotNull ServerSnapshot updateSnapshot(@NotNull SnapshotUpdater updater) {
        List<AtomicReference<InstanceSnapshot>> instanceRefs = new ArrayList<>();
        Int2ObjectOpenHashMap<AtomicReference<EntitySnapshot>> entityRefs = new Int2ObjectOpenHashMap<>();
        for (Instance instance : instanceManager.getInstances()) {
            instanceRefs.add(updater.reference(instance));
            for (Entity entity : instance.getEntities()) {
                entityRefs.put(entity.getEntityId(), updater.reference(entity));
            }
        }
        return new SnapshotImpl.Server(MappedCollection.plainReferences(instanceRefs), entityRefs);
    }

    @Override
    public MojangAuth getMojangAuth() {
        return mojangAuth;
    }

    private final class TickerImpl implements Ticker {
        @Override
        public void tick(long nanoTime) {
            final long msTime = System.currentTimeMillis();

            getSchedulerManager().processTick();

            // Connection tick (let waiting clients in, send keep alives, handle configuration players packets)
            getConnectionManager().tick(msTime);

            // Server tick (chunks/entities)
            serverTick(msTime);

            // Flush all waiting packets
            PacketUtils.flush();

            // Server connection tick
            getServer().tick();

            // Monitoring
            {
                final double acquisitionTimeMs = Acquirable.resetAcquiringTime() / 1e6D;
                final double tickTimeMs = (System.nanoTime() - nanoTime) / 1e6D;
                final TickMonitor tickMonitor = new TickMonitor(tickTimeMs, acquisitionTimeMs);
                getGlobalEventHandler().call(new ServerTickMonitorEvent(tickMonitor));
            }
        }

        private void serverTick(long tickStart) {
            // Tick all instances
            for (Instance instance : getInstanceManager().getInstances()) {
                try {
                    instance.tick(tickStart);
                } catch (Exception e) {
                    getExceptionHandler().handleException(e);
                }
            }
            // Tick all chunks (and entities inside)
            dispatcher().updateAndAwait(tickStart);

            // Clear removed entities & update threads
            final long tickTime = System.currentTimeMillis() - tickStart;
            dispatcher().refreshThreads(tickTime);
        }
    }
}
