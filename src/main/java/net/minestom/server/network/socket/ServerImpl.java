package net.minestom.server.network.socket;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerSettings;
import net.minestom.server.event.GlobalEventHandlerProvider;
import net.minestom.server.exception.ExceptionHandlerProvider;
import net.minestom.server.network.ConnectionManagerProvider;
import net.minestom.server.network.PacketProcessor;
import net.minestom.server.network.PacketProcessorProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public final class ServerImpl implements Server {

    private final ServerSettings serverSettings;
    private final ExceptionHandlerProvider exceptionHandlerProvider;
    private final PacketProcessorProvider packetProcessorProvider;
    private volatile boolean stop;

    private final Selector selector;
    private final List<Worker> workers;
    private int index;

    private ServerSocketChannel serverSocket;
    private SocketAddress socketAddress;
    private String address;
    private int port;

    public ServerImpl(ServerSettings serverSettings, MinecraftServer minecraftServer) {
        this(serverSettings, minecraftServer, minecraftServer, minecraftServer, minecraftServer);
    }

    public ServerImpl(ServerSettings serverSettings, ConnectionManagerProvider connectionManagerProvider, GlobalEventHandlerProvider globalEventHandlerProvider, ExceptionHandlerProvider exceptionHandlerProvider, PacketProcessorProvider packetProcessorProvider) {
        this.serverSettings = serverSettings;
        this.exceptionHandlerProvider = exceptionHandlerProvider;
        this.packetProcessorProvider = packetProcessorProvider;
        Worker[] workers = new Worker[serverSettings.getWorkers()];
        Arrays.setAll(workers, value -> new Worker(this, connectionManagerProvider, globalEventHandlerProvider, exceptionHandlerProvider, () -> serverSettings));
        this.workers = List.of(workers);
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @ApiStatus.Internal
    public void init(SocketAddress address) throws IOException {
        ProtocolFamily family;
        if (address instanceof InetSocketAddress inetSocketAddress) {
            this.address = inetSocketAddress.getHostString();
            this.port = inetSocketAddress.getPort();
            family = inetSocketAddress.getAddress().getAddress().length == 4 ? StandardProtocolFamily.INET : StandardProtocolFamily.INET6;
        } else if (address instanceof UnixDomainSocketAddress unixDomainSocketAddress) {
            this.address = "unix://" + unixDomainSocketAddress.getPath();
            this.port = 0;
            family = StandardProtocolFamily.UNIX;
        } else {
            throw new IllegalArgumentException("Address must be an InetSocketAddress or a UnixDomainSocketAddress");
        }

        ServerSocketChannel server = ServerSocketChannel.open(family);
        server.bind(address);
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);
        this.serverSocket = server;
        this.socketAddress = address;

        if (address instanceof InetSocketAddress && port == 0) {
            port = server.socket().getLocalPort();
        }
    }

    @Override
    @ApiStatus.Internal
    public void start() {
        this.workers.forEach(Thread::start);
        new Thread(() -> {
            while (!stop) {
                // Busy wait for connections
                try {
                    this.selector.select(key -> {
                        if (!key.isAcceptable()) return;
                        try {
                            // Register socket and forward to thread
                            Worker worker = findWorker();
                            final SocketChannel client = serverSocket.accept();
                            worker.receiveConnection(client);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (IOException e) {
                    exceptionHandlerProvider.getExceptionHandler().handleException(e);
                }
            }
        }, "Ms-entrypoint").start();
    }

    @Override
    public void tick() {
        this.workers.forEach(Worker::tick);
    }

    @Override
    public boolean isOpen() {
        return !stop;
    }

    @Override
    public void stop() {
        this.stop = true;
        try {
            if(serverSocket != null) {
                this.serverSocket.close();
            }

            if (socketAddress instanceof UnixDomainSocketAddress unixDomainSocketAddress) {
                Files.deleteIfExists(unixDomainSocketAddress.getPath());
            }
        } catch (IOException e) {
            exceptionHandlerProvider.getExceptionHandler().handleException(e);
        }
        this.selector.wakeup();
        this.workers.forEach(worker -> worker.selector.wakeup());
    }

    @Override
    @ApiStatus.Internal
    public @NotNull PacketProcessor packetProcessor() {
        return packetProcessorProvider.getPacketProcessor();
    }

    @Override
    public SocketAddress socketAddress() {
        return socketAddress;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public int getPort() {
        return port;
    }

    private Worker findWorker() {
        this.index = ++index % serverSettings.getWorkers();
        return workers.get(index);
    }
}
