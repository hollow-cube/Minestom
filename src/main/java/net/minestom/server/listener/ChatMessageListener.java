package net.minestom.server.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.message.ChatPosition;
import net.minestom.server.message.Messenger;
import net.minestom.server.network.packet.client.play.ClientChatMessagePacket;
import net.minestom.server.network.packet.client.play.ClientCommandChatPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Function;

public class ChatMessageListener {

    public static void commandChatListener(ClientCommandChatPacket packet, Player player) {
        final String command = packet.message();
        if (Messenger.canReceiveCommand(player)) {
            player.getServerProcess().getCommandManager().execute(player, command);
        } else {
            Messenger.sendRejectionMessage(player);
        }
    }

    public static void chatMessageListener(ClientChatMessagePacket packet, Player player) {
        final String message = packet.message();
        if (!Messenger.canReceiveMessage(player)) {
            Messenger.sendRejectionMessage(player);
            return;
        }

        final Collection<Player> players = player.getServerProcess().getConnectionManager().getOnlinePlayers();
        PlayerChatEvent playerChatEvent = new PlayerChatEvent(player, players, () -> buildDefaultChatMessage(player, message), message);

        // Call the event
        player.getServerProcess().getGlobalEventHandler().callCancellable(playerChatEvent, () -> {
            final Function<PlayerChatEvent, Component> formatFunction = playerChatEvent.getChatFormatFunction();

            Component textObject;

            if (formatFunction != null) {
                // Custom format
                textObject = formatFunction.apply(playerChatEvent);
            } else {
                // Default format
                textObject = playerChatEvent.getDefaultChatFormat().get();
            }

            final Collection<Player> recipients = playerChatEvent.getRecipients();
            if (!recipients.isEmpty()) {
                // delegate to the messenger to avoid sending messages we shouldn't be
                Messenger.sendMessage(player.getServerProcess().getServerSetting(), recipients, textObject, ChatPosition.CHAT, player.getUuid());
            }
        });
    }

    private static @NotNull Component buildDefaultChatMessage(@NotNull Player player, @NotNull String message) {
        final String username = player.getUsername();
        return Component.translatable("chat.type.text")
                .args(Component.text(username)
                                .insertion(username)
                                .clickEvent(ClickEvent.suggestCommand("/msg " + username + " "))
                                .hoverEvent(player),
                        Component.text(message)
                );
    }

}