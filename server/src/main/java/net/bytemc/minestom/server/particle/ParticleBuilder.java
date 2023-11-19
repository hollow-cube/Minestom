package net.bytemc.minestom.server.particle;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;

import java.nio.ByteBuffer;

public class ParticleBuilder {
    private final Particle particle;

    private final int count;
    private final float speed;

    private byte[] data = new byte[0];
    private final float[] offset = new float[] { 0, 0, 0 };

    public ParticleBuilder(float r, float g, float b, int count, float speed) {
        this(Particle.DUST, count, speed);

        float scale = 0.8f;
        var data = ByteBuffer.allocate(16);
        data.putFloat(r);
        data.putFloat(g);
        data.putFloat(b);
        data.putFloat(scale);
        this.data = data.array();
    }

    public ParticleBuilder(Particle particle, int count, float speed) {
        this.particle = particle;
        this.count = count;
        this.speed = speed;
    }

    public void circle(Player player, Pos pos, float radius) {
        for (int d = 0; d <= 90; d += 1) {
            var newPos = pos.add(Math.cos(d) * radius, 0, Math.sin(d) * radius);
            send(player, newPos);
        }
    }

    public void send(Player player, Pos pos) {
        player.sendPacket(toPacket(pos));
    }

    public ParticlePacket toPacket(Pos pos) {
        return new ParticlePacket(particle.id(), false, pos.x(), pos.y(), pos.z(), offset[0], offset[1], offset[2], speed, count, data);
    }
}
