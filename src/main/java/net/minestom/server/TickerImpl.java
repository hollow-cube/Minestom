package net.minestom.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.exception.ExceptionHandler;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.socket.Server;
import net.minestom.server.thread.Acquirable;
import net.minestom.server.thread.ThreadDispatcher;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.utils.PacketUtils;

@Getter
@RequiredArgsConstructor
final class TickerImpl implements Ticker {
    private final ConnectionManager connectionManager;
    private final SchedulerManager schedulerManager;
    private final Server server;
    private final EventNode<Event> globalEventHandler;
    private final ExceptionHandler exceptionHandler;
    private final InstanceManager instanceManager;
    private final ThreadDispatcher<Chunk> dispatcher;

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
        getDispatcher().updateAndAwait(tickStart);

        // Clear removed entities & update threads
        final long tickTime = System.currentTimeMillis() - tickStart;
        getDispatcher().refreshThreads(tickTime);
    }
}
