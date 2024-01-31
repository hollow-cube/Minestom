package net.minestom.server;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public
interface Ticker {
    void tick(long nanoTime);
}
