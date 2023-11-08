package net.bytemc.minestom.server.inventory.item;

public enum ClickType {
    ALL("ALL"),
    LEFT("LEFT_CLICK"),
    RIGHT("RIGHT_CLICK"),
    SHIFT("START_SHIFT_CLICK"),
    DOUBLE("START_DOUBLE_CLICK");

    private final String identifier;

    ClickType(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
