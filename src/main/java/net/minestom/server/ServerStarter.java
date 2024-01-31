package net.minestom.server;

import lombok.RequiredArgsConstructor;
import net.minestom.server.exception.ExceptionHandler;
import net.minestom.server.instance.Chunk;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.socket.Server;
import net.minestom.server.thread.ThreadDispatcher;
import net.minestom.server.thread.TickSchedulerThread;
import net.minestom.server.timer.SchedulerManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class ServerStarter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerStarter.class);

    private final ServerSettings serverSettings;
    private final Server server;
    private final ExceptionHandler exceptionHandler;
    private final SchedulerManager schedulerManager;
    private final ConnectionManager connectionManager;
    private final BenchmarkManager benchmarkManager;
    private final ThreadDispatcher<Chunk> dispatcher;
    private final Ticker ticker;

    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicBoolean stopped = new AtomicBoolean();
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
        if (serverSettings.isShutdownOnSignal()) Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        new TickSchedulerThread(serverSettings, ticker, this, exceptionHandler).start();
    }

    public void start(@NotNull String hostname, int port) {
        start(new InetSocketAddress(hostname, port));
    }

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

    public boolean isAlive() {
        return started.get() && !stopped.get();
    }
}
