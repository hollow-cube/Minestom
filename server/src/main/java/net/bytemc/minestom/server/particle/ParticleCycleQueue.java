package net.bytemc.minestom.server.particle;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

public final class ParticleCycleQueue extends AbstractParticleQueue {
    private final Pos position;
    private final int radius;

    private int max = 0;

    public ParticleCycleQueue(ParticleBuilder builder, int speed, Pos pos, Instance instance, int radius) {
        super(builder, speed, instance);
        this.position = pos;
        this.radius = radius;
    }

    @Override
    public void tick() {
        if (max == (90 * (radius * 2))) {
            max = 0;
        }
        max++;
        double particleData = (double) max / (radius * 3);
        var pos = new Pos(position.x() + Math.cos(particleData) * radius, 0, position.z() + Math.sin(particleData) * radius);
        getInstance().getPlayers().forEach(player -> getBuilder().send(player, pos));
    }
}
