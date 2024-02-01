package net.minestom.server.network.player;

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.crypto.PlayerPublicKey;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManagerProvider;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.socket.ServerProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.List;

/**
 * A PlayerConnection is an object needed for all created {@link Player}.
 * It can be extended to create a new kind of player (NPC for instance).
 */
public abstract class PlayerConnection {
    private final ServerProvider serverProvider;
    private final ConnectionManagerProvider connectionManagerProvider;
    @Setter
    @Getter
    private Player player;
    @Setter
    @Getter
    private volatile ConnectionState connectionState;
    @Getter
    @Setter
    private PlayerPublicKey playerPublicKey;
    @Getter
    volatile boolean online;

    public PlayerConnection(ServerProvider serverProvider, ConnectionManagerProvider connectionManagerProvider) {
        this.serverProvider = serverProvider;
        this.connectionManagerProvider = connectionManagerProvider;
        this.online = true;
        this.connectionState = ConnectionState.HANDSHAKE;
    }

    /**
     * Returns a printable identifier for this connection, will be the player username
     * or the connection remote address.
     *
     * @return this connection identifier
     */
    public @NotNull String getIdentifier() {
        final Player player = getPlayer();
        return player != null ?
                player.getUsername() :
                getRemoteAddress().toString();
    }

    /**
     * Serializes the packet and send it to the client.
     *
     * @param packet the packet to send
     */
    public abstract void sendPacket(@NotNull SendablePacket packet);

    @ApiStatus.Experimental
    public void sendPackets(@NotNull Collection<SendablePacket> packets) {
        packets.forEach(this::sendPacket);
    }

    @ApiStatus.Experimental
    public void sendPackets(@NotNull SendablePacket... packets) {
        sendPackets(List.of(packets));
    }

    /**
     * Gets the remote address of the client.
     *
     * @return the remote address
     */
    public abstract @NotNull SocketAddress getRemoteAddress();

    /**
     * Gets protocol version of client.
     *
     * @return the protocol version
     */
    public int getProtocolVersion() {
        return MinecraftServer.PROTOCOL_VERSION;
    }

    /**
     * Gets the server address that the client used to connect.
     * <p>
     * WARNING: it is given by the client, it is possible for it to be wrong.
     *
     * @return the server address used
     */
    public @Nullable String getServerAddress() {
        return serverProvider.getServer().getAddress();
    }


    /**
     * Gets the server port that the client used to connect.
     * <p>
     * WARNING: it is given by the client, it is possible for it to be wrong.
     *
     * @return the server port used
     */
    public int getServerPort() {
        return serverProvider.getServer().getPort();
    }

    /**
     * Forcing the player to disconnect.
     */
    public void disconnect() {
        this.online = false;
        connectionManagerProvider.getConnectionManager().removePlayer(this);
        final Player player = getPlayer();
        if (player != null && !player.isRemoved()) {
            player.scheduleNextTick(Entity::remove);
        }
    }

    @Override
    public String toString() {
        return "PlayerConnection{" +
                "connectionState=" + connectionState +
                ", identifier=" + getIdentifier() +
                '}';
    }
}
