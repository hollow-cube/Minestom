package net.bytemc.minestom.server.inventory

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.inventory.InventoryCloseEvent
import net.minestom.server.event.player.PlayerPacketEvent
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket

abstract class AnvilInventory(var title: String, var targetPlayer: Player) {
    var inventory: Inventory = Inventory(InventoryType.ANVIL, title)
    var oldLevel: Int = targetPlayer.level

    init {
        inventory.addInventoryCondition { player, slot, _, result ->
            result.isCancel = true
        }

        targetPlayer.level = 1
        MinecraftServer.getGlobalEventHandler().addListener(PlayerPacketEvent::class.java) { event ->
            if (event.packet is ClientClickWindowPacket) {
                val packet: ClientClickWindowPacket = event.packet as ClientClickWindowPacket
                if(packet.windowId == inventory.getWindowId() && packet.slot.toInt() == 2 && packet.clickedItem.getDisplayName() != null) {
                    onSubmit(event.player, (packet.clickedItem.getDisplayName() as TextComponent).content())
                    targetPlayer.closeInventory()
                    targetPlayer.level = oldLevel
                }
            }
        }

        MinecraftServer.getGlobalEventHandler().addListener(InventoryCloseEvent::class.java) { event ->
            event.player.level = oldLevel
        }

        inventory.setItemStack(0, ItemStack.of(Material.BAMBOO_SIGN).withDisplayName(Component.text("Enter value")))
    }

    abstract fun onSubmit(player: Player, value: String)

    fun open() {
        targetPlayer.openInventory(inventory)
    }
}