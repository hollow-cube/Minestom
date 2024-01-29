package net.minestom.server;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.minestom.server.network.packet.server.play.ServerDifficultyPacket;
import net.minestom.server.thread.TickSchedulerThread;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.Difficulty;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * The main server class used to start the server and retrieve all the managers.
 * <p>
 * The server needs to be initialized with {@link #init()} and started with {@link #start(String, int)}.
 * You should register all of your dimensions, biomes, commands, events, etc... in-between.
 */
public final class MinecraftServer {

    public static final ComponentLogger LOGGER = ComponentLogger.logger(MinecraftServer.class);

    public static final String VERSION_NAME = "1.20.4";
    public static final int PROTOCOL_VERSION = 765;

    // Threads
    public static final String THREAD_NAME_BENCHMARK = "Ms-Benchmark";

    public static final String THREAD_NAME_TICK_SCHEDULER = "Ms-TickScheduler";
    public static final String THREAD_NAME_TICK = "Ms-Tick";

    // Config
    // Can be modified at performance cost when increased
    public static final int TICK_PER_SECOND = Integer.getInteger("minestom.tps", 20);
    public static final int TICK_MS = 1000 / TICK_PER_SECOND;

    // In-Game Manager
    volatile ServerProcess serverProcess;

    private int chunkViewDistance = Integer.getInteger("minestom.chunk-view-distance", 8);
    private int entityViewDistance = Integer.getInteger("minestom.entity-view-distance", 5);
    private int compressionThreshold = 256;
    private boolean terminalEnabled = System.getProperty("minestom.terminal.disabled") == null;
    private String brandName = "Minestom";
    private Difficulty difficulty = Difficulty.NORMAL;

    public void init() {
        updateProcess();
    }

    @ApiStatus.Internal
    public ServerProcess updateProcess() {
        ServerProcess process;
        try {
            process = new ServerProcessImpl(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return process;
    }

    /**
     * Gets the current server brand name.
     *
     * @return the server brand name
     */
    @NotNull
    public String getBrandName() {
        return brandName;
    }

    /**
     * Changes the server brand name and send the change to all connected players.
     *
     * @param brandName the server brand name
     * @throws NullPointerException if {@code brandName} is null
     */
    public void setBrandName(@NotNull String brandName) {
        this.brandName = brandName;
        PacketUtils.broadcastPlayPacket(this, PluginMessagePacket.getBrandPacket(this));
    }

    /**
     * Gets the server difficulty showed in game option.
     *
     * @return the server difficulty
     */
    @NotNull
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Changes the server difficulty and send the appropriate packet to all connected clients.
     *
     * @param difficulty the new server difficulty
     */
    public void setDifficulty(@NotNull Difficulty difficulty) {
        this.difficulty = difficulty;
        PacketUtils.broadcastPlayPacket(this, new ServerDifficultyPacket(difficulty, true));
    }

    @ApiStatus.Experimental
    public @UnknownNullability ServerProcess process() {
        return serverProcess;
    }

    /**
     * Gets the chunk view distance of the server.
     *
     * @return the chunk view distance
     */
    public int getChunkViewDistance() {
        return chunkViewDistance;
    }

    /**
     * Changes the chunk view distance of the server.
     *
     * @param chunkViewDistance the new chunk view distance
     * @throws IllegalArgumentException if {@code chunkViewDistance} is not between 2 and 32
     * @deprecated should instead be defined with a java property
     */
    @Deprecated
    public void setChunkViewDistance(int chunkViewDistance) {
        Check.stateCondition(serverProcess.isAlive(), "You cannot change the chunk view distance after the server has been started.");
        Check.argCondition(!MathUtils.isBetween(chunkViewDistance, 2, 32),
                "The chunk view distance must be between 2 and 32");
        this.chunkViewDistance = chunkViewDistance;
    }

    /**
     * Gets the entity view distance of the server.
     *
     * @return the entity view distance
     */
    public int getEntityViewDistance() {
        return entityViewDistance;
    }

    /**
     * Changes the entity view distance of the server.
     *
     * @param entityViewDistance the new entity view distance
     * @throws IllegalArgumentException if {@code entityViewDistance} is not between 0 and 32
     * @deprecated should instead be defined with a java property
     */
    @Deprecated
    public void setEntityViewDistance(int entityViewDistance) {
        Check.stateCondition(serverProcess.isAlive(), "You cannot change the entity view distance after the server has been started.");
        Check.argCondition(!MathUtils.isBetween(entityViewDistance, 0, 32),
                "The entity view distance must be between 0 and 32");
        this.entityViewDistance = entityViewDistance;
    }

    /**
     * Gets the compression threshold of the server.
     *
     * @return the compression threshold, 0 means that compression is disabled
     */
    public int getCompressionThreshold() {
        return compressionThreshold;
    }

    /**
     * Changes the compression threshold of the server.
     * <p>
     * WARNING: this need to be called before {@link #start(SocketAddress)}.
     *
     * @param compressionThreshold the new compression threshold, 0 to disable compression
     * @throws IllegalStateException if this is called after the server started
     */
    public void setCompressionThreshold(int compressionThreshold) {
        Check.stateCondition(serverProcess != null && serverProcess.isAlive(), "The compression threshold cannot be changed after the server has been started.");
        this.compressionThreshold = compressionThreshold;
    }

    /**
     * Gets if the built in Minestom terminal is enabled.
     *
     * @return true if the terminal is enabled
     */
    public boolean isTerminalEnabled() {
        return terminalEnabled;
    }

    /**
     * Enabled/disables the built in Minestom terminal.
     *
     * @param enabled true to enable, false to disable
     */
    public void setTerminalEnabled(boolean enabled) {
        Check.stateCondition(serverProcess.isAlive(), "Terminal settings may not be changed after starting the server.");
        terminalEnabled = enabled;
    }

    /**
     * Starts the server.
     * <p>
     * It should be called after {@link #init()} and probably your own initialization code.
     *
     * @param address the server address
     * @throws IllegalStateException if called before {@link #init()} or if the server is already running
     */
    public void start(@NotNull SocketAddress address) {
        serverProcess.start(address);
        new TickSchedulerThread(serverProcess).start();
    }

    public void start(@NotNull String address, int port) {
        start(new InetSocketAddress(address, port));
    }

    /**
     * Stops this server properly (saves if needed, kicking players, etc.)
     */
    public void stopCleanly() {
        serverProcess.stop();
    }
}
