package net.minestom.server.permission;

import org.jetbrains.annotations.NotNull;

/**
 * A default permission handler. Used when no custom permission handler is set.
 */
public final class DefaultPermissionHandler implements PermissionHandler {
    @Override
    public boolean hasPermission(@NotNull String permission) {
        return false;
    }
}
