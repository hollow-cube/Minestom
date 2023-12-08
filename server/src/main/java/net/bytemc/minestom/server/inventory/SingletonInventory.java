package net.bytemc.minestom.server.inventory;

import net.bytemc.minestom.server.inventory.item.Item;
import net.bytemc.minestom.server.inventory.item.impl.ClickableItem;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonInventory implements InventoryConstruct {

    private final Inventory inventory;
    private final Map<Integer, Item> items = new ConcurrentHashMap<>();

    public SingletonInventory(InventoryType inventoryType, boolean clickable, String title) {
        this.inventory = new Inventory(inventoryType, title);
        this.inventory.addInventoryCondition((player, slot, clickType, result) -> {
            result.setCancel(!clickable);
            if (items.containsKey(slot) && items.get(slot) instanceof ClickableItem clickableItem) {
                clickableItem.click(player, clickType.name());
            }
        });
    }

    @Override
    public void setItem(int slot, @NotNull Item stack) {
        this.inventory.setItemStack(slot, stack.getItemStack());
        this.items.put(slot, stack);
    }

    @Override
    public void addItem(@NotNull Item stack) {
        var slot = this.getNextSlot();
        this.inventory.setItemStack(slot, stack.getItemStack());
        this.items.put(slot, stack);
    }

    @Override
    public void removeItem(int slot) {
        this.inventory.setItemStack(slot, ItemStack.AIR);
        this.items.remove(slot);
    }

    @Override
    public void removeItem(@NotNull Item clickableItem) {
        removeItem(this.getSlotByClickedItem(clickableItem));
    }

    @Override
    public void open(@NotNull Player player) {
        player.openInventory(this.inventory);
    }

    @Override
    public void clear() {
        this.inventory.clear();
    }

    @Override
    public void setTitle(String title) {
        this.inventory.setTitle(Component.text(title));
    }

    private int getNextSlot() {
        for (int i = 0; i < this.inventory.getSize(); i++) if (!items.containsKey(i)) return i;
        return -1;
    }

    private int getSlotByClickedItem(Item clickableItem) {
        for (Integer slot : this.items.keySet()) if (this.items.get(slot).equals(clickableItem)) return slot;
        return -1;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Map<Integer, Item> getItems() {
        return items;
    }
}
