package net.bytemc.minestom.server.particle.impl;

import net.bytemc.minestom.server.particle.AbstractParticle;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;

public final class DefaultParticle extends AbstractParticle {

    public DefaultParticle(Particle particle, int count, float speed) {
        super(particle, count, speed);
    }

    @Override
    public ParticlePacket toPacket(Pos pos) {
        var offset = getOffset();
        return ParticleCreator.createParticlePacket(getParticle(), false, pos.x(), pos.y(), pos.z(), offset[0], offset[1], offset[2], getSpeed(), getCount(), null);
    }
}
