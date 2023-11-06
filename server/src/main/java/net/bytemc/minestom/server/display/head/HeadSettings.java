package net.bytemc.minestom.server.display.head;

import net.bytemc.minestom.server.display.head.misc.HeadSize;
import net.minestom.server.utils.Direction;

public final class HeadSettings {
    private Direction direction;
    private HeadSize headSize;
    private Boolean spacer;

    public HeadSettings() {
        this.direction = Direction.NORTH;
        this.headSize = HeadSize.BIG;
        this.spacer = true;
    }

    public Direction getDirection() {
        return direction;
    }

    public void withDirection(Direction direction) {
        this.direction = direction;
    }

    public HeadSize getHeadSize() {
        return headSize;
    }

    public void withHeadSize(HeadSize headSize) {
        this.headSize = headSize;
    }

    public Boolean getSpacer() {
        return spacer;
    }

    public void withSpacer(Boolean spacer) {
        this.spacer = spacer;
    }
}
