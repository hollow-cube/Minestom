package net.bytemc.minestom.server.inventory.item.impl.switchitem;

import net.bytemc.minestom.server.inventory.item.Item;
import net.minestom.server.entity.Player;

import java.util.function.Consumer;
import java.util.function.Predicate;

public final class SwitchEntry {
    private final Item item;
    private final Consumer<Player> consumer;
    private final Predicate<Player> predicate;

    public SwitchEntry(Item item, Consumer<Player> consumer, Predicate<Player> predicate) {
        this.item = item;
        this.consumer = consumer;
        this.predicate = predicate;
    }

    public Item getItem() {
        return item;
    }

    public Consumer<Player> getConsumer() {
        return consumer;
    }

    public Predicate<Player> getPredicate() {
        return predicate;
    }
}
