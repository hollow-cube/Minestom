package net.minestom.server;

import lombok.RequiredArgsConstructor;
import net.minestom.server.event.GlobalEventHandlerProvider;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.exception.ExceptionHandlerProvider;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManagerProvider;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.network.ConnectionManagerProvider;
import net.minestom.server.network.socket.ServerProvider;
import net.minestom.server.thread.Acquirable;
import net.minestom.server.thread.ChunkDispatcherProvider;
import net.minestom.server.timer.SchedulerManagerProvider;
import net.minestom.server.utils.PacketUtils;

@RequiredArgsConstructor
public final class TickerImpl implements Ticker {
    private final ConnectionManagerProvider connectionManagerProvider;
    private final SchedulerManagerProvider schedulerManagerProvider;
    private final ServerProvider serverProvider;
    private final GlobalEventHandlerProvider globalEventHandlerProvider;
    private final ExceptionHandlerProvider exceptionHandlerProvider;
    private final InstanceManagerProvider instanceManagerProvider;
    private final ChunkDispatcherProvider chunkDispatcherProvider;

    public TickerImpl(MinecraftServer minecraftServer) {
        this(minecraftServer, minecraftServer, minecraftServer, minecraftServer, minecraftServer, minecraftServer, minecraftServer);
    }

    @Override
    public void tick(long nanoTime) {
        final long msTime = System.currentTimeMillis();

        schedulerManagerProvider.getSchedulerManager().processTick();

        // Connection tick (let waiting clients in, send keep alives, handle configuration players packets)
        connectionManagerProvider.getConnectionManager().tick(msTime);

        // Server tick (chunks/entities)
        serverTick(msTime);

        // Flush all waiting packets
        PacketUtils.flush();

        // Server connection tick
        serverProvider.getServer().tick();

        // Monitoring
        {
            final double acquisitionTimeMs = Acquirable.resetAcquiringTime() / 1e6D;
            final double tickTimeMs = (System.nanoTime() - nanoTime) / 1e6D;
            final TickMonitor tickMonitor = new TickMonitor(tickTimeMs, acquisitionTimeMs);
            globalEventHandlerProvider.getGlobalEventHandler().call(new ServerTickMonitorEvent(tickMonitor));
        }
    }

    private void serverTick(long tickStart) {
        // Tick all instances
        for (Instance instance : instanceManagerProvider.getInstanceManager().getInstances()) {
            try {
                instance.tick(tickStart);
            } catch (Exception e) {
                exceptionHandlerProvider.getExceptionHandler().handleException(e);
            }
        }
        // Tick all chunks (and entities inside)
        chunkDispatcherProvider.getChunkDispatcher().updateAndAwait(tickStart);

        // Clear removed entities & update threads
        final long tickTime = System.currentTimeMillis() - tickStart;
        chunkDispatcherProvider.getChunkDispatcher().refreshThreads(tickTime);
    }
}
