package net.bytemc.minestom.server.inventory

import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack
import java.util.function.Consumer

class ClickableItem(var itemStack: ItemStack) {
    private var onClick: HashMap<Consumer<Player>, List<ClickType>> = HashMap()

    fun subscribe(consumer: Consumer<Player>, clickType: List<ClickType>): ClickableItem {
        onClick[consumer] = clickType
        return this
    }

    fun click(player: Player, identifier: String) {
        for (entry in onClick) {
            for (it in entry.value) {
                if(it.identifier == identifier || it.identifier == "ALL") {
                    entry.key.accept(player)
                    break
                }
            }
        }
    }
}