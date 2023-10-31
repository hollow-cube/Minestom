package net.bytemc.minestom.server.fakeplayer

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
                it.teleport(pos)
            } else {
                it.setInstance(instance).whenComplete { _, _ ->
                    it.teleport(pos)
                }
            }

            it.isCustomNameVisible = false
            it.setNoGravity(true)
            it.isInvisible = true
            it.skin = skin

            fakePlayer = it
        }
    }

    fun destroy() {
        fakePlayer.remove()
    }
}