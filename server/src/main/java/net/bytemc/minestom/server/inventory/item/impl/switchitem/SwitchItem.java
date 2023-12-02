package net.bytemc.minestom.server.inventory.item.impl.switchitem;

import net.bytemc.minestom.server.inventory.SingletonInventory;
import net.bytemc.minestom.server.inventory.item.Item;
import net.bytemc.minestom.server.inventory.item.impl.ClickableItem;
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
        return switches.get(currentItem).item().getItemStack();
    }

    public void click(Player player, SingletonInventory inventory, int slot) {
        var nextIndex = (currentItem + 1) % switches.size();

        var nextItem = switches.get(nextIndex);
        if(nextItem.predicate() != null && !nextItem.predicate().test(player)) {
            return;
        }

        currentItem = nextIndex;
        nextItem.consumer().accept(player);
        inventory.fill(slot, this);
    }
}
