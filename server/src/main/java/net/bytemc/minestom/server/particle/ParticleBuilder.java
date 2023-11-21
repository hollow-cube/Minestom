package net.bytemc.minestom.server.particle;

import net.minestom.server.coordinate.Point;
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

    public ParticleBuilder(float r, float g, float b) {
        this(r, g, b, 1, 0);
    }

    public ParticleBuilder(float r, float g, float b, int count, float speed) {
        this(Particle.DUST, count, speed);
        float scale = 0.8f;
        var data = ByteBuffer.allocate(16);
        data.putFloat(r / 255f);
        data.putFloat(g / 255f);
        data.putFloat(b / 255f);
        data.putFloat(scale);
        this.data = data.array();
    }

    public ParticleBuilder(Particle particle) {
        this(particle, 1, 0);
    }

    public ParticleBuilder(Particle particle, int count, float speed) {
        this.particle = particle;
        this.count = count;
        this.speed = speed;
    }

    public void send(Player player, Point point) {
        player.sendPacket(toPacket(point));
    }

    public ParticlePacket toPacket(Point point) {
        return new ParticlePacket(particle.id(), false, point.x(), point.y(), point.z(), offset[0], offset[1], offset[2], speed, count, data);
    }
}