package net.minestom.server.listener.manager;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

public interface PacketListenerManager {

    /**
     * Processes a packet by getting its {@link PacketPlayListenerConsumer} and calling all the packet listeners.
     *
     * @param packet     the received packet
     * @param connection the connection of the player who sent the packet
     * @param <T>        the packet type
     */
    <T extends ClientPacket> void processClientPacket(@NotNull T packet, @NotNull PlayerConnection connection);

    /**
     * Sets the listener of a packet.
     * <p>
     * WARNING: this will overwrite the default minestom listener, this is not reversible.
     *
     * @param state       the state of the packet
     * @param packetClass the class of the packet
     * @param consumer    the new packet's listener
     * @param <T>         the type of the packet
     */
    <T extends ClientPacket> void setListener(@NotNull ConnectionState state, @NotNull Class<T> packetClass, @NotNull PacketPrePlayListenerConsumer<T> consumer);

    /**
     * Sets the listener of a packet.
     * <p>
     * WARNING: this will overwrite the default minestom listener, this is not reversible.
     *
     * @param packetClass the class of the packet
     * @param consumer    the new packet's listener
     * @param <T>         the type of the packet
     */
    <T extends ClientPacket> void setPlayListener(@NotNull Class<T> packetClass, @NotNull PacketPlayListenerConsumer<T> consumer);

    <T extends ClientPacket> void setConfigurationListener(@NotNull Class<T> packetClass, @NotNull PacketPlayListenerConsumer<T> consumer);

    /**
     * Sets the listener of a packet.
     * <p>
     * WARNING: this will overwrite the default minestom listener, this is not reversible.
     *
     * @param packetClass the class of the packet
     * @param consumer    the new packet's listener
     * @param <T>         the type of the packet
     */
    @Deprecated
    <T extends ClientPacket> void setListener(@NotNull Class<T> packetClass, @NotNull PacketPlayListenerConsumer<T> consumer);
}
