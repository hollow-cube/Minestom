package net.minestom.server.inventory.type;

import net.kyori.adventure.text.Component;
import net.minestom.server.ServerProcess;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryProperty;
import net.minestom.server.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

public class AnvilInventory extends Inventory {

    private short repairCost;

    public AnvilInventory(ServerProcess serverProcess, @NotNull Component title) {
        super(serverProcess, InventoryType.ANVIL, title);
    }

    public AnvilInventory(ServerProcess serverProcess, @NotNull String title) {
        super(serverProcess, InventoryType.ANVIL, title);
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
