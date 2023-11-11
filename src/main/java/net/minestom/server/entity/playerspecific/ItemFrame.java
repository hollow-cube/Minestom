package net.minestom.server.entity.playerspecific;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.ItemFrameMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ItemFrame extends Entity {
    private final Function<Player, ItemStack> getItemStack;
    private final Helper helper;

    public ItemFrame(@NotNull Function<@NotNull Player, @NotNull ItemStack> getItemStack) {
        super(EntityType.ITEM_FRAME);
        this.getItemStack = getItemStack;

        helper = new Helper(this);
        metadata = helper.getMetadata();
        entityMeta = helper.getEntityMeta();
    }

    @Override
    public @NotNull ItemFrameMeta getEntityMeta() {
        return (ItemFrameMeta) super.getEntityMeta();
    }

    @Override
    public void setUuid(@NotNull UUID uuid) {
        super.setUuid(uuid);
        helper.setUuid(uuid);
    }

    @Override
    public void updateNewViewer(@NotNull Player player) {
        super.updateNewViewer(player);
        update(player);
    }

    public void update(@NotNull Player player) {
        withItemStack(getItemStack.apply(player), () -> {
            var packet = getMetadataPacket();
            player.sendPacket(packet);
        });
    }

    private void withItemStack(ItemStack itemStack, Runnable action) {
        var itemBefore = getEntityMeta().getItem();
        getEntityMeta().setItem(itemStack);
        action.run();
        getEntityMeta().setItem(itemBefore);
    }

    public void setOrientation(Direction direction) {
        getEntityMeta().setOrientation(direction.getItemFrameOrientation());
        position = getPosition().withDirection(new Vec(
                direction.normalX(),
                direction.normalY(),
                direction.normalZ()));
    }

    @Override
    public CompletableFuture<Void> setInstance(@NotNull Instance instance,
                                               @NotNull Point spawnPosition) {
        return super.setInstance(instance,
                Pos.fromPoint(spawnPosition).withDirection(position.direction()));
    }
}
