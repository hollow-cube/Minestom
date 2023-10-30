package net.bytemc.minestom.server.instance

import net.minestom.server.MinecraftServer
import net.minestom.server.instance.AnvilLoader
import net.minestom.server.world.DimensionType
import kotlin.collections.HashMap

class InstanceHandler {
    private var instances: HashMap<String, ByteInstance> = HashMap()

    fun create() {
        TODO("Not implemented yet.")
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