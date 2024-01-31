package net.minestom.server.world;

import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.List;

/**
 * Allows servers to register custom dimensions. Also used during player login to send the list of all existing dimensions.
 * <p>
 * Contains {@link DimensionType#OVERWORLD} by default but can be removed.
 */
public interface DimensionTypeManager {

    /**
     * Adds a new dimension type. This does NOT send the new list to players.
     *
     * @param dimensionType the dimension to add
     */
    void addDimension(@NotNull DimensionType dimensionType);

    /**
     * Removes a dimension type. This does NOT send the new list to players.
     *
     * @param dimensionType the dimension to remove
     * @return if the dimension type was removed, false if it was not present before
     */
    boolean removeDimension(@NotNull DimensionType dimensionType);

    /**
     * @param dimensionType dimension to check if is registered
     * @return true if the dimension is registered
     */
    boolean isRegistered(@Nullable DimensionType dimensionType);

    /**
     * @param namespaceID The dimension name
     * @return true if the dimension is registered
     */
    default boolean isRegistered(@NotNull NamespaceID namespaceID) {
        return isRegistered(getDimension(namespaceID));
    }

    /**
     * Return to a @{@link DimensionType} only if present and registered
     *
     * @param namespaceID The Dimension Name
     * @return a DimensionType if it is present and registered
     */
    @Nullable DimensionType getDimension(@NotNull NamespaceID namespaceID);

    /**
     * Returns an immutable copy of the dimension types already registered.
     *
     * @return an unmodifiable {@link List} containing all the added dimensions
     */
    @NotNull List<DimensionType> unmodifiableList();

    /**
     * Creates the {@link NBTCompound} containing all the registered dimensions.
     * <p>
     * Used when a player connects.
     *
     * @return an nbt compound containing the registered dimensions
     */
    @NotNull NBTCompound toNBT();
}
