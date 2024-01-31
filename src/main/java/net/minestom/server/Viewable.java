package net.minestom.server;

import net.kyori.adventure.audience.Audience;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Represents something which can be displayed or hidden to players.
 */
public interface Viewable {

    /**
     * Adds a viewer.
     *
     * @param player the viewer to add
     * @return true if the player has been added, false otherwise (could be because he is already a viewer)
     */
    boolean addViewer(@NotNull Player player);

    /**
     * Removes a viewer.
     *
     * @param player the viewer to remove
     * @return true if the player has been removed, false otherwise (could be because he was not a viewer)
     */
    boolean removeViewer(@NotNull Player player);

    /**
     * Gets all the viewers of this viewable element.
     *
     * @return A Set containing all the element's viewers
     */
    @NotNull Set<@NotNull Player> getViewers();

    /**
     * Gets if a player is seeing this viewable object.
     *
     * @param player the player to check
     * @return true if {@code player} is a viewer, false otherwise
     */
    default boolean isViewer(@NotNull Player player) {
        return getViewers().contains(player);
    }

    /**
     * Sends a packet to all viewers.
     * <p>
     * It is better than looping through the viewers
     * to send a packet since it is here only serialized once.
     *
     * @param packet the packet to send to all viewers
     */
    default void sendPacketToViewers(ServerSettingsProvider serverSettingsProvider, @NotNull SendablePacket packet) {
        if (packet instanceof ServerPacket serverPacket) {
            PacketUtils.sendGroupedPacket(serverSettingsProvider, getViewers(), serverPacket);
        } else {
            getViewers().forEach(player -> player.sendPacket(packet));
        }
    }

    default void sendPacketsToViewers(ServerSettingsProvider serverSettingsProvider, @NotNull Collection<SendablePacket> packets) {
        for (SendablePacket packet : packets) {
            sendPacketToViewers(serverSettingsProvider, packet);
        }
    }

    default void sendPacketsToViewers(ServerSettingsProvider serverSettingsProvider, @NotNull SendablePacket... packets) {
        sendPacketsToViewers(serverSettingsProvider, List.of(packets));
    }

    /**
     * Sends a packet to all viewers and the viewable element if it is a player.
     * <p>
     * If 'this' isn't a player, then only {@link #sendPacketToViewers(ServerSettingsProvider, SendablePacket)} is called.
     *
     * @param packet the packet to send
     */
    default void sendPacketToViewersAndSelf(ServerSettingsProvider serverSettingsProvider, @NotNull SendablePacket packet) {
        sendPacketToViewers(serverSettingsProvider, packet);
    }

    /**
     * Gets the result of {@link #getViewers()} as an Adventure Audience.
     *
     * @return the audience
     */
    default @NotNull Audience getViewersAsAudience(ServerSettings serverSettings) {
        return PacketGroupingAudience.of(serverSettings, this.getViewers());
    }

    /**
     * Gets the result of {@link #getViewers()} as an {@link Iterable} of Adventure
     * {@link Audience}s.
     *
     * @return the audiences
     */
    default @NotNull Iterable<? extends Audience> getViewersAsAudiences() {
        return this.getViewers();
    }
}
