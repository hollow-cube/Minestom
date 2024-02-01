package net.minestom.server;

import net.minestom.server.exception.ExceptionHandlerProvider;
import net.minestom.server.monitoring.BenchmarkManagerProvider;
import net.minestom.server.network.ConnectionManagerProvider;
import net.minestom.server.network.socket.ServerProvider;
import net.minestom.server.thread.ChunkDispatcherProvider;
import net.minestom.server.thread.TickSchedulerThread;
import net.minestom.server.timer.SchedulerManagerProvider;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerProcessImpl implements ServerProcess {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerProcessImpl.class);

    private final ServerSettingsProvider serverSettingsProvider;
    private final ServerProvider serverProvider;
    private final ExceptionHandlerProvider exceptionHandlerProvider;
    private final SchedulerManagerProvider schedulerManagerProvider;
    private final ConnectionManagerProvider connectionManagerProvider;
    private final BenchmarkManagerProvider benchmarkManagerProvider;
    private final ChunkDispatcherProvider chunkDispatcherProvider;
    private final TickerProvider tickerProvider;

    public ServerProcessImpl(MinecraftServer minecraftServer) {
        this(minecraftServer, minecraftServer, minecraftServer, minecraftServer, minecraftServer, minecraftServer, minecraftServer, minecraftServer);
    }

    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicBoolean stopped = new AtomicBoolean();

    public ServerProcessImpl(ServerSettingsProvider serverSettingsProvider, ServerProvider serverProvider, ExceptionHandlerProvider exceptionHandlerProvider, SchedulerManagerProvider schedulerManagerProvider, ConnectionManagerProvider connectionManagerProvider, BenchmarkManagerProvider benchmarkManagerProvider, ChunkDispatcherProvider chunkDispatcherProvider, TickerProvider tickerProvider) {
        this.serverSettingsProvider = serverSettingsProvider;
        this.serverProvider = serverProvider;
        this.exceptionHandlerProvider = exceptionHandlerProvider;
        this.schedulerManagerProvider = schedulerManagerProvider;
        this.connectionManagerProvider = connectionManagerProvider;
        this.benchmarkManagerProvider = benchmarkManagerProvider;
        this.chunkDispatcherProvider = chunkDispatcherProvider;
        this.tickerProvider = tickerProvider;
    }

    @Override
    public void start(@NotNull SocketAddress socketAddress) {
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("Server already started");
        }

        LOGGER.info("Starting " + serverSettingsProvider.getServerSettings().getBrandName() + " server.");

        // Init server
        try {
            serverProvider.getServer().init(socketAddress);
        } catch (IOException e) {
            exceptionHandlerProvider.getExceptionHandler().handleException(e);
            throw new RuntimeException(e);
        }

        // Start server
        serverProvider.getServer().start();

        LOGGER.info(serverSettingsProvider.getServerSettings().getBrandName() + " server started successfully.");

        // Stop the server on SIGINT
        if (serverSettingsProvider.getServerSettings().isShutdownOnSignal())
            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        new TickSchedulerThread(serverSettingsProvider.getServerSettings(), tickerProvider.getTicker(), this, exceptionHandlerProvider.getExceptionHandler()).start();
    }

    @Override
    public void stop() {
        if (!stopped.compareAndSet(false, true))
            return;
        LOGGER.info("Stopping " + serverSettingsProvider.getServerSettings().getBrandName() + " server.");
        schedulerManagerProvider.getSchedulerManager().shutdown();
        connectionManagerProvider.getConnectionManager().shutdown();
        serverProvider.getServer().stop();
        LOGGER.info("Shutting down all thread pools.");
        benchmarkManagerProvider.getBenchmarkManager().disable();
        chunkDispatcherProvider.getChunkDispatcher().shutdown();
        LOGGER.info(serverSettingsProvider.getServerSettings().getBrandName() + " server stopped successfully.");
    }

    @Override
    public boolean isAlive() {
        return started.get() && !stopped.get();
    }
}
