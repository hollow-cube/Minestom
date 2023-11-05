package net.bytemc.minestom.server.display.head

import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.EntityCreature
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.other.ArmorStandMeta
import net.minestom.server.instance.Instance
import net.minestom.server.item.ItemStack
import net.minestom.server.utils.Direction

class HeadDisplay(private val value: String, val instance: Instance, val pos: Pos, val direction: Direction, val size: HeadSize = HeadSize.MID, private val space: Boolean = true) {
    private var entities: ArrayList<EntityCreature> = ArrayList()

    fun spawn() {
        for (i in value.indices) {
            var char = value[i].toString()
            if (char.isEmpty() || char.isBlank() || char == "") {
                if(!space) {
                    continue
                }
                char = "BLANK"
            }
            val head = StringHead.entries.find { it.name == char.uppercase() } ?: throw Exception("Head with the String $char does not exists!")
            val headStack = ItemStack.of(head.skinData)

            val entity = EntityCreature(EntityType.ARMOR_STAND)
            val meta = entity.entityMeta as ArmorStandMeta

            entity.setNoGravity(true)
            entity.isInvisible = true

            var distance = 0.0
            when (size) {
                HeadSize.BIG -> {
                    distance = i / 1.6
                    entity.helmet = headStack
                    meta.isSmall = false
                }

                HeadSize.MID -> {
                    distance = i / 2.3
                    entity.helmet = headStack
                    meta.isSmall = true
                }

                HeadSize.SMALL -> TODO("not implemented yet. Put in hand")
                HeadSize.VERY_SMALL -> TODO("not implemented yet. Put in hand and small")
            }

            var tempPos: Pos
            tempPos = pos.withYaw(direction.yaw)
            when (direction) {
                Direction.DOWN -> TODO()
                Direction.UP -> TODO()
                Direction.NORTH -> {
                    tempPos = tempPos.withX(tempPos.x - distance)
                }
                Direction.SOUTH -> {
                    tempPos = tempPos.withX(tempPos.x + distance)
                }
                Direction.WEST -> {
                    tempPos = tempPos.withZ(tempPos.z + distance)
                }
                Direction.EAST -> {
                    tempPos = tempPos.withZ(tempPos.z - distance)
                }
            }

            entity.setInstance(instance, tempPos).whenComplete { _, _ ->
                entity.spawn()
            }
            entities.add(entity)
        }
    }

    fun destroy() {
        for (entity in entities) {
            entity.remove()
        }
        entities.clear()
    }
}