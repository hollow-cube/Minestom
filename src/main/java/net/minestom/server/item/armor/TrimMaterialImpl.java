package net.minestom.server.item.armor;

import net.minestom.server.adventure.serializer.nbt.NbtComponentSerializer;
import net.minestom.server.item.Material;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTType;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

record TrimMaterialImpl(Registry.TrimMaterialEntry registry, int id) implements TrimMaterial {
    private static final Registry.Container<TrimMaterial> CONTAINER;

    static {
        AtomicInteger i = new AtomicInteger();
        CONTAINER = Registry.createContainer(Registry.Resource.TRIM_MATERIALS,
                (namespace, properties) -> new TrimMaterialImpl(Registry.trimMaterial(namespace, properties), i.getAndIncrement()));
    }
    public static TrimMaterial get(String namespace) {
        return CONTAINER.get(namespace);
    }

    static Collection<TrimMaterial> values() {
        return CONTAINER.values();
    }

    public NBTCompound asNBT() {
        return NBT.Compound(nbt -> {
            nbt.setString("asset_name",assetName());
            nbt.setString("ingredient",ingredient().namespace().asString());
            nbt.setFloat("item_model_index", itemModelIndex());
            nbt.set("override_armor_materials",NBT.Compound(overrideArmorMaterials().entrySet().stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    entry -> NBT.String(entry.getValue().asString())
                            ))
            ));
            nbt.set("description", NbtComponentSerializer.nbt().serialize(description()));
        });
    }
    private static NBTCompound lazyNbt = null;

    static NBTCompound getNBT() {
        if (lazyNbt == null) {
            var trimMaterials = values().stream()
                    .map((trimMaterial) -> NBT.Compound(Map.of(
                            "id", NBT.Int(trimMaterial.id()),
                            "name", NBT.String(trimMaterial.name()),
                            "element", trimMaterial.asNBT()
                    )))
                    .toList();

            lazyNbt = NBT.Compound(Map.of(
                    "type", NBT.String("minecraft:trim_material"),
                    "value", NBT.List(NBTType.TAG_Compound, trimMaterials)
            ));
        }
        return lazyNbt;
    }

    public static @Nullable TrimMaterial fromIngredient(Material ingredient) {
        return values().stream().filter(trimMaterial -> trimMaterial.ingredient().equals(ingredient)).findFirst().orElse(null);
    }

}
