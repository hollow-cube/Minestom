package net.minestom.server.extras;

import net.minestom.server.MinecraftServer;
import net.minestom.server.extras.mojangAuth.MojangCrypt;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

import java.security.KeyPair;

public final class MojangAuth {
    public final String AUTH_URL = System.getProperty("minestom.auth.url", "https://sessionserver.mojang.com/session/minecraft/hasJoined").concat("?username=%s&serverId=%s");
    private volatile boolean enabled = false;
    private volatile KeyPair keyPair;
    private final MinecraftServer minecraftServer;
    private final MojangCrypt mojangCrypt;

    public MojangAuth(MinecraftServer minecraftServer) {
        this.minecraftServer = minecraftServer;
        this.mojangCrypt = new MojangCrypt(minecraftServer);
    }

    /**
     * Enables mojang authentication on the server.
     * <p>
     * Be aware that enabling a proxy will make Mojang authentication ignored.
     */
    public void init() {
        Check.stateCondition(enabled, "Mojang auth is already enabled!");
        Check.stateCondition(minecraftServer.process().isAlive(), "The server has already been started!");
        enabled = true;
        // Generate necessary fields...
        keyPair = mojangCrypt.generateKeyPair();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public @Nullable KeyPair getKeyPair() {
        return keyPair;
    }

    public MojangCrypt getMojangCrypt() {
        return mojangCrypt;
    }
}
