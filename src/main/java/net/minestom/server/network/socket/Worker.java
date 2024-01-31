package net.minestom.server.network.socket;

import net.minestom.server.ServerSettings;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.exception.ExceptionHandler;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.server.thread.MinestomThread;
import net.minestom.server.utils.ObjectPool;
import net.minestom.server.utils.binary.BinaryBuffer;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscUnboundedXaddArrayQueue;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ApiStatus.Internal
public final class Worker extends MinestomThread {
    private static final AtomicInteger COUNTER = new AtomicInteger();

    final Selector selector;
    private final Map<SocketChannel, PlayerSocketConnection> connectionMap = new ConcurrentHashMap<>();

    private final ConnectionManager connectionManager;
    private final EventNode<Event> globalEventHandler;
    private final ExceptionHandler exceptionHandler;
    private final ServerSettings serverSettings;
    private final Server server;
    private final MpscUnboundedXaddArrayQueue<Runnable> queue = new MpscUnboundedXaddArrayQueue<>(1024);

    Worker(Server server, ConnectionManager connectionManager, EventNode<Event> globalEventHandler, ExceptionHandler exceptionHandler, ServerSettings serverSettings) {
        super("Ms-worker-" + COUNTER.getAndIncrement());
        this.connectionManager = connectionManager;
        this.globalEventHandler = globalEventHandler;
        this.exceptionHandler = exceptionHandler;
        this.serverSettings = serverSettings;
        this.server = server;
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void tick() {
        this.selector.wakeup();
    }

    @Override
    public void run() {
        while (server.isOpen()) {
            try {
                try {
                    this.queue.drain(Runnable::run);
                } catch (Exception e) {
                    exceptionHandler.handleException(e);
                }
                // Flush all connections if needed
                for (PlayerSocketConnection connection : connectionMap.values()) {
                    try {
                        connection.flushSync();
                    } catch (Exception e) {
                        connection.disconnect();
                    }
                }
                // Wait for an event
                this.selector.select(key -> {
                    final SocketChannel channel = (SocketChannel) key.channel();
                    if (!channel.isOpen()) return;
                    if (!key.isReadable()) return;
                    final PlayerSocketConnection connection = connectionMap.get(channel);
                    if (connection == null) {
                        try {
                            channel.close();
                        } catch (IOException e) {
                            // Empty
                        }
                        return;
                    }
                    try {
                        try (var holder = ObjectPool.PACKET_POOL.hold()) {
                            BinaryBuffer readBuffer = BinaryBuffer.wrap(holder.get());
                            // Consume last incomplete packet
                            connection.consumeCache(readBuffer);
                            // Read & process
                            readBuffer.readChannel(channel);
                            connection.processPackets(readBuffer, server.packetProcessor());
                        }
                    } catch (IOException e) {
                        // TODO print exception? (should ignore disconnection)
                        connection.disconnect();
                    } catch (Throwable t) {
                        exceptionHandler.handleException(t);
                        connection.disconnect();
                    }
                });
            } catch (Exception e) {
                exceptionHandler.handleException(e);
            }
        }
    }

    public void disconnect(PlayerSocketConnection connection, SocketChannel channel) {
        assert !connection.isOnline();
        assert Thread.currentThread() == this;
        this.connectionMap.remove(channel);
        if (channel.isOpen()) {
            try {
                connection.flushSync();
                channel.close();
            } catch (IOException e) {
                // Socket operation may fail if the socket is already closed
            }
        }
    }

    void receiveConnection(SocketChannel channel) throws IOException {
        this.connectionMap.put(channel, new PlayerSocketConnection(server, connectionManager, globalEventHandler, exceptionHandler, serverSettings, this, channel, channel.getRemoteAddress()));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        if (channel.getLocalAddress() instanceof InetSocketAddress) {
            Socket socket = channel.socket();

            socket.setSendBufferSize(serverSettings.getSendBufferSize());
            socket.setReceiveBufferSize(serverSettings.getReceiveBufferSize());
            socket.setTcpNoDelay(serverSettings.isTcpNoDelay());
            socket.setSoTimeout(30 * 1000); // 30 seconds
        }
    }

    public MessagePassingQueue<Runnable> queue() {
        return queue;
    }
}
