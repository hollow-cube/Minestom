package net.minestom.server.inventory.type;

import net.kyori.adventure.text.Component;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryProperty;
import net.minestom.server.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

public class LecternInventory extends Inventory {
    private short page;

    public LecternInventory(@NotNull Component title) {
        super(InventoryType.LECTERN, title);
    }

    public LecternInventory(@NotNull String title) {
        super(InventoryType.LECTERN, title);
    }

    /**
     * Gets the viewed page number.
     *
     * @return the page number
     */
    public short getPage() {
        return page;
    }

    /**
     * Sets the viewed page number.
     *
     * @param page the new page number
     */
    public void setPage(short page) {
        this.page = page;
        sendProperty(InventoryProperty.LECTERN_PAGE_NUMBER, page);
    }
}
