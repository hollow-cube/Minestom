package net.minestom.server.entity.playerspecific;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

// for heads and holograms that can be shown to players differently
public class ArmorStand extends EntityCreature {
    @Nullable
    private final Function<Player, ItemStack> getHead;
    @Nullable
    private final Function<Player, Component> getName;
    private final HelperCreature helper;

    public ArmorStand(@Nullable Function<Player, ItemStack> getHead,
                      @Nullable Function<Player, Component> getName) {
        super(EntityType.ARMOR_STAND);
        this.getHead = getHead;
        this.getName = getName;

        helper = new HelperCreature(this);
        metadata = helper.getMetadata();
        entityMeta = helper.getEntityMeta();
        setCustomNameVisible(true);
    }
    public ArmorStand(@Nullable Function<Player, ItemStack> getHead,
                      @Nullable Component name) {
        this(getHead, name == null ? null : p -> name);
    }
    public ArmorStand(@Nullable ItemStack head,
                      @Nullable Function<Player, Component> getName) {
        this(head == null ? null : p -> head, getName);
    }
    public ArmorStand(@Nullable ItemStack head,
                      @Nullable Component name) {
        this(head == null ? null : p -> head,
                name == null ? null : p -> name);
    }

    @Override
    public void updateNewViewer(@NotNull Player player) {
        super.updateNewViewer(player);
        update(player);
    }

    public void update(@NotNull Player player) {
        var meta = getEntityMeta();
        var nameBefore = meta.getCustomName();
        var headBefore = getHelmet();
        meta.setCustomName(getName == null ? Component.empty() : getName.apply(player));
        meta.setCustomNameVisible(!meta.getCustomName().equals(Component.empty()));
        helper.setHelmet(getHead == null ? ItemStack.AIR : getHead.apply(player));
        player.sendPackets(getMetadataPacket(), getEquipmentsPacket());
        meta.setCustomName(nameBefore);
        helper.setHelmet(headBefore);
    }

    @Override
    public void setUuid(@NotNull UUID uuid) {
        super.setUuid(uuid);
        helper.setUuid(uuid);
    }

    @NotNull
    @Override
    public ArmorStandMeta getEntityMeta() {
        return (ArmorStandMeta) super.getEntityMeta();
    }

    @NotNull
    @Override
    public EntityEquipmentPacket getEquipmentsPacket() {
        return new EntityEquipmentPacket(getEntityId(), Map.of(
                EquipmentSlot.MAIN_HAND,
                helper.getItemInMainHand(),
                EquipmentSlot.OFF_HAND,
                helper.getItemInOffHand(),
                EquipmentSlot.BOOTS,
                helper.getBoots(),
                EquipmentSlot.LEGGINGS,
                helper.getLeggings(),
                EquipmentSlot.CHESTPLATE,
                helper.getChestplate(),
                EquipmentSlot.HELMET,
                helper.getHelmet()));
    }
}
