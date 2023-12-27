package net.bytemc.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.instance.generator.Generators;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InstanceHandler {
    private final Map<String, InstanceContainer> instances;
    private InstanceContainer spawnInstance;

    public InstanceHandler() {
        this.instances = new ConcurrentHashMap<>();
        this.spawnInstance = create("Default", Generators.FLAT_GENERATOR);

        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent.class, event -> {
            event.setSpawningInstance(spawnInstance);
        });
    }

    public InstanceContainer getSpawnInstance() {
        return spawnInstance;
    }

    public void setSpawnInstance(InstanceContainer spawnInstance) {
        this.spawnInstance = spawnInstance;
    }

    public InstanceContainer create(String name, Generator generator) {
        var byteInstance = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD);
        byteInstance.setGenerator(generator);
        MinecraftServer.getInstanceManager().registerInstance(byteInstance);
        instances.put(name, byteInstance);
        return byteInstance;
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

    public InstanceContainer getOrNull(String name) {
        return instances.get(name);
    }

    public InstanceContainer getOrThrow(String name) {
        var instance = getOrNull(name);
        if(instance == null) {
            throw new RuntimeException("Instance '" + name + "' is null!");
        }
        return instance;
    }
}
