package net.bytemc.minestom.server.utils;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;

public class FlyingItem {

    private LivingEntity parent;
    private ItemEntity item;
    private ItemStack stack;
    private Instance instance;
    private Pos pos;

    public FlyingItem(ItemStack stack, Instance instance, Pos pos) {
        this.instance = instance;
        this.pos = pos;
        this.stack = stack;
    }

    public void spawn() {
        this.parent = new LivingEntity(EntityType.ARMOR_STAND);

        var meta = (ArmorStandMeta) parent.getEntityMeta();
        meta.setHasNoGravity(true);
        meta.setInvisible(true);
        meta.setMarker(true);

        this.parent.setInstance(instance, pos).whenComplete((unused, throwable) -> {
            this.parent.spawn();
            item = new ItemEntity(this.stack);
            item.setAutoViewable(true);
            item.setInstance(instance, parent.getPosition());
            item.setNoGravity(true);
            item.setPickable(false);
        });
    }

    public void updateItem(ItemStack stack) {
        this.stack = stack;
        this.item.setItemStack(stack);
    }

    public void teleport(Instance instance, Pos pos) {
        this.instance = instance;
        this.pos = pos;

        this.parent.setInstance(instance, pos);
        this.item.setInstance(instance, pos);
    }

    public void destroy() {
        this.item.remove();
        this.parent.remove();
    }
}
