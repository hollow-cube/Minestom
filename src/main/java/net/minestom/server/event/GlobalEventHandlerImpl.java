package net.minestom.server.event;

import net.minestom.server.exception.ExceptionHandler;

/**
 * Object containing all the global event listeners.
 */
public final class GlobalEventHandlerImpl extends EventNodeImpl<Event> implements GlobalEventHandler {
    public GlobalEventHandlerImpl(ExceptionHandler exceptionHandler) {
        super(exceptionHandler, "global", EventFilter.ALL, null);
    }
}
