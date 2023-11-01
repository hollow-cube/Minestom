package net.bytemc.minestom.server.inventory

import net.minestom.server.entity.Player
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType

abstract class SingletonInventory(var title: String, var type: InventoryType, var clickable: Boolean) {
    var inventory: Inventory = Inventory(type, title)
    var items: HashMap<Int, ClickableItem> = HashMap()

    init {
        inventory.addInventoryCondition { player, slot, clickType, result ->
            items[slot]?.click(player, clickType.name)

            if (!clickable) {
                result.isCancel = true
            }
        }
    }

    fun open(player: Player) {
        player.openInventory(inventory)
    }

    fun fill(slot: Int, clickableItem: ClickableItem) {
        inventory.setItemStack(slot, clickableItem.itemStack)
        items[slot] = clickableItem
    }

    fun fill(row: Int, slot: Int, clickableItem: ClickableItem) {
        fill(((row - 1) * 9) + slot, clickableItem)
    }
}