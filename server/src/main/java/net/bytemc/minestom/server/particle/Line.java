package net.bytemc.minestom.server.particle;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.UnknownNullability;

public class Line implements Curve {
    private final Point[] vertices;
    private final double[] lengths;
    private double currentPosition;
    private int currentEdge;

    public Line(Point[] vertices) {
        this.vertices = vertices.clone();
        this.lengths = new double[vertices.length - 1];
        for (int i = 0; i < lengths.length; i++) {
            lengths[i] = vertices[i].distance(vertices[i + 1]);
        }
        reset();
    }

    @Override
    @UnknownNullability
    public Point getNextPoint(double distance, boolean continueFromStart) {
        currentPosition += distance;
        while (currentPosition >= lengths[currentEdge]) {
            currentPosition -= lengths[currentEdge];
            currentEdge++;
            if (currentEdge >= lengths.length) {
                if (continueFromStart) {
                    currentEdge = 0;
                } else {
                    return null;
                }
            }
        }
        var edge = vertices[currentEdge + 1].sub(vertices[currentEdge]);
        return vertices[currentEdge].add(edge.mul(currentPosition / lengths[currentEdge]));
    }

    public void reset() {
        currentEdge = 0;
        currentPosition = 0;
    }
}
