package net.bytemc.minestom.server

import net.minestom.server.MinecraftServer

fun main() {
    println("[ByteServer] initializing minestom server...")

    val server = MinecraftServer.init()

    println("[ByteServer] initializing byteServer...")
    ByteServer(server)
}