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

    private int currentItem;
    private Predicate<Player> predicate;

    public ToggleItem(ItemStack itemStack, Item change) {
        this.itemStack = itemStack;
        this.change = change;

        this.currentItem = 0;
    }

    public ToggleItem(ItemStack itemStack, Item change, Predicate<Player> predicate) {
        this(itemStack, change);

        this.predicate = predicate;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void click(Player player, SingletonInventory inventory, int slot) {
        if(predicate != null && !this.predicate.test(player)) {
            return;
        }
        currentItem++;
        if(currentItem >= 1) {
            currentItem = 0;
            inventory.fill(slot, new ClickableItem(itemStack));
        } else {
            inventory.fill(slot, change);
        }
    }
}
