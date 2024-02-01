package net.minestom.server.world;

import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTType;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DimensionTypeManagerImpl implements DimensionTypeManager {

    private final List<DimensionType> dimensionTypes = new CopyOnWriteArrayList<>();

    public DimensionTypeManagerImpl() {
        addDimension(DimensionType.OVERWORLD);
    }

    @Override
    public void addDimension(@NotNull DimensionType dimensionType) {
        dimensionType.registered = true;
        this.dimensionTypes.add(dimensionType);
    }

    @Override
    public boolean removeDimension(@NotNull DimensionType dimensionType) {
        dimensionType.registered = false;
        return dimensionTypes.remove(dimensionType);
    }

    @Override
    public boolean isRegistered(@Nullable DimensionType dimensionType) {
        return dimensionType != null && dimensionTypes.contains(dimensionType) && dimensionType.isRegistered();
    }

    @Override
    public @Nullable DimensionType getDimension(@NotNull NamespaceID namespaceID) {
        return unmodifiableList().stream().filter(dimensionType -> dimensionType.getName().equals(namespaceID)).filter(DimensionType::isRegistered).findFirst().orElse(null);
    }

    @Override
    public @NotNull List<DimensionType> unmodifiableList() {
        return Collections.unmodifiableList(dimensionTypes);
    }

    @Override
    public @NotNull NBTCompound toNBT() {
        return NBT.Compound(dimensions -> {
            dimensions.setString("type", "minecraft:dimension_type");
            dimensions.set("value", NBT.List(
                    NBTType.TAG_Compound,
                    dimensionTypes.stream()
                            .map(DimensionType::toIndexedNBT)
                            .toList()
            ));
        });
    }
}
