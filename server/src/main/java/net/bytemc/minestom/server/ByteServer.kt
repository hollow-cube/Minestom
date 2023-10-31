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
    }

    companion object {
        lateinit var instance: ByteServer
    }
}