package net.bytemc.minestom.server.particle;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;

public class ParticleLineQueue extends AbstractParticleQueue {
    private final Point[] vertices;
    private final double[] lengths;
    private double currentPosition;
    private int currentEdge;
    private final double speed;
    private final int updateSpeed;

    public ParticleLineQueue(ParticleBuilder builder, int updateSpeed, double speed, Instance instance, Point[] vertices) {
        super(builder, updateSpeed, instance);
        this.vertices = vertices.clone();
        lengths = new double[vertices.length - 1];
        for (int i = 0; i < lengths.length; i++) {
            lengths[i] = vertices[i].distance(vertices[i + 1]);
        }
        currentEdge = 0;
        currentPosition = 0;
        this.speed = speed;
        this.updateSpeed = updateSpeed;
    }

    @Override
    public void tick() {
        currentPosition += speed * updateSpeed / 1000.;
        while (currentPosition >= lengths[currentEdge]) {
            currentPosition -= lengths[currentEdge];
            currentEdge++;
            if (currentEdge >= lengths.length) {
                currentEdge = 0;
            }
        }
        var edge = vertices[currentEdge + 1].sub(vertices[currentEdge]);
        var pos = vertices[currentEdge].add(edge.mul(currentPosition / lengths[currentEdge]));
        getInstance().getPlayers().forEach(player -> getBuilder().send(player, pos));
    }
}
