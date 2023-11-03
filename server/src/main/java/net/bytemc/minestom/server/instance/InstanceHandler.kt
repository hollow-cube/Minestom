package net.bytemc.minestom.server.instance

import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.player.PlayerLoginEvent
import net.minestom.server.instance.AnvilLoader
import net.minestom.server.instance.block.Block
import net.minestom.server.world.DimensionType
import kotlin.collections.HashMap

class InstanceHandler {
    private var instances: HashMap<String, ByteInstance> = HashMap()
    private var spawnInstance: ByteInstance

    init {
        spawnInstance = create("Default", DimensionType.OVERWORLD)

        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent::class.java) { event ->
            event.setSpawningInstance(spawnInstance)
            event.player.respawnPoint = Pos(0.0, 42.0, 0.0)
        }
    }

    fun getSpawnInstance(): ByteInstance {
        return spawnInstance
    }

    fun setSpawnInstance(instance: ByteInstance) {
        spawnInstance = instance
    }

    fun create(name: String, type: DimensionType): ByteInstance {
        val byteInstance = ByteInstance(name, type)
        //TODO: Own generator
        byteInstance.setGenerator {
            it.modifier().fillHeight(0, 40, Block.GRASS_BLOCK)
        }

        MinecraftServer.getInstanceManager().registerInstance(byteInstance)
        return byteInstance
    }

    fun getOrNull(name: String): ByteInstance? {
        return instances[name]
    }

    fun register(name: String): ByteInstance {
        val byteInstance = ByteInstance(name, DimensionType.OVERWORLD, AnvilLoader("instances/${name}"))
        MinecraftServer.getInstanceManager().registerInstance(byteInstance)
        instances[name] = byteInstance
        return byteInstance
    }
}