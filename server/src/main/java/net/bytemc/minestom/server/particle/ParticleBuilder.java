package net.bytemc.minestom.server.particle;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;

@SuppressWarnings("unused")
public final class ParticleBuilder {
    private final Particle particle;
    private final int count;

    private final float speed;
    private final float[] offset = new float[] {0, 0, 0};

    public ParticleBuilder(Particle particle, int count, float speed) {
        this.particle = particle;
        this.count = count;
        this.speed = speed;
    }

    public ParticleBuilder offset(float x, float y, float z) {
        this.offset[0] = x;
        this.offset[1] = y;
        this.offset[2] = z;

        return this;
    }

    public void showCircle(Player player, Pos pos, float radius) {
        for (int d = 0; d <= 90; d += 1) {
            var newPos = pos.add(Math.cos(d) * radius, 0, Math.sin(d) * radius);
            show(player, newPos);
        }
    }

    public static ParticleBuilder build(Particle particle, int count) {
        return build(particle, count, 1f);
    }

    public static ParticleBuilder build(Particle particle, int count, float speed) {
        return new ParticleBuilder(particle, count, speed);
    }

    public void show(Player player, Pos pos) {
        player.sendPacket(toPacket(pos));
    }

    public ParticlePacket toPacket(Pos pos) {
        return ParticleCreator.createParticlePacket(particle, false, pos.x(), pos.y(), pos.z(), offset[0], offset[1], offset[2], speed, count, null);
    }

    public Particle getParticle() {
        return particle;
    }

    public Pos getPos() {
        return pos;
    }

    public int getCount() {
        return count;
    }

    public float[] getOffset() {
        return offset;
    }

    public float getSpeed() {
        return speed;
    }
}
