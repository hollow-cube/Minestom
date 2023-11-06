package net.bytemc.minestom.server.display.head.misc;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HeadSize {
    BIG(1.6),
    MID(2.3),
    SMALL(3.0),
    TINY(3.7);

    private final double distance;
}
