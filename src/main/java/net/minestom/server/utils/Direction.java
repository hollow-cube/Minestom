package net.minestom.server.utils;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

public enum Direction {
    DOWN(0, -1, 0),
    UP(0, 1, 0),
    NORTH(0, 0, -1),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    EAST(1, 0, 0);

    public static final Direction[] HORIZONTAL = {SOUTH, WEST, NORTH, EAST};

    private final int normalX;
    private final int normalY;
    private final int normalZ;

    Direction(int normalX, int normalY, int normalZ) {
        this.normalX = normalX;
        this.normalY = normalY;
        this.normalZ = normalZ;
    }

    public int normalX() {
        return normalX;
    }

    public int normalY() {
        return normalY;
    }

    public int normalZ() {
        return normalZ;
    }

    public @NotNull Direction opposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case EAST -> WEST;
            case WEST -> EAST;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
        };
    }

    public Vec rotate(Vec vector) {
        return vector.rotateAroundY(Math.toRadians(getYaw()));
    }

    public Direction rotateDirectionOnce() {
        return switch (this) {
            case NORTH -> EAST;
            case EAST -> SOUTH;
            case SOUTH -> WEST;
            case WEST -> NORTH;
            default -> null;
        };
    }

    public String getFacingProperty() {
        return name().toLowerCase();
    }

    public float getYaw() {
        return switch (this) {
            case NORTH -> 180;
            case EAST -> -90;
            case WEST -> 90;
            default -> 0;
        };
    }

    public boolean isHorizontal() {
        return this.normalY == 0;
    }

    public static Direction fromPoint(Point direction, boolean onlyHorizontal) {
        double x = direction.x();
        double y = direction.y();
        double z = direction.z();

        if ((Math.abs(x) > Math.abs(y) || onlyHorizontal) && Math.abs(x) > Math.abs(z)) {
            return x < 0 ? WEST : EAST;
        } else if (Math.abs(y) > Math.abs(z) && !onlyHorizontal) {
            return y < 0 ? DOWN : UP;
        } else {
            return z < 0 ? NORTH : SOUTH;
        }
    }
    public static Direction fromPoint(Point direction) {
        return fromPoint(direction, false);
    }
}
