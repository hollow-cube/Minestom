package net.minestom.server.network.socket;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerSettings;
import net.minestom.server.listener.manager.PacketListenerManagerImpl;
import net.minestom.server.network.PacketProcessorImpl;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnixDomainSocketAddress;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class ServerAddressTest {

    @Test
    public void inetAddressTest() throws IOException {
        MinecraftServer minecraftServer = MinecraftServer.of(ServerSettings.builder().build());
        // These like to fail on github actions
        assumeTrue(System.getenv("GITHUB_ACTIONS") == null);

        InetSocketAddress address = new InetSocketAddress("localhost", 25565);
        var server = new ServerImpl(minecraftServer, new PacketProcessorImpl(new PacketListenerManagerImpl(minecraftServer)));
        server.init(address);
        assertSame(address, server.socketAddress());
        assertEquals(address.getHostString(), server.getAddress());
        assertEquals(address.getPort(), server.getPort());

        assertDoesNotThrow(server::start);
        assertDoesNotThrow(server::stop);
    }

    @Test
    public void inetAddressDynamicTest() throws IOException {
        MinecraftServer minecraftServer = MinecraftServer.of(ServerSettings.builder().build());
        // These like to fail on github actions
        assumeTrue(System.getenv("GITHUB_ACTIONS") == null);

        InetSocketAddress address = new InetSocketAddress("localhost", 0);
        var server = new ServerImpl(minecraftServer, new PacketProcessorImpl(new PacketListenerManagerImpl(minecraftServer)));
        server.init(address);
        assertSame(address, server.socketAddress());
        assertEquals(address.getHostString(), server.getAddress());
        assertNotEquals(address.getPort(), server.getPort());

        assertDoesNotThrow(server::start);
        assertDoesNotThrow(server::stop);
    }

    @Test
    public void unixAddressTest() throws IOException {
        MinecraftServer minecraftServer = MinecraftServer.of(ServerSettings.builder().build());
        // These like to fail on github actions
        assumeTrue(System.getenv("GITHUB_ACTIONS") == null);

        UnixDomainSocketAddress address = UnixDomainSocketAddress.of("minestom.sock");
        var server = new ServerImpl(minecraftServer, new PacketProcessorImpl(new PacketListenerManagerImpl(minecraftServer)));
        server.init(address);
        assertTrue(Files.exists(address.getPath()));
        assertSame(address, server.socketAddress());
        assertEquals("unix://" + address.getPath(), server.getAddress());
        assertEquals(0, server.getPort());

        assertDoesNotThrow(server::start);
        assertDoesNotThrow(server::stop);
        assertFalse(Files.exists(address.getPath()), "The socket file should be deleted");
    }

    @Test
    public void noAddressTest() {
        MinecraftServer minecraftServer = MinecraftServer.of(ServerSettings.builder().build());
        var server = new ServerImpl(minecraftServer, new PacketProcessorImpl(new PacketListenerManagerImpl(minecraftServer)));
        assertDoesNotThrow(server::stop);
    }
}
