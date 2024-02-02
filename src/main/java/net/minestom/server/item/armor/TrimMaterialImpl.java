package net.minestom.server.item.armor;

import net.minestom.server.adventure.serializer.nbt.NbtComponentSerializer;
import net.minestom.server.item.Material;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

record TrimMaterialImpl(Registry.TrimMaterialEntry registry, int id) implements TrimMaterial {
    private static final Registry.Container<TrimMaterial> CONTAINER;
    static final AtomicInteger i = new AtomicInteger();
    static {
        CONTAINER = Registry.createContainer(Registry.Resource.TRIM_MATERIALS,
                (namespace, properties) -> new TrimMaterialImpl(Registry.trimMaterial(namespace, properties)));
    }

    public TrimMaterialImpl(Registry.TrimMaterialEntry registry) {
        this(registry, i.getAndIncrement());
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
                                    entry -> NBT.String(entry.getValue())
                            ))
            ));
            nbt.set("description", NbtComponentSerializer.nbt().serialize(description()));
        });
    }

    public static @Nullable TrimMaterial fromIngredient(Material ingredient) {
        return values().stream().filter(trimMaterial -> trimMaterial.ingredient().equals(ingredient)).findFirst().orElse(null);
    }

}
