package net.minestom.server.instance;

import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * Used to register {@link Instance}.
 */
public interface InstanceManager {

    /**
     * Registers an {@link Instance} internally.
     * <p>
     * Note: not necessary if you created your instance using {@link #createInstanceContainer()} or {@link #createSharedInstance(InstanceContainer)}
     * but only if you instantiated your instance object manually
     *
     * @param instance the {@link Instance} to register
     */
    void registerInstance(@NotNull Instance instance);

    /**
     * Creates and register an {@link InstanceContainer} with the specified {@link DimensionType}.
     *
     * @param dimensionType the {@link DimensionType} of the instance
     * @param loader        the chunk loader
     * @return the created {@link InstanceContainer}
     */
    @ApiStatus.Experimental
    @NotNull InstanceContainer createInstanceContainer(@NotNull DimensionType dimensionType, @Nullable IChunkLoader loader);

    default @NotNull InstanceContainer createInstanceContainer(@NotNull DimensionType dimensionType) {
        return createInstanceContainer(dimensionType, null);
    }

    @ApiStatus.Experimental
    default @NotNull InstanceContainer createInstanceContainer(@Nullable IChunkLoader loader) {
        return createInstanceContainer(DimensionType.OVERWORLD, loader);
    }

    /**
     * Creates and register an {@link InstanceContainer}.
     *
     * @return the created {@link InstanceContainer}
     */
    default @NotNull InstanceContainer createInstanceContainer() {
        return createInstanceContainer(DimensionType.OVERWORLD, null);
    }

    /**
     * Registers a {@link SharedInstance}.
     * <p>
     * WARNING: the {@link SharedInstance} needs to have an {@link InstanceContainer} assigned to it.
     *
     * @param sharedInstance the {@link SharedInstance} to register
     * @return the registered {@link SharedInstance}
     * @throws NullPointerException if {@code sharedInstance} doesn't have an {@link InstanceContainer} assigned to it
     */
    @NotNull SharedInstance registerSharedInstance(@NotNull SharedInstance sharedInstance);

    /**
     * Creates and register a {@link SharedInstance}.
     *
     * @param instanceContainer the container assigned to the shared instance
     * @return the created {@link SharedInstance}
     * @throws IllegalStateException if {@code instanceContainer} is not registered
     */
    @NotNull SharedInstance createSharedInstance(@NotNull InstanceContainer instanceContainer);

    /**
     * Unregisters the {@link Instance} internally.
     * <p>
     * If {@code instance} is an {@link InstanceContainer} all chunks are unloaded.
     *
     * @param instance the {@link Instance} to unregister
     */
    void unregisterInstance(@NotNull Instance instance);

    /**
     * Gets all the registered instances.
     *
     * @return an unmodifiable {@link Set} containing all the registered instances
     */
    @NotNull Set<@NotNull Instance> getInstances();

    /**
     * Gets an instance by the given UUID.
     *
     * @param uuid UUID of the instance
     * @return the instance with the given UUID, null if not found
     */
    @Nullable Instance getInstance(@NotNull UUID uuid);
}
