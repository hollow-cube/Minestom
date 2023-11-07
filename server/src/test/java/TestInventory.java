import net.bytemc.minestom.server.inventory.AnvilInventory;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TestInventory extends AnvilInventory {

    public TestInventory(@NotNull String title, @NotNull Player targetPlayer) {
        super(title, targetPlayer);
    }

    @Override
    public void onSubmit(@NotNull Player player, @NotNull String value) {

    }
}
