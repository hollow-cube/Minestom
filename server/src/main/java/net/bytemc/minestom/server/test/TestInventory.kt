package net.bytemc.minestom.server.test

import net.bytemc.minestom.server.inventory.ClickType
import net.bytemc.minestom.server.inventory.ClickableItem
import net.bytemc.minestom.server.inventory.SingletonInventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

class TestInventory: SingletonInventory("Test", InventoryType.CHEST_6_ROW, false) {

    init {
        fill(1, ClickableItem(ItemStack.of(Material.ACACIA_BOAT)).subscribe({
            it.sendMessage("test")
        }, listOf(ClickType.ALL)))
    }

}