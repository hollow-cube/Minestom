package net.minestom.server.permission;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// TODO: more tests
public class TestPermissions {

    private Player player;
    private TestPermissionHandler handler;

    private String permission1, permission2, permission3;

    @BeforeEach
    public void init() {
        MinecraftServer.init(); // for entity manager
        player = new Player(UUID.randomUUID(), "TestPlayer", null) {
            @Override
            protected void playerConnectionInit() {
            }

            @Override
            public boolean isOnline() {
                return false;
            }
        };

        handler = new TestPermissionHandler();
        player.setPermissionHandler(handler);

        permission1 = "perm.name";
        permission2 = "perm.name2";
        permission3 = "perm.name2.sub.sub2";
    }

    @Test
    public void noPermission() {
        assertFalse(player.hasPermission(""));
        assertFalse(player.hasPermission("random.permission"));
    }

    @Test
    public void hasPermissionClass() {
        assertFalse(player.hasPermission(permission1));
        handler.addPermission(permission1);
        assertTrue(player.hasPermission(permission1));
        assertFalse(player.hasPermission(permission2));

        handler.addPermission(permission2);
        assertTrue(player.hasPermission(permission2));
    }

    @AfterEach
    public void cleanup() {

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
