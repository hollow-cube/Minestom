package net.minestom.server.event;

import net.minestom.server.exception.ExceptionHandler;

/**
 * Object containing all the global event listeners.
 */
public final class GlobalEventHandler extends EventNodeImpl<Event> {
    public GlobalEventHandler(ExceptionHandler exceptionHandler) {
        super(exceptionHandler, "global", EventFilter.ALL, null);
    }
}
