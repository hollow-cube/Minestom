package net.bytemc.minestom.server.schematics;

import org.jetbrains.annotations.NotNull;

public enum Rotation {

    NONE,
    CLOCKWISE_90,
    CLOCKWISE_180,
    CLOCKWISE_270;

    public Rotation rotate(Rotation rotation) {
        return values()[(ordinal() + rotation.ordinal()) % 4];
    }

    public static @NotNull Rotation from(@NotNull net.minestom.server.utils.Rotation rotation) {
        return values()[rotation.ordinal() / 2];
    }
}

