package net.minestom.server.permission;

import org.jetbrains.annotations.NotNull;

public interface PermissionProvider {

    boolean hasPermission(@NotNull String permission);

}
