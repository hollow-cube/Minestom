package net.bytemc.minestom.server.inventory.anvil;

import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;

import java.util.function.BiConsumer;

record AnvilEntry(int level, BiConsumer<Player, String> consumer, Inventory inventory) {
}
