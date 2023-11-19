package net.bytemc.minestom.server.particle;

import net.bytemc.minestom.server.particle.impl.ColorParticle;
import net.bytemc.minestom.server.particle.impl.DefaultParticle;
import net.minestom.server.color.Color;
import net.minestom.server.particle.Particle;

public final class ParticleBuilder {

    public static ColorParticle color(Color color, int count, float speed) {
        return new ColorParticle(color, count, speed);
    }

    public static DefaultParticle particle(Particle particle, int count, float speed) {
        return new DefaultParticle(particle, count, speed);
    }
}
