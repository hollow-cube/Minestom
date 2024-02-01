package net.minestom.server.listener.preplay;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket;
import org.jetbrains.annotations.NotNull;

public final class ConfigListener {

    public static void finishConfigListener(@NotNull ClientFinishConfigurationPacket packet, @NotNull Player player) {
        player.getConnectionManagerProvider().getConnectionManager().transitionConfigToPlay(player);
    }
}
