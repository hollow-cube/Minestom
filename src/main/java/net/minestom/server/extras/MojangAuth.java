package net.minestom.server.extras;

import net.minestom.server.ServerProcessProvider;
import net.minestom.server.exception.ExceptionHandlerProvider;
import net.minestom.server.extras.mojangAuth.MojangCrypt;
import net.minestom.server.utils.validate.Check;

import java.security.KeyPair;

public final class MojangAuth {
    public final String AUTH_URL = System.getProperty("minestom.auth.url", "https://sessionserver.mojang.com/session/minecraft/hasJoined").concat("?username=%s&serverId=%s");
    private volatile boolean enabled = false;
    private volatile KeyPair keyPair;
    private final ServerProcessProvider serverProcessProvider;
    private final MojangCrypt mojangCrypt;

    public MojangAuth(ServerProcessProvider serverProcessProvider, ExceptionHandlerProvider exceptionHandlerProvider) {
        this.serverProcessProvider = serverProcessProvider;
        this.mojangCrypt = new MojangCrypt(exceptionHandlerProvider);
    }

    /**
     * Enables mojang authentication on the server.
     * <p>
     * Be aware that enabling a proxy will make Mojang authentication ignored.
     */
    public void init() {
        Check.stateCondition(enabled, "Mojang auth is already enabled!");
        Check.stateCondition(serverProcessProvider.getServerProcess().isAlive(), "The server has already been started!");
        enabled = true;
        // Generate necessary fields...
        keyPair = mojangCrypt.generateKeyPair();
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public KeyPair getKeyPair() {
        return this.keyPair;
    }

    public MojangCrypt getMojangCrypt() {
        return this.mojangCrypt;
    }
}
