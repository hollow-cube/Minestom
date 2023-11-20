package net.bytemc.minestom.server.particle.impl;

import net.bytemc.minestom.server.particle.AbstractParticleQueue;
import net.bytemc.minestom.server.particle.ParticleBuilder;
import net.minestom.server.coordinate.Pos;

public final class ParticleCycleQueue extends AbstractParticleQueue {
    private final Pos pos;
    private final float radius;

    public ParticleCycleQueue(ParticleBuilder builder, Pos pos, float radius) {
        super(builder);

        this.pos = pos;
        this.radius = radius;
    }

    @Override
    public void tick() {
        setCount(getCount() + 1);
        if (getCount() <= 90) {
            setCount(1);
        }

        var newPos = pos.add(Math.cos(getCount()) * radius, 0, Math.sin(getCount()) * radius);
        getPlayers().forEach(player -> getBuilder().send(player, newPos));
    }
}
