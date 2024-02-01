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

record TrimPatternImpl(Registry.TrimPatternEntry registry, int id) implements TrimPattern {
    private static final Registry.Container<TrimPattern> CONTAINER;

    static {
        AtomicInteger i = new AtomicInteger();
        CONTAINER = Registry.createContainer(Registry.Resource.TRIM_PATTERNS,
                (namespace, properties) -> new TrimPatternImpl(Registry.trimPattern(namespace, properties), i.getAndIncrement()));
    }
    public static TrimPattern get(String namespace) {
        return CONTAINER.get(namespace);
    }

    static Collection<TrimPattern> values() {
        return CONTAINER.values();
    }

    public NBTCompound asNBT() {
        return NBT.Compound(nbt -> {
           nbt.setString("asset_id",assetID().asString());
           nbt.setString("template_item",template().namespace().asString());
           nbt.set("description", NbtComponentSerializer.nbt().serialize(description()));
           nbt.setByte("decal", (byte) (decal() ? 1 : 0));
        });
    }
    private static NBTCompound lazyNbt = null;

    static NBTCompound getNBT() {
        if (lazyNbt == null) {
            var trimPatterns = values().stream()
                    .map((trimPattern) -> NBT.Compound(Map.of(
                            "id", NBT.Int(trimPattern.id()),
                            "name", NBT.String(trimPattern.name()),
                            "element", trimPattern.asNBT()
                    )))
                    .toList();

            lazyNbt = NBT.Compound(Map.of(
                    "type", NBT.String("minecraft:trim_pattern"),
                    "value", NBT.List(NBTType.TAG_Compound, trimPatterns)
            ));
        }
        return lazyNbt;
    }

    public static @Nullable TrimPattern fromTemplate(Material material) {
        return values().stream().filter(trimPattern -> trimPattern.template().equals(material)).findFirst().orElse(null);
    }

}
