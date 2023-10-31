package net.bytemc.minestom.server

import net.bytemc.minestom.server.fakeplayer.FakePlayerHandler
import net.bytemc.minestom.server.instance.InstanceHandler
import net.minestom.server.MinecraftServer

class ByteServer(server: MinecraftServer) {
    val fakePlayerHandler: FakePlayerHandler = FakePlayerHandler()
    val instanceHandler: InstanceHandler = InstanceHandler()

    init {
        instance = this

        server.start("127.0.0.1", 25565)
        println("[ByteServer] minestom server was started!")

        // Just testing
        //fakePlayerHandler.register("", PlayerSkin.fromUsername("FlxwDNS")!!, instanceHandler.getSpawnInstance(), Pos(0.0, 44.0, 0.0))
    }

    companion object {
        lateinit var instance: ByteServer
    }
}