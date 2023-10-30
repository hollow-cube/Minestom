package net.bytemc.minestom.server.instance

import net.minestom.server.instance.IChunkLoader
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.world.DimensionType
import java.util.*

class ByteInstance(var name: String, dimensionType: DimensionType, chunkLoader: IChunkLoader) : InstanceContainer(UUID.randomUUID(), dimensionType, chunkLoader) {

    fun save() {
        TODO("Not implemented yet.")
    }

    fun setTime() {
        TODO("Not implemented yet.")
    }

    fun setWeather() {
        TODO("Not implemented yet.")
    }
}