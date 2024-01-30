package net.minestom.server.event;

import net.minestom.server.ServerProcess;

/**
 * Object containing all the global event listeners.
 */
public final class GlobalEventHandler extends EventNodeImpl<Event> {
    public GlobalEventHandler(ServerProcess serverProcess) {
        super(serverProcess,"global", EventFilter.ALL, null);
    }
}
