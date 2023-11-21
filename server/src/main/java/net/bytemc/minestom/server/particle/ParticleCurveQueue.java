package net.bytemc.minestom.server.particle;

import net.minestom.server.instance.Instance;

public class ParticleCurveQueue extends AbstractParticleQueue {
    private final Curve curve;
    private final double speed;
    private final double deltaTime;

    public ParticleCurveQueue(ParticleBuilder builder, int updateSpeed, double speed, Instance instance, Curve curve) {
        super(builder, updateSpeed, instance);
        this.curve = curve;
        this.speed = speed;
        deltaTime = updateSpeed / 1000.;
    }

    @Override
    public void tick() {
        var point = curve.getNextPoint(speed * deltaTime);
        sendParticle(point);
    }
}
