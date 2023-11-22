package net.bytemc.minestom.server.display.head.misc;

public enum HeadSize {
    BIG(1.6),
    MID(2.3),
    SMALL(3.8),
    TINY(7.6);

    private final double distance;

    HeadSize(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }
}
