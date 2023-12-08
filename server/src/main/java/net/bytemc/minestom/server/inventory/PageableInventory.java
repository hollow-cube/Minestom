package net.bytemc.minestom.server.inventory;

import net.bytemc.minestom.server.inventory.item.impl.ClickableItem;
import net.kyori.adventure.text.Component;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.PlayerHeadMeta;

import java.util.List;

public abstract class PageableInventory<T> extends SingletonInventory {

    private static final ItemStack NEXT_PAGE_ITEM = ItemStack.of(Material.PLAYER_HEAD)
            .withMeta(new PlayerHeadMeta.Builder().withTexture("https://textures.minecraft.net/texture/19bf3292e126a105b54eba713aa1b152d541a1d8938829c56364d178ed22bf").build())
            .withDisplayName(Component.text("§8» §bNächste §7Seite"));

    private static final ItemStack BE_PAGE_ITEM = ItemStack.of(Material.PLAYER_HEAD)
            .withMeta(new PlayerHeadMeta.Builder().withTexture("https://textures.minecraft.net/texture/bd69e06e5dadfd84e5f3d1c21063f2553b2fa945ee1d4d7152fdc5425bc12a9").build())
            .withDisplayName(Component.text("§8» §bVorherige §7Seite"));

    private final List<T> elements;
    private final int[] possibleSlots;

    private int currentPage = 1;
    private final ClickableItem localNextPageItem = new ClickableItem(NEXT_PAGE_ITEM).subscribe(it -> buildPage(++currentPage));
    private final ClickableItem localBehaviorPageItem = new ClickableItem(BE_PAGE_ITEM).subscribe(it -> buildPage(--currentPage));

    public PageableInventory(InventoryType type, String title, boolean clickable, int[] possibleSlots, List<T> values) {
        super(type, clickable, title);
        this.elements = values;
        this.possibleSlots = possibleSlots;
        this.fill();
        this.buildPage(1);
    }

    //set placeholder or something like this
    public void fill() {}

    public abstract ClickableItem constructItem(T value);

    @SuppressWarnings("EmptyMethod")
    public void onChangePage(PageableInventory<T> pageableInventory) {}

    public int calculateNextPageSlot() {
        return this.getInventory().getSize() - 1;
    }

    public int calculateBehaviorPageSlot() {
        return this.getInventory().getSize() - 9;
    }

    private void buildPage(int id) {

        this.currentPage = id;
        this.clear();

        if (currentPage > 1) {
            setItem(calculateBehaviorPageSlot(), localBehaviorPageItem);
        } else {
            getInventory().setItemStack(calculateBehaviorPageSlot(), ItemStack.AIR);
            this.getItems().remove(calculateBehaviorPageSlot());
        }


        if(elements.size() == possibleSlots.length) {
            getInventory().setItemStack(calculateNextPageSlot(), ItemStack.AIR);
            this.getItems().remove(calculateNextPageSlot());
        }else if (currentPage < getMaximalPage()) {
            setItem(calculateNextPageSlot(), localNextPageItem);
        } else {
            getInventory().setItemStack(calculateNextPageSlot(), ItemStack.AIR);
            this.getItems().remove(calculateNextPageSlot());
        }

        int stepId = 0;
        for (T element : elements.subList(possibleSlots.length * (currentPage - 1), Math.min(elements.size(), possibleSlots.length * (currentPage - 1) + possibleSlots.length))) {
            setItem(possibleSlots[stepId], constructItem(element));
            stepId++;
        }
        onChangePage(this);
    }

    @Override
    public void clear() {
        for (int possibleSlot : possibleSlots) getInventory().setItemStack(possibleSlot, ItemStack.AIR);
    }

    public int getMaximalPage() {
        return elements.size() / possibleSlots.length;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public Inventory getInventory() {
        return super.getInventory();
    }

    public List<T> getElements() {
        return elements;
    }
}
