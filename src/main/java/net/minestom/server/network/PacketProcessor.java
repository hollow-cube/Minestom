package net.minestom.server.network;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.ClientPacketsHandler;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Responsible for processing client packets.
 * <p>
 * You can retrieve the different packet handlers per state (status/login/play)
 * from the {@link ClientPacketsHandler} classes.
 */
public interface PacketProcessor {
    @NotNull ClientPacket create(@NotNull ConnectionState connectionState, int packetId, ByteBuffer body);

    ClientPacket process(@NotNull PlayerConnection connection, int packetId, ByteBuffer body);
}
