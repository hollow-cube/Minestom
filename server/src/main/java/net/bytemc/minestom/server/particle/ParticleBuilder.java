package net.bytemc.minestom.server.particle;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import java.nio.ByteBuffer;

public final class ParticleBuilder {
    private final Particle particle;

    private final int count;
    private final float speed;

    private byte[] data = new byte[0];

    private float offsetX = 0;
    private float offsetY = 0;
    private float offsetZ = 0;

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

    public ParticleBuilder withOffSet(float x, float y, float z) {
        offsetX = x;
        offsetY = y;
        offsetZ = z;
        return this;
    }

    public ParticlePacket toPacket(Point point) {
        return new ParticlePacket(particle.id(), false, point.x(), point.y(), point.z(), offsetX, offsetY, offsetZ, speed, count, data);
    }
}