package net.bytemc.minestom.server.display.head.misc;

public enum HeadSize {
    BIG(1.6),
    MID(2.3),
    SMALL(3.0),
    TINY(3.7);

    private final double distance;

    HeadSize(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }
}
