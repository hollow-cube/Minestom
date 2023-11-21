package net.bytemc.minestom.server.inventory.item.impl;

import net.bytemc.minestom.server.inventory.SingletonInventory;
import net.bytemc.minestom.server.inventory.item.Item;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public final class SwitchItem implements Item {
    private final List<SwitchEntry> switches;

    private int currentItem = 0;

    public SwitchItem(ItemStack item, Consumer<Player> consumer, Predicate<Player> predicate) {
        this.switches = new ArrayList<>();

        addSwitch(new ClickableItem(item), consumer, predicate);
    }

    public SwitchItem addSwitch(Item item, Consumer<Player> onSwitch, Predicate<Player> predicate) {
        this.switches.add(new SwitchEntry(item, onSwitch, predicate));
        return this;
    }

    public ItemStack getItemStack() {
        return switches.get(0).getItem().getItemStack();
    }

    public void click(Player player, SingletonInventory inventory, int slot) {
        currentItem++;
        if(currentItem >= switches.size()) {
            currentItem = 0;
        }

        var item = switches.get(currentItem);
        if(item.getPredicate() != null && !item.getPredicate().test(player)) {
            currentItem--;
            return;
        }

        item.getConsumer().accept(player);
        inventory.fill(slot, item.getItem());
    }

    private static class SwitchEntry {
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
}
