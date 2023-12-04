package net.bytemc.minestom.server.inventory.anvil;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;


import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class AnvilInventory {
    private final static Map<Player, AnvilEntry> entries;

    static {
        entries = new HashMap<>();

        MinecraftServer.getGlobalEventHandler().addListener(PlayerPacketEvent.class, event -> {
            var player = event.getPlayer();
            var entry = entries.get(player);
            if (event.getPacket() instanceof ClientClickWindowPacket packet) {
                var inventory = entry.inventory();
                if(packet.windowId() == inventory.getWindowId() && ((int) packet.slot()) == 2 && packet.clickedItem().getDisplayName() != null) {
                    var textComponent = ((TextComponent) packet.clickedItem().getDisplayName());

                    player.closeInventory();
                    player.setLevel(entry.level());

                    entries.remove(player);

                    entry.consumer().accept(player, textComponent.content());
                }
            }
        });
    }

    public static void open(Player player, String customName, BiConsumer<Player, String> onSubmit) {
        var inventory = new Inventory(InventoryType.ANVIL, "§7");

        inventory.setItemStack(0, ItemStack.of(Material.BAMBOO_SIGN).withDisplayName(Component.text(customName)));
        entries.put(player, new AnvilEntry(player.getLevel(), onSubmit, inventory));

        player.setLevel(1);
        player.openInventory(inventory);
    }
}
