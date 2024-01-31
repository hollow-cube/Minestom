package net.minestom.server.network;

import net.minestom.server.entity.Player;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.ClientPacketsHandler;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;


public class PacketProcessorImpl implements PacketProcessor {
    private final ClientPacketsHandler statusHandler;
    private final ClientPacketsHandler loginHandler;
    private final ClientPacketsHandler configurationHandler;
    private final ClientPacketsHandler playHandler;

    private final PacketListenerManager packetListenerManager;

    public PacketProcessorImpl(@NotNull PacketListenerManager packetListenerManager) {
        statusHandler = new ClientPacketsHandler.Status();
        loginHandler = new ClientPacketsHandler.Login();
        configurationHandler = new ClientPacketsHandler.Configuration();
        playHandler = new ClientPacketsHandler.Play();

        this.packetListenerManager = packetListenerManager;
    }

    @Override
    public @NotNull ClientPacket create(@NotNull ConnectionState connectionState, int packetId, ByteBuffer body) {
        NetworkBuffer buffer = new NetworkBuffer(body);
        final ClientPacket clientPacket = switch (connectionState) {
            case HANDSHAKE -> {
                assert packetId == 0;
                yield new ClientHandshakePacket(buffer);
            }
            case STATUS -> statusHandler.create(packetId, buffer);
            case LOGIN -> loginHandler.create(packetId, buffer);
            case CONFIGURATION -> configurationHandler.create(packetId, buffer);
            case PLAY -> playHandler.create(packetId, buffer);
        };
        body.position(buffer.readIndex());
        return clientPacket;
    }

    @Override
    public ClientPacket process(@NotNull PlayerConnection connection, int packetId, ByteBuffer body) {
        final ClientPacket packet = create(connection.getConnectionState(), packetId, body);

        switch (connection.getConnectionState()) {
            // Process all pre-config packets immediately
            case HANDSHAKE, STATUS, LOGIN -> packetListenerManager.processClientPacket(packet, connection);
            // Process config and play packets on the next tick
            case CONFIGURATION, PLAY -> {
                final Player player = connection.getPlayer();
                assert player != null;
                player.addPacketToQueue(packet);
            }
        }
        return packet;
    }
}
