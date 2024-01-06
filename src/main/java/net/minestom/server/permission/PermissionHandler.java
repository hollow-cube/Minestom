package net.minestom.server.permission;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an object which can have permissions.
 * <p>
 * Permissions are in-memory only by default.
 */
public interface PermissionHandler {
    /**
     * Gets if this handler has the permission {@code permission}.
     *
     * @param permission the permission to check
     * @return true if the handler has the permission, false otherwise
     */
    boolean hasPermission(@NotNull String permission);
}
