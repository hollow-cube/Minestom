package net.bytemc.minestom.server.instance

import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.player.PlayerLoginEvent
import net.minestom.server.instance.AnvilLoader
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.generator.Generator
import net.minestom.server.instance.generator.Generators
import net.minestom.server.world.DimensionType
import java.util.*
import kotlin.collections.HashMap

class InstanceHandler {
    private var instances: HashMap<String, InstanceContainer> = HashMap()
    private var spawnInstance: InstanceContainer

    init {
        spawnInstance = create("Default", Generators.FLAT_GENERATOR)

        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent::class.java) { event ->
            event.setSpawningInstance(spawnInstance)
            event.player.respawnPoint = Pos(0.0, 42.0, 0.0)
        }
    }

    fun getSpawnInstance(): InstanceContainer {
        return spawnInstance
    }

    fun setSpawnInstance(instance: InstanceContainer) {
        spawnInstance = instance
    }

    fun create(name: String, generator: Generator): InstanceContainer {
        val byteInstance = InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD)
        byteInstance.setGenerator(generator)
        MinecraftServer.getInstanceManager().registerInstance(byteInstance)
        instances[name] = byteInstance
        return byteInstance
    }

    fun getOrNull(name: String): InstanceContainer? {
        return instances[name]
    }

    fun register(name: String): InstanceContainer {
        val byteInstance = InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD, AnvilLoader("instances/${name}"))
        MinecraftServer.getInstanceManager().registerInstance(byteInstance)
        instances[name] = byteInstance
        return byteInstance
    }
}