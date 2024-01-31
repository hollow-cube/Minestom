package net.minestom.server.extras;

import lombok.Getter;
import net.minestom.server.ServerStarterProvider;
import net.minestom.server.exception.ExceptionHandlerProvider;
import net.minestom.server.extras.mojangAuth.MojangCrypt;
import net.minestom.server.utils.validate.Check;

import java.security.KeyPair;

public final class MojangAuth {
    public final String AUTH_URL = System.getProperty("minestom.auth.url", "https://sessionserver.mojang.com/session/minecraft/hasJoined").concat("?username=%s&serverId=%s");
    @Getter
    private volatile boolean enabled = false;
    @Getter
    private volatile KeyPair keyPair;
    private final ServerStarterProvider serverStarterProvider;
    @Getter
    private final MojangCrypt mojangCrypt;

    public MojangAuth(ServerStarterProvider serverStarterProvider, ExceptionHandlerProvider exceptionHandlerProvider) {
        this.serverStarterProvider = serverStarterProvider;
        this.mojangCrypt = new MojangCrypt(exceptionHandlerProvider);
    }

    /**
     * Enables mojang authentication on the server.
     * <p>
     * Be aware that enabling a proxy will make Mojang authentication ignored.
     */
    public void init() {
        Check.stateCondition(enabled, "Mojang auth is already enabled!");
        Check.stateCondition(serverStarterProvider.getServerStarter().isAlive(), "The server has already been started!");
        enabled = true;
        // Generate necessary fields...
        keyPair = mojangCrypt.generateKeyPair();
    }
}
