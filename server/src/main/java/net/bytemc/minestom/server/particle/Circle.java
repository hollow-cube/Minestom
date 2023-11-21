package net.bytemc.minestom.server.particle;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.UnknownNullability;

public class Circle implements Curve {
    private final Point center;
    private final double radius;
    private double currentDistance;

    public Circle(Point center, double radius) {
        this.center = center;
        this.radius = radius;
        reset();
    }

    public double getLength() {
        return 2 * Math.PI * radius;
    }

    @Override
    @UnknownNullability
    public Point getNextPoint(double distance, boolean continueFromStart) {
        currentDistance += distance;
        if (currentDistance > getLength()) {
            if (continueFromStart) {
                currentDistance %= getLength();
            } else {
                return null;
            }
        }

        var angle = currentDistance / radius;
        var relativePoint = new Vec(Math.cos(angle), Math.sin(angle)).mul(radius);
        return center.add(relativePoint);
    }

    @Override
    public void reset() {
        currentDistance = 0;
    }
}
