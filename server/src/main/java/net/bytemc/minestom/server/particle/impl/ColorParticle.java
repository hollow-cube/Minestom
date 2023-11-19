package net.bytemc.minestom.server.particle.impl;

import net.bytemc.minestom.server.particle.AbstractParticle;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;

import java.nio.ByteBuffer;

public final class ColorParticle extends AbstractParticle {
    private final Color color;

    public ColorParticle(Color color, int count, float speed) {
        super(Particle.DUST, count, speed);

        this.color = color;
    }

    @Override
    public ParticlePacket toPacket(Pos pos) {
        var offset = getOffset();
        float r = color.red() / 255f;
        float g = color.green() / 255f;
        float b = color.blue() / 255f;
        float scale = 0.8f;

        var buffer = ByteBuffer.allocate(16);
        buffer.putFloat(r);
        buffer.putFloat(g);
        buffer.putFloat(b);
        buffer.putFloat(scale);

        return new ParticlePacket(getParticle().id(), false, pos.x(), pos.y(), pos.z(), offset[0], offset[1], offset[2], getSpeed(), getCount(), buffer.array());
    }
}
