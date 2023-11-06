package net.bytemc.minestom.server;

import net.minestom.server.MinecraftServer;

public class ByteServerBootstrap {

    public static void main(String[] args) {
        System.out.println("[ByteServer] starting minestom server...");

        var server = MinecraftServer.init();

        System.out.println("[ByteServer] initializing byteServer...");
        new ByteServer(server, args);
    }
}
