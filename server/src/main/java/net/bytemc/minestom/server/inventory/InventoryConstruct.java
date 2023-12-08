package net.bytemc.minestom.server.inventory;

import net.bytemc.minestom.server.inventory.item.Item;
import net.bytemc.minestom.server.inventory.item.impl.ClickableItem;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface InventoryConstruct {

    void setItem(int slot, @NotNull Item stack);

    void addItem(@NotNull Item stack);

    void removeItem(int slot);

    void removeItem(@NotNull Item clickableItem);

    void open(@NotNull Player player);

    void clear();

    void setTitle(String title);

}

