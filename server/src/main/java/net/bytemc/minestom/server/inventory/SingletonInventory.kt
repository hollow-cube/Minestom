package net.bytemc.minestom.server.inventory

import net.bytemc.minestom.server.inventory.item.ClickableItem
import net.minestom.server.entity.Player
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.Material

open class SingletonInventory(var title: String, var type: InventoryType, var clickable: Boolean) {
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

    fun fill(clickableItem: ClickableItem) {
        for(i in 0 until inventory.size) {
            if(inventory.itemStacks[i].material() == Material.AIR) {
                fill(i, clickableItem)
                break
            }
        }
    }

    fun fill(slot: Int, clickableItem: ClickableItem) {
        inventory.setItemStack(slot, clickableItem.itemStack)
        items[slot] = clickableItem
    }

    fun fill(row: Int, slot: Int, clickableItem: ClickableItem) {
        fill(((row - 1) * 9) + slot, clickableItem)
    }

    fun fillRow(row: Int, clickableItem: ClickableItem) {
        for(i in 0 until 9) {
            fill(row, i, clickableItem)
        }
    }
}