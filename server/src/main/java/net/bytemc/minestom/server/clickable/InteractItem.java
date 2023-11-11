package net.bytemc.minestom.server.clickable;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class InteractItem {

    private static final List<InteractItem> ITEM_LIST = new ArrayList<>();

    static {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerUseItemEvent.class, event -> {
            //TODO FIX IF BlockPlacementListener.java line 58
            for (InteractItem item : ITEM_LIST) {
                if (event.getPlayer().getItemInMainHand().equals(item.item)) {
                    item.interactionPlayer.accept(event.getPlayer());
                }
                break;
            }
        });
    }

    private final ItemStack item;
    private final Consumer<Player> interactionPlayer;

    public InteractItem(ItemStack item, Consumer<Player> interactionPlayer) {
        this.item = item;
        this.interactionPlayer = interactionPlayer;

        ITEM_LIST.add(this);
    }

    public void equip(Player player, int slot) {
        player.getInventory().setItemStack(slot, this.item);
    }

    public void remove() {
        ITEM_LIST.remove(this);
    }
}
