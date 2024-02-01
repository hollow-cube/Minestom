package net.minestom.server.item.armor;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.Material;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Map;

public interface TrimMaterial extends ProtocolObject {
    @Contract(pure = true)
    @NotNull Registry.TrimMaterialEntry registry();

    @Override
    default @NotNull NamespaceID namespace() {
        return registry().namespace();
    }

    default @NotNull String assetName() {
        return registry().assetName();
    }

    default @NotNull Material ingredient() {
        return registry().ingredient();
    }

    default float itemModelIndex() {
        return registry().itemModelIndex();
    }

    default @NotNull Map<String,String> overrideArmorMaterials() {
        return registry().overrideArmorMaterials();
    }
    default @NotNull Component description() {
        return registry().description();
    }
    NBTCompound asNBT();
    static NBTCompound getNBT() {
        return TrimMaterialImpl.getNBT();
    }

    static @Nullable TrimMaterial fromIngredient(Material ingredient) {
        return TrimMaterialImpl.fromIngredient(ingredient);
    }

}
