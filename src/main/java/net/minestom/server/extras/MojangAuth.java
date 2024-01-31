package net.minestom.server.extras;

import net.minestom.server.ServerStarter;
import net.minestom.server.exception.ExceptionHandler;
import net.minestom.server.extras.mojangAuth.MojangCrypt;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

import java.security.KeyPair;

public final class MojangAuth {
    public final String AUTH_URL = System.getProperty("minestom.auth.url", "https://sessionserver.mojang.com/session/minecraft/hasJoined").concat("?username=%s&serverId=%s");
    private volatile boolean enabled = false;
    private volatile KeyPair keyPair;
    private final ServerStarter serverStarter;
    private final MojangCrypt mojangCrypt;

    public MojangAuth(ServerStarter serverStarter, ExceptionHandler exceptionHandler) {
        this.serverStarter = serverStarter;
        this.mojangCrypt = new MojangCrypt(exceptionHandler);
    }

    /**
     * Enables mojang authentication on the server.
     * <p>
     * Be aware that enabling a proxy will make Mojang authentication ignored.
     */
    public void init() {
        Check.stateCondition(enabled, "Mojang auth is already enabled!");
        Check.stateCondition(serverStarter.isAlive(), "The server has already been started!");
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
