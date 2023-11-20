package net.bytemc.minestom.server.particle.impl;

import net.bytemc.minestom.server.particle.AbstractParticleQueue;
import net.bytemc.minestom.server.particle.ParticleBuilder;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;

import java.util.ArrayList;
import java.util.List;

public final class LineParticleQueue extends AbstractParticleQueue {
    private final List<Pos> lines;

    private int vecState = 0;
    private Vec vec;

    public LineParticleQueue(ParticleBuilder builder, Pos... lines) {
        super(builder);

        this.lines = new ArrayList<>(List.of(lines));
        this.vec = getVec(0);
    }

    private Vec getVec(int value) {
        if((value + 1) == lines.size()) {
            return this.lines.get(value).asVec().sub(this.lines.get(0).asVec()).normalize();
        }
        return this.lines.get(value).asVec().sub(this.lines.get(value + 1).asVec()).normalize();
    }

    @Override
    public void tick() {
        this.vecState += 1;
        if(this.vecState >= lines.size()) {
            this.vecState = 0;
        }
        this.vec = getVec(vecState);
    }
}
