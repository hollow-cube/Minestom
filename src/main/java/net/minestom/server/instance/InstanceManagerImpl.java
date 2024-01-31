package net.minestom.server.instance;

import lombok.RequiredArgsConstructor;
import net.minestom.server.ServerSettings;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.InstanceRegisterEvent;
import net.minestom.server.event.instance.InstanceUnregisterEvent;
import net.minestom.server.exception.ExceptionHandler;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.thread.ThreadDispatcher;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biomes.BiomeManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

@RequiredArgsConstructor
public final class InstanceManagerImpl implements InstanceManager {
    private final ThreadDispatcher<Chunk> dispatcher;
    private final EventNode<Event> globalEventHandler;
    private final ServerSettings serverSettings;
    private final ExceptionHandler exceptionHandler;
    private final BlockManager blockManager;
    private final BiomeManager biomeManager;

    private final Set<Instance> instances = new CopyOnWriteArraySet<>();

    @Override
    public void registerInstance(@NotNull Instance instance) {
        Check.stateCondition(instance instanceof SharedInstance, "Please use InstanceManager#registerSharedInstance to register a shared instance");
        UNSAFE_registerInstance(instance);
    }

    @Override
    @ApiStatus.Experimental
    public @NotNull InstanceContainer createInstanceContainer(@NotNull DimensionType dimensionType, @Nullable IChunkLoader loader) {
        final InstanceContainer instanceContainer = new InstanceContainer(exceptionHandler, blockManager, biomeManager, serverSettings, globalEventHandler, dispatcher, UUID.randomUUID(), dimensionType, loader);
        registerInstance(instanceContainer);
        return instanceContainer;
    }

    @Override
    public @NotNull SharedInstance registerSharedInstance(@NotNull SharedInstance sharedInstance) {
        final InstanceContainer instanceContainer = sharedInstance.getInstanceContainer();
        Check.notNull(instanceContainer, "SharedInstance needs to have an InstanceContainer to be created!");

        instanceContainer.addSharedInstance(sharedInstance);
        UNSAFE_registerInstance(sharedInstance);
        return sharedInstance;
    }

    @Override
    public @NotNull SharedInstance createSharedInstance(@NotNull InstanceContainer instanceContainer) {
        Check.notNull(instanceContainer, "Instance container cannot be null when creating a SharedInstance!");
        Check.stateCondition(!instanceContainer.isRegistered(), "The container needs to be register in the InstanceManager");

        final SharedInstance sharedInstance = new SharedInstance(serverSettings, globalEventHandler, UUID.randomUUID(), instanceContainer);
        return registerSharedInstance(sharedInstance);
    }

    @Override
    public void unregisterInstance(@NotNull Instance instance) {
        Check.stateCondition(!instance.getPlayers().isEmpty(), "You cannot unregister an instance with players inside.");
        synchronized (instance) {
            InstanceUnregisterEvent event = new InstanceUnregisterEvent(instance);
            globalEventHandler.call(event);

            // Unload all chunks
            if (instance instanceof InstanceContainer) {
                instance.getChunks().forEach(instance::unloadChunk);
                instance.getChunks().forEach(dispatcher::deletePartition);
            }
            // Unregister
            instance.setRegistered(false);
            this.instances.remove(instance);
        }
    }

    @Override
    public @NotNull Set<@NotNull Instance> getInstances() {
        return Collections.unmodifiableSet(instances);
    }

    @Override
    public @Nullable Instance getInstance(@NotNull UUID uuid) {
        Optional<Instance> instance = getInstances()
                .stream()
                .filter(someInstance -> someInstance.getUniqueId().equals(uuid))
                .findFirst();
        return instance.orElse(null);
    }

    /**
     * Registers an {@link Instance} internally.
     * <p>
     * Unsafe because it does not check if {@code instance} is a {@link SharedInstance} to verify its container.
     *
     * @param instance the {@link Instance} to register
     */
    private void UNSAFE_registerInstance(@NotNull Instance instance) {
        instance.setRegistered(true);
        this.instances.add(instance);
        instance.getChunks().forEach(dispatcher::createPartition);
        InstanceRegisterEvent event = new InstanceRegisterEvent(instance);
        globalEventHandler.call(event);
    }
}
