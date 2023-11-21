package net.bytemc.minestom.server.particle;

import net.minestom.server.instance.Instance;

public class PermanentParticleCurveQueue extends AbstractParticleQueue {
    private final Curve curve;
    private final double spacing;

    public PermanentParticleCurveQueue(ParticleBuilder builder, int updateSpeed, double spacing, Instance instance, Curve curve) {
        super(builder, updateSpeed, instance);
        this.curve = curve;
        this.spacing = spacing;
    }

    @Override
    public void tick() {
        curve.reset();
        while (true) {
            var point = curve.getNextPoint(spacing, false);
            if (point == null) {
                return;
            }
            sendParticle(point);
        }
    }
}
