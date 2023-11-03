package net.bytemc.minestom.server

import net.bytemc.minestom.server.handler.BlockHandlers
import net.bytemc.minestom.server.instance.InstanceHandler
import net.minestom.server.MinecraftServer
import net.minestom.server.utils.NamespaceID


class ByteServer(server: MinecraftServer) {
    val instanceHandler = InstanceHandler()

    init {
        instance = this

        MinecraftServer.getBlockManager().registerHandler(NamespaceID.from("minecraft:skull")) { BlockHandlers.SKULL_HANDLER }
        MinecraftServer.getBlockManager().registerHandler(NamespaceID.from("minecraft:banner")) { BlockHandlers.BANNER_HELPER }

        server.start("127.0.0.1", 25565)
        println("[ByteServer] minestom server was started!")

        // Just testing
        /*MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockBreakEvent::class.java) {
            TestInventory().open(it.player)
        }
        fakePlayerHandler.register("", PlayerSkin.fromUsername("FlxwDNS")!!, instanceHandler.getSpawnInstance(), Pos(0.0, 44.0, 0.0))*/
    }

    companion object {
        lateinit var instance: ByteServer
    }
}