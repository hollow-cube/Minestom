package net.bytemc.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.instance.generator.Generators;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class InstanceHandler {
    private final Map<String, InstanceContainer> instances;
    private InstanceContainer spawnInstance;

    public InstanceHandler() {
        this.instances = new ConcurrentHashMap<>();

        this.create("Default", Generators.FLAT_GENERATOR).thenAccept(instanceContainer -> {
            this.spawnInstance = instanceContainer;
        });

        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, event ->
                event.setSpawningInstance(spawnInstance));
    }

    public InstanceContainer getSpawnInstance() {
        return spawnInstance;
    }

    public void setSpawnInstance(InstanceContainer spawnInstance) {
        this.spawnInstance = spawnInstance;
    }

    public CompletableFuture<InstanceContainer> create(String name, Generator generator) {
        return this.create(name, generator, 9);
    }

    public CompletableFuture<InstanceContainer> create(String name, Generator generator, int chunkLoadDistance) {
        var byteInstance = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD);
        byteInstance.setGenerator(generator);
        MinecraftServer.getInstanceManager().registerInstance(byteInstance);
        instances.put(name, byteInstance);

        CompletableFuture<InstanceContainer> future = new CompletableFuture<>();

        for (int x = -chunkLoadDistance; x <= chunkLoadDistance; x++) {
            for (int y = -chunkLoadDistance; y <= chunkLoadDistance; y++) {
                byteInstance.loadChunk(x, y);
            }
        }

        return future;
    }

    public CompletableFuture<InstanceContainer> load(String name) {
        return this.load(name, 9);
    }

    public CompletableFuture<InstanceContainer> load(String name, int chunkLoadDistance) {
        var byteInstance = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD);
        byteInstance.setChunkLoader(new AnvilLoader("./" + name));
        MinecraftServer.getInstanceManager().registerInstance(byteInstance);
        instances.put(name, byteInstance);

        CompletableFuture<InstanceContainer> future = new CompletableFuture<>();

        for (int x = -chunkLoadDistance; x <= chunkLoadDistance; x++) {
            for (int y = -chunkLoadDistance; y <= chunkLoadDistance; y++) {
                byteInstance.loadChunk(x, y);
            }
        }

        return future;
    }

    public void unregister(String name) {
        instances.remove(name);
        MinecraftServer.getInstanceManager().unregisterInstance(getInstanceFromName(name));
    }

    public void unregister(Instance instance) {
        instances.remove(getNameFromInstance(instance));
        MinecraftServer.getInstanceManager().unregisterInstance(instance);
    }

    @Nullable
    public String getNameFromInstance(Instance instance) {
        return this.getNameFromUUID(instance.getUniqueId());
    }

    @Nullable
    public String getNameFromUUID(UUID uuid) {
        return this.instances.entrySet().stream()
                .filter(entry -> entry.getValue().getUniqueId().equals(uuid))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public InstanceContainer getInstanceFromName(String name) {
        return instances.get(name);
    }

    public InstanceContainer getInstanceFromUuid(UUID uuid) {
        return this.getInstanceFromName(this.getNameFromUUID(uuid));
    }

    public InstanceContainer getOrThrow(String name) {
        var instance = getInstanceFromName(name);
        if(instance == null) {
            throw new RuntimeException("Instance '" + name + "' is null!");
        }
        return instance;
    }
}
