package net.minestom.server;

import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public interface ServerProcess {
    void start(@NotNull SocketAddress socketAddress);

    default void start(@NotNull String hostname, int port) {
        start(new InetSocketAddress(hostname, port));
    }

    void stop();

    boolean isAlive();
}
