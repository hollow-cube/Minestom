package net.bytemc.minestom.server.display.head;

import lombok.Data;
import net.bytemc.minestom.server.display.head.misc.HeadSize;
import net.minestom.server.utils.Direction;

@Data
public final class HeadSettings {
    private Direction direction;
    private HeadSize headSize;
    private Boolean spacer;

    public HeadSettings() {
        this.direction = Direction.NORTH;
        this.headSize = HeadSize.BIG;
        this.spacer = true;
    }
}
