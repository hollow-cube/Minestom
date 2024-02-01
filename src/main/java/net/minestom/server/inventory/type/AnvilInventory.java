package net.minestom.server.inventory.type;

import net.kyori.adventure.text.Component;
import net.minestom.server.ServerSettings;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryProperty;
import net.minestom.server.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

public class AnvilInventory extends Inventory {

    private short repairCost;

    public AnvilInventory(@NotNull EventNode<Event> globalEventHandler, @NotNull ServerSettings serverSettings, @NotNull Component title) {
        super(globalEventHandler, serverSettings, InventoryType.ANVIL, title);
    }

    public AnvilInventory(@NotNull EventNode<Event> globalEventHandler, @NotNull ServerSettings serverSettings, @NotNull String title) {
        super(globalEventHandler, serverSettings, InventoryType.ANVIL, title);
    }

    /**
     * Gets the anvil repair cost.
     *
     * @return the repair cost
     */
    public short getRepairCost() {
        return repairCost;
    }

    /**
     * Sets the anvil repair cost.
     *
     * @param cost the new anvil repair cost
     */
    public void setRepairCost(short cost) {
        this.repairCost = cost;
        sendProperty(InventoryProperty.ANVIL_REPAIR_COST, cost);
    }
}
