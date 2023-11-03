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
        double angle = switch (this) {
            case WEST -> 90;
            case SOUTH -> 180;
            case EAST -> 270;
            default -> 0;
        };

        return vector.rotateAroundY(Math.toRadians(angle));
    }

    public Direction getChestFacing() {
        return switch (this) {
            case NORTH -> SOUTH;
            case EAST -> WEST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> null;
        };
    }

    public Direction getSignFacing() {
        return switch (this) {
            case NORTH -> SOUTH;
            case EAST -> WEST;
            case SOUTH -> NORTH;
            case WEST -> EAST;
            default -> null;
        };
    }

    public static Direction fromDirection(Point direction) {
        double x = direction.x();
        double z = direction.z();

        if (Math.abs(x) > Math.abs(z)) {
            return x < 0 ? WEST : EAST;
        } else {
            return z < 0 ? NORTH : SOUTH;
        }
    }
}
