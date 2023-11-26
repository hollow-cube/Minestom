package net.bytemc.minestom.server.inventory.item.impl.switchitem;

import net.bytemc.minestom.server.inventory.item.Item;
import net.minestom.server.entity.Player;

import java.util.function.Consumer;
import java.util.function.Predicate;

public record SwitchEntry(Item item, Consumer<Player> consumer, Predicate<Player> predicate) {
}