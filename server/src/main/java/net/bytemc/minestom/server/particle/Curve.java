package net.bytemc.minestom.server.particle;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

interface Curve {
    @NotNull
    default Point getNextPoint(double distance) {
        return getNextPoint(distance, true);
    }
    @UnknownNullability
    Point getNextPoint(double distance, boolean continueFromStart);
    void reset();
}
