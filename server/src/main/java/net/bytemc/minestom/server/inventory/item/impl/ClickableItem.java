package net.bytemc.minestom.server.inventory.item.impl;

import net.bytemc.minestom.server.inventory.item.ClickType;
import net.bytemc.minestom.server.inventory.item.Item;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public final class ClickableItem implements Item {
    private final ItemStack itemStack;
    private final Map<Consumer<Player>, List<ClickType>> onClick;

    public ClickableItem(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.onClick = new HashMap<>();
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ClickableItem subscribe(Consumer<Player> onClick, ClickType... clickTypes) {
        if (clickTypes.length == 0) {
            clickTypes = new ClickType[] { ClickType.ALL };
        }
        this.onClick.put(onClick, Arrays.stream(clickTypes).toList());
        return this;
    }

    public void click(Player player, String identifier) {
        for (Map.Entry<Consumer<Player>, List<ClickType>> entry : onClick.entrySet()) {
            for (ClickType clickType : entry.getValue()) {
                if(clickType.getIdentifier().equals(identifier) || clickType.getIdentifier().equals("ALL")) {
                    entry.getKey().accept(player);
                    break;
                }
            }
        }
    }
}
