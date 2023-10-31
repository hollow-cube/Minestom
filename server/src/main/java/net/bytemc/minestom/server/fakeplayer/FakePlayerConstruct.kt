package net.bytemc.minestom.server.fakeplayer

import net.kyori.adventure.text.Component
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.PlayerSkin
import net.minestom.server.entity.fakeplayer.FakePlayer
import net.minestom.server.entity.fakeplayer.FakePlayerOption
import net.minestom.server.instance.Instance
import java.util.*

class FakePlayerConstruct(id: String, skin: PlayerSkin, instance: Instance, pos: Pos) {
    private lateinit var fakePlayer: FakePlayer

    init {
        FakePlayer.initPlayer(UUID.randomUUID(), id, FakePlayerOption().setInTabList(false).setRegistered(false)) {
            if (instance == it.instance) {
                it.refreshPosition(pos)
            } else {
                it.setInstance(instance, pos).whenComplete { _, _ ->
                    it.teleport(pos)
                }
            }

            // update design
            val meta = it.entityMeta
            meta.isHatEnabled = true
            meta.isLeftSleeveEnabled = true
            meta.isRightSleeveEnabled = true
            meta.isCapeEnabled = true
            meta.isJacketEnabled = true
            meta.isLeftLegEnabled = true
            meta.isRightLegEnabled = true

            it.customName = Component.empty()
            it.isCustomNameVisible = false
            it.setNoGravity(true)
            it.isInvulnerable = true
            it.skin = skin

            fakePlayer = it
        }
    }

    fun destroy() {
        fakePlayer.remove()
    }
}