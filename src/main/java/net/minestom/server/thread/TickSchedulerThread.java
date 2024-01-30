package net.minestom.server.thread;

import net.minestom.server.ServerConsts;
import net.minestom.server.ServerProcess;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.locks.LockSupport;

@ApiStatus.Internal
public final class TickSchedulerThread extends MinestomThread {
    private final ServerProcess serverProcess;

    private final long startTickNs = System.nanoTime();
    private long tick = 1;

    public TickSchedulerThread(ServerProcess serverProcess) {
        super(ServerConsts.THREAD_NAME_TICK_SCHEDULER);
        this.serverProcess = serverProcess;
    }

    @Override
    public void run() {
        final long tickNs = (long) (serverProcess.getMinecraftServer().getTickMs() * 1e6);
        while (serverProcess.isAlive()) {
            final long tickStart = System.nanoTime();
            try {
                serverProcess.ticker().tick(tickStart);
            } catch (Exception e) {
                serverProcess.getExceptionManager().handleException(e);
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
