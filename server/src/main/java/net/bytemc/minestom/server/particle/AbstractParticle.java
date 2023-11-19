package net.bytemc.minestom.server.particle;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;

public abstract class AbstractParticle {
    private final Particle particle;

    private final int count;
    private final float speed;

    private final float[] offset = new float[] { 0, 0, 0 };

    public AbstractParticle(Particle particle, int count, float speed) {
        this.particle = particle;
        this.count = count;
        this.speed = speed;
    }

    public void offset(float x, float y, float z) {
        this.offset[0] = x;
        this.offset[1] = y;
        this.offset[2] = z;
    }

    public Particle getParticle() {
        return particle;
    }

    public int getCount() {
        return count;
    }

    public float getSpeed() {
        return speed;
    }

    public float[] getOffset() {
        return offset;
    }

    public void showCircle(Player player, Pos pos, float radius) {
        for (int d = 0; d <= 90; d += 1) {
            var newPos = pos.add(Math.cos(d) * radius, 0, Math.sin(d) * radius);
            showPlayer(player, newPos);
        }
    }

    public void showPlayer(Player player, Pos pos) {
        player.sendPacket(toPacket(pos));
    }

    public abstract ParticlePacket toPacket(Pos pos);
}
