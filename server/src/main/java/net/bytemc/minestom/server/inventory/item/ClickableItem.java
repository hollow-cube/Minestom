package net.bytemc.minestom.server.inventory.item;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public final class ClickableItem {
    private final ItemStack itemStack;
    private final Map<Consumer<Player>, List<ClickType>> onClick;

    public ClickableItem(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.onClick = new HashMap<>();
    }

    public ItemStack getItemStack() {
        return itemStack;
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
