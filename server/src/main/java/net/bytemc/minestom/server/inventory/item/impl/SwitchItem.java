package net.bytemc.minestom.server.inventory.item.impl;

import net.bytemc.minestom.server.inventory.SingletonInventory;
import net.bytemc.minestom.server.inventory.item.Item;
import net.minestom.server.item.ItemStack;

@SuppressWarnings("unused")
public final class SwitchItem implements Item {
    private final ItemStack itemStack;
    private final Item change;

    public SwitchItem(ItemStack itemStack, Item change) {
        this.itemStack = itemStack;
        this.change = change;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void click(SingletonInventory inventory, int slot) {
        inventory.fill(slot, change);
    }
}
