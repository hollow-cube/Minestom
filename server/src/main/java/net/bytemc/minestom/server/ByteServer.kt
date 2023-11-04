package net.bytemc.minestom.server

import net.bytemc.minestom.server.clickable.InteractItem
import net.bytemc.minestom.server.handler.BlockHandlers
import net.bytemc.minestom.server.instance.InstanceHandler
import net.minestom.server.MinecraftServer
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.utils.NamespaceID
import java.util.function.Consumer


class ByteServer(server: MinecraftServer) {
    val instanceHandler = InstanceHandler()

    init {
        instance = this

        MinecraftServer.getBlockManager().registerHandler(NamespaceID.from("minecraft:skull")) { BlockHandlers.SKULL_HANDLER }
        MinecraftServer.getBlockManager().registerHandler(NamespaceID.from("minecraft:banner")) { BlockHandlers.BANNER_HELPER }

        server.start("127.0.0.1", 25565)
        println("[ByteServer] minestom server was started!")

        MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockBreakEvent::class.java) { event ->
            var item = InteractItem(ItemStack.of(Material.STICK)) {
                run {
                    it.sendMessage("§aYou clicked the item!")

                }
            }
            item.equip(event.player, 0)
        }

    }

    companion object {
        private lateinit var instance: ByteServer

        fun getInstance(): ByteServer {
            return instance
        }
    }
}