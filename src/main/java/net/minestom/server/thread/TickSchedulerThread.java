package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerSettings;
import net.minestom.server.ServerStarter;
import net.minestom.server.Ticker;
import net.minestom.server.exception.ExceptionHandler;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.locks.LockSupport;

@ApiStatus.Internal
public final class TickSchedulerThread extends MinestomThread {

    private final long startTickNs = System.nanoTime();
    private final ServerSettings serverSettings;
    private final Ticker ticker;
    private final ServerStarter serverStarter;
    private final ExceptionHandler exceptionHandler;
    private long tick = 1;

    public TickSchedulerThread(ServerSettings serverSettings, Ticker ticker, ServerStarter serverStarter, ExceptionHandler exceptionHandler) {
        super(MinecraftServer.THREAD_NAME_TICK_SCHEDULER);
        this.serverSettings = serverSettings;
        this.ticker = ticker;
        this.serverStarter = serverStarter;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void run() {
        final long tickNs = (long) (serverSettings.getTickMs() * 1e6);
        while (serverStarter.isAlive()) {
            final long tickStart = System.nanoTime();
            try {
                ticker.tick(tickStart);
            } catch (Exception e) {
                exceptionHandler.handleException(e);
            }
            fixTickRate(tickNs);
        }
    }

    private void fixTickRate(long tickNs) {
        long nextTickNs = startTickNs + (tickNs * tick);
        if (System.nanoTime() < nextTickNs) {
            while (true) {
                // Checks in every 1/10 ms to see if the current time has reached the next scheduled time.
                Thread.yield();
                LockSupport.parkNanos(100000);
                long currentNs = System.nanoTime();
                if (currentNs >= nextTickNs) {
                    break;
                }
            }
        }
        tick++;
    }
}
