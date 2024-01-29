package net.minestom.server.event;

import net.minestom.server.MinecraftServer;

/**
 * Object containing all the global event listeners.
 */
public final class GlobalEventHandler extends EventNodeImpl<Event> {
    public GlobalEventHandler(MinecraftServer minecraftServer) {
        super(minecraftServer,"global", EventFilter.ALL, null);
    }
}
