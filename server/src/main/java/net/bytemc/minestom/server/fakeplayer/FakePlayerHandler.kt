package net.bytemc.minestom.server.fakeplayer

import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.PlayerSkin
import net.minestom.server.instance.Instance

class FakePlayerHandler {
    private val fakePlayers: HashMap<String, FakePlayerConstruct> = HashMap()

    fun register(id: String, skin: PlayerSkin, instance: Instance, pos: Pos) {
        val fakePlayer = FakePlayerConstruct(id, skin, instance, pos)
        fakePlayers[id] = fakePlayer
    }

    fun destroy(id: String) {
        fakePlayers[id]?.destroy()
        fakePlayers.remove(id)
    }
}