package net.bytemc.minestom.server.hologram

import net.kyori.adventure.text.Component
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.EntityCreature
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.other.ArmorStandMeta
import net.minestom.server.instance.Instance

class Hologram(var instance: Instance, var spawn: Point, var lines: List<String>) {
    private var entities: ArrayList<EntityCreature> = ArrayList()

    fun spawn() {
        for (line in lines) {
            val hologram = EntityCreature(EntityType.ARMOR_STAND)
            val meta = hologram.entityMeta as ArmorStandMeta

            hologram.setNoGravity(true)
            hologram.isInvisible = true
            meta.isHasNoBasePlate = true
            meta.isSmall = true
            meta.isMarker = true

            hologram.isCustomNameVisible = true
            hologram.customName = Component.text(line)

            hologram.setInstance(instance).whenComplete { _, _ ->
                hologram.spawn()
            }
            
            entities.add(hologram)
        }
    }
    
    fun destroy() {
        for (entity in entities) {
            entity.remove()
        }
        entities.clear()
    }
}