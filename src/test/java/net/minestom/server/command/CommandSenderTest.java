package net.minestom.server.command;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.permission.PermissionHandler;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CommandSenderTest {

    @Test
    public void testSenderPermissions() {
        TestPermissionHandler handler = new TestPermissionHandler();

        String permission = "permission.test";

        assertEquals(handler.getAllPermissions(), Set.of());

        handler.addPermission(permission);
        assertEquals(handler.getAllPermissions(), Set.of(permission));

        handler.removePermission(permission);
        assertEquals(handler.getAllPermissions(), Set.of());
    }

    @Test
    public void testMessageSending() {
        SenderTest sender = new SenderTest();

        assertNull(sender.getMostRecentMessage());

        sender.sendMessage("Hey!!");
        assertEquals(sender.getMostRecentMessage(), Component.text("Hey!!"));

        sender.sendMessage(new String[]{"Message", "Sending", "Test"});
        assertEquals(sender.getMostRecentMessage(), Component.text("Test"));

        sender.sendMessage(Component.text("Message test!", NamedTextColor.GREEN));
        assertEquals(sender.getMostRecentMessage(), Component.text("Message test!", NamedTextColor.GREEN));
    }

    private static final class SenderTest implements CommandSender {

        private final TagHandler handler = TagHandler.newHandler();

        private Component mostRecentMessage = null;

        @Override
        public @NotNull TagHandler tagHandler() {
            return handler;
        }

        @Override
        public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
            mostRecentMessage = message;
        }

        public @Nullable Component getMostRecentMessage() {
            return mostRecentMessage;
        }

        @Override
        public @NotNull Identity identity() {
            return Identity.nil();
        }

        @Override
        public @NotNull PermissionHandler getPermissionHandler() {
            return null;
        }

        @Override
        public void setPermissionHandler(@NotNull PermissionHandler handler) {

        }
    }

    private static final class TestPermissionHandler implements PermissionHandler {

        private final Set<String> permissions = new HashSet<>();

        @Override
        public boolean hasPermission(@NotNull String permission) {
            return permissions.contains(permission);
        }

        public @NotNull Set<String> getAllPermissions() {
            return Set.copyOf(this.permissions);
        }

        public void addPermission(@NotNull String permission) {
            this.permissions.add(permission);
        }

        public void removePermission(@NotNull String permission) {
            this.permissions.remove(permission);
        }
    }
}
