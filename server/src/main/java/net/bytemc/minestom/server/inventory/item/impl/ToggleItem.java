package net.bytemc.minestom.server.inventory.item.impl;

import net.bytemc.minestom.server.inventory.SingletonInventory;
import net.bytemc.minestom.server.inventory.item.Item;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

import java.util.function.Predicate;

@SuppressWarnings("unused")
public final class ToggleItem implements Item {
    private final ItemStack itemStack;
    private final Item change;

    private int currentItem = 0;
    private Predicate<Player> predicate;

    public ToggleItem(ItemStack itemStack, Item change) {
        this.itemStack = itemStack;
        this.change = change;
    }

    public ToggleItem(ItemStack itemStack, Item change, Predicate<Player> predicate) {
        this(itemStack, change);
        this.predicate = predicate;
    }

    public ItemStack getItemStack() {
        return currentItem == 0 ? itemStack : change.getItemStack();
    }

    public void click(Player player, SingletonInventory inventory, int slot) {
        if(predicate != null && !this.predicate.test(player)) {
            return;
        }
        currentItem = 1 - currentItem; // 1 -> 0, 0 -> 1
        inventory.fill(slot, this);
    }
}
