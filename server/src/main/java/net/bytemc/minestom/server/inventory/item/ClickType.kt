package net.bytemc.minestom.server.inventory.item

enum class ClickType(var identifier: String) {
    ALL("ALL"),
    LEFT("LEFT_CLICK"),
    RIGHT("RIGHT_CLICK"),
    SHIFT("START_SHIFT_CLICK"),
    DOUBLE("START_DOUBLE_CLICK"),

}