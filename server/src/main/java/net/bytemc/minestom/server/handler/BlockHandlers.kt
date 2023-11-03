package net.bytemc.minestom.server.handler

import net.minestom.server.tag.Tag

import net.minestom.server.instance.block.BlockHandler
import net.minestom.server.utils.NamespaceID


object BlockHandlers {

    val BANNER_HELPER: BlockHandler = object : BlockHandler {
        override fun getNamespaceId(): NamespaceID {
            return NamespaceID.from("minecraft:banner")
        }
        override fun getBlockEntityTags(): Collection<Tag<*>> {
            return listOf(Tag.NBT("Patterns"))
        }
    }

    val SKULL_HANDLER: BlockHandler = object : BlockHandler {
        override fun getNamespaceId(): NamespaceID {
            return NamespaceID.from("minecraft:skull")
        }
        override fun getBlockEntityTags(): Collection<Tag<*>> {
            return listOf(Tag.NBT("SkullOwner"))
        }
    }
}