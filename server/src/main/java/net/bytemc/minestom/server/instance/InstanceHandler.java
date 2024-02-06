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
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InstanceHandler {
    private final Map<String, InstanceContainer> instances;
    private InstanceContainer spawnInstance;

    public InstanceHandler() {
        this.instances = new ConcurrentHashMap<>();
        this.spawnInstance = this.create("Default", Generators.FLAT_GENERATOR);

        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, event ->
                event.setSpawningInstance(spawnInstance));
    }

    public InstanceContainer getSpawnInstance() {
        return spawnInstance;
    }

    public void setSpawnInstance(InstanceContainer spawnInstance) {
        this.spawnInstance = spawnInstance;
    }

    public InstanceContainer create(String name, Generator generator) {
        return this.create(name, generator, 9);
    }

    public InstanceContainer create(String name, Generator generator, NamespaceID namespaceID) {
        return this.create(name, generator, namespaceID, 9);
    }

    public InstanceContainer create(String name, Generator generator, int chunkLoadDistance) {
        return this.create(name, generator, DimensionType.OVERWORLD.getName(), chunkLoadDistance);
    }

    public InstanceContainer create(String name, Generator generator, NamespaceID namespaceID, int chunkLoadDistance) {
        var byteInstance = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD, namespaceID);
        instances.put(name, byteInstance);
        byteInstance.setGenerator(generator);
        MinecraftServer.getInstanceManager().registerInstance(byteInstance);

        for (int x = -chunkLoadDistance; x <= chunkLoadDistance; x++) {
            for (int y = -chunkLoadDistance; y <= chunkLoadDistance; y++) {
                byteInstance.loadChunk(x, y);
            }
        }

        return byteInstance;
    }

    public InstanceContainer load(String name) {
        return this.load(name, 9);
    }

    public InstanceContainer load(String name, NamespaceID namespaceID) {
        return this.load(name, namespaceID, 9);
    }

    public InstanceContainer load(String name, int chunkLoadDistance) {
        return this.load(name, DimensionType.OVERWORLD.getName(), chunkLoadDistance);
    }

    public InstanceContainer load(String name, NamespaceID namespaceID, int chunkLoadDistance) {
        var byteInstance = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD, namespaceID);
        byteInstance.setChunkLoader(new AnvilLoader("./" + name));
        instances.put(name, byteInstance);
        MinecraftServer.getInstanceManager().registerInstance(byteInstance);

        for (int x = -chunkLoadDistance; x <= chunkLoadDistance; x++) {
            for (int y = -chunkLoadDistance; y <= chunkLoadDistance; y++) {
                byteInstance.loadChunk(x, y);
            }
        }

        return byteInstance;
    }

    public void unregister(String name) {
        MinecraftServer.getInstanceManager().unregisterInstance(getInstanceFromName(name));
        instances.remove(name);
    }

    public void unregister(Instance instance) {
        MinecraftServer.getInstanceManager().unregisterInstance(instance);
        instances.remove(getNameFromInstance(instance));
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
