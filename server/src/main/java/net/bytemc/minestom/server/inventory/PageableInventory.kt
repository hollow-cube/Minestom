package net.bytemc.minestom.server.inventory

import net.bytemc.minestom.server.inventory.item.ClickType
import net.bytemc.minestom.server.inventory.item.ClickableItem
import net.kyori.adventure.text.Component
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*
import kotlin.math.ceil
import kotlin.math.min

abstract class PageableInventory<T>(title: String, type: InventoryType, clickable: Boolean, val values: List<T>) : SingletonInventory(title, type, clickable) {
    private var currentPage = 1
    private var possibleAmount: Int

    init {
        clear()
        possibleAmount = Arrays.stream(inventory.itemStacks).filter {
            it.material() == Material.AIR
        }.count().toInt()

        createPage(1)
    }

    abstract fun constructItem(value: T): ClickableItem

    fun createPage(id: Int) {
        currentPage = id
        clear()

        if(currentPage > 1) {
            fill(inventory.size / 9, 2, ClickableItem(ItemStack.of(Material.ARROW).withDisplayName(Component.text("§7Back"))).subscribe({
                createPage(currentPage - 1)
            }, ClickType.ALL))
        }

        if(currentPage < getMaxPage()) {
            fill(inventory.size / 9, 6, ClickableItem(ItemStack.of(Material.ARROW).withDisplayName(Component.text("§7Forward"))).subscribe({
                createPage(currentPage + 1)
            }, ClickType.ALL))
        }

        if(values.isEmpty()) {
            fill((inventory.size / 9) / 2, ClickableItem(ItemStack.of(Material.RED_CARPET).withDisplayName(Component.text("§cNo values present§8!"))))
        }

        for (element in values.subList(possibleAmount * (currentPage - 1), min(values.size.toDouble(), (possibleAmount * (currentPage - 1) + possibleAmount).toDouble()).toInt())) {
            fill(constructItem(element))
        }
    }

    private fun clear() {
        for(i in 0 until inventory.size) {
            inventory.setItemStack(i, ItemStack.of(Material.AIR))
        }
    }

    fun getMaxPage(): Int {
        return ceil(values.size.toDouble() / possibleAmount).toInt()
    }
}