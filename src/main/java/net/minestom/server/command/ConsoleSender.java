package net.minestom.server.command;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.permission.PermissionProvider;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Represents the console when sending a command to the server.
 */
public class ConsoleSender implements CommandSender {
    private static final ComponentLogger LOGGER = ComponentLogger.logger(ConsoleSender.class);

    private final TagHandler tagHandler = TagHandler.newHandler();
    private PermissionProvider permissionProvider;

    @Override
    public void sendMessage(@NotNull String message) {
        LOGGER.info(message);
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
        LOGGER.info(message);
    }

    public void setPermissionProvider(@NotNull PermissionProvider permissionProvider) {
        this.permissionProvider = permissionProvider;
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        if (permissionProvider == null) return true;
        return permissionProvider.hasPermission(permission);
    }

    @Override
    public boolean isConsole() {
        return true;
    }

    @Override
    public ConsoleSender asConsole() {
        return this;
    }

    @Override
    public @NotNull TagHandler tagHandler() {
        return tagHandler;
    }
}
