package net.bytemc.minestom.server.inventory.item.impl;

import net.bytemc.minestom.server.inventory.SingletonInventory;
import net.bytemc.minestom.server.inventory.item.Item;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

import java.util.function.Predicate;

@SuppressWarnings("unused")
public final class SwitchItem implements Item {
    private final ItemStack itemStack;
    private final Item change;

    private Predicate<Player> predicate;

    public SwitchItem(ItemStack itemStack, Item change) {
        this.itemStack = itemStack;
        this.change = change;
    }

    public SwitchItem(ItemStack itemStack, Item change, Predicate<Player> predicate) {
        this.itemStack = itemStack;
        this.change = change;
        this.predicate = predicate;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void click(Player player, SingletonInventory inventory, int slot) {
        if(predicate != null && !this.predicate.test(player)) {
            return;
        }
        inventory.fill(slot, change);
    }
}
