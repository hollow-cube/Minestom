package net.minestom.server.inventory.type;

import net.kyori.adventure.text.Component;
import net.minestom.server.ServerSettings;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryProperty;
import net.minestom.server.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

public class BrewingStandInventory extends Inventory {

    private short brewTime;
    private short fuelTime;

    public BrewingStandInventory(@NotNull EventNode<Event> globalEventHandler, @NotNull ServerSettings serverSettings, @NotNull Component title) {
        super(globalEventHandler, serverSettings, InventoryType.BREWING_STAND, title);
    }

    public BrewingStandInventory(@NotNull EventNode<Event> globalEventHandler, @NotNull ServerSettings serverSettings, @NotNull String title) {
        super(globalEventHandler, serverSettings, InventoryType.BREWING_STAND, title);
    }

    /**
     * Gets the brewing stand brew time.
     *
     * @return the brew time in tick
     */
    public short getBrewTime() {
        return brewTime;
    }

    /**
     * Changes the brew time.
     *
     * @param brewTime the new brew time in tick
     */
    public void setBrewTime(short brewTime) {
        this.brewTime = brewTime;
        sendProperty(InventoryProperty.BREWING_STAND_BREW_TIME, brewTime);
    }

    /**
     * Gets the brewing stand fuel time.
     *
     * @return the fuel time in tick
     */
    public short getFuelTime() {
        return fuelTime;
    }

    /**
     * Changes the fuel time.
     *
     * @param fuelTime the new fuel time in tick
     */
    public void setFuelTime(short fuelTime) {
        this.fuelTime = fuelTime;
        sendProperty(InventoryProperty.BREWING_STAND_FUEL_TIME, fuelTime);
    }

}
