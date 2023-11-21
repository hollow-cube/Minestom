package net.bytemc.minestom.server.inventory

import net.bytemc.minestom.server.inventory.item.Item
import net.bytemc.minestom.server.inventory.item.impl.ClickableItem
import net.bytemc.minestom.server.inventory.item.impl.toggle.SwitchItem
import net.bytemc.minestom.server.inventory.item.impl.ToggleItem
import net.minestom.server.entity.Player
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.Material

open class SingletonInventory(var title: String, var type: InventoryType, var clickable: Boolean) {
    var inventory: Inventory = Inventory(type, title)
    private var items: HashMap<Int, Item> = HashMap()

    init {
        inventory.addInventoryCondition { player, slot, clickType, result ->
            val item = items[slot]
            if(item is ClickableItem) {
                item.click(player, clickType.name)
            } else if(item is SwitchItem) {
                item.click(player,this, slot)
            } else if(item is ToggleItem) {
                item.click(player,this, slot)
            }

            if (!clickable) {
                result.isCancel = true
            }
        }
    }

    fun open(player: Player) {
        player.openInventory(inventory)
    }

    fun fill(item: Item) {
        for(i in 0 until inventory.size) {
            if(inventory.itemStacks[i].material() == Material.AIR) {
                fill(i, item)
                break
            }
        }
    }

    fun fill(slot: Int, item: Item) {
        inventory.setItemStack(slot, item.itemStack)
        items[slot] = item
    }

    fun fill(row: Int, slot: Int, item: Item) {
        fill(((row - 1) * 9) + slot, item)
    }

    fun fillRow(row: Int, item: Item) {
        for(i in 0 until 9) {
            fill(row, i, item)
        }
    }
}