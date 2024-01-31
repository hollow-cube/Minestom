package net.minestom.server.network.socket;

import net.minestom.server.network.PacketProcessor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.SocketAddress;

public interface Server {
    int MAX_PACKET_SIZE = Integer.getInteger("minestom.max-packet-size", 2_097_151); // 3 bytes var-int

    @ApiStatus.Internal
    void init(SocketAddress address) throws IOException;

    @ApiStatus.Internal
    void start();

    void tick();

    boolean isOpen();

    void stop();

    @ApiStatus.Internal
    @NotNull PacketProcessor packetProcessor();

    SocketAddress socketAddress();

    String getAddress();

    int getPort();
}
