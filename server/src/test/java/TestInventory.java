import net.bytemc.minestom.server.inventory.SingletonInventory;
import net.bytemc.minestom.server.inventory.item.impl.ClickableItem;
import net.bytemc.minestom.server.inventory.item.impl.switchitem.SwitchItem;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class TestInventory extends SingletonInventory {

    public TestInventory() {
        super("Test", InventoryType.CHEST_3_ROW, false);

        var switchItem = new SwitchItem(ItemStack.of(Material.RED_CONCRETE_POWDER), player -> {

        }, player -> {
            return true;
        });
        switchItem.addSwitch(new ClickableItem(ItemStack.of(Material.DIRT)), player -> {

        }, player -> true);
        switchItem.addSwitch(new ClickableItem(ItemStack.of(Material.GRASS)), player -> {

        }, player -> true);

        fill(switchItem);
    }
}
