package net.bytemc.minestom.server

import net.minestom.server.MinecraftServer

fun main(args: Array<String>) {
    println("[ByteServer] initializing minestom server...")

    val server = MinecraftServer.init()

    println("[ByteServer] initializing byteServer...")
    ByteServer(server, args)
}