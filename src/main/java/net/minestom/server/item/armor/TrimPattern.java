package net.minestom.server.item.armor;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.Material;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public interface TrimPattern extends ProtocolObject {
    @Contract(pure = true)
    @NotNull Registry.TrimPatternEntry registry();

    @Override
    default @NotNull NamespaceID namespace() {
        return registry().namespace();
    }

    default @NotNull NamespaceID assetID() {
        return registry().assetID();
    }

    default @NotNull Material template() {
        return registry().template();
    }

    default @NotNull Component description() {
        return registry().description();
    }

    default boolean decal() {
        return registry().decal();
    }

    NBTCompound asNBT();
    static NBTCompound getNBT() {
        return TrimPatternImpl.getNBT();
    }

    static TrimPattern fromTemplate(Material material) {
        return TrimPatternImpl.fromTemplate(material);
    }

}
