package net.minestom.server.event;

import net.minestom.server.exception.ExceptionHandlerProvider;

/**
 * Object containing all the global event listeners.
 */
public final class GlobalEventHandlerImpl extends EventNodeImpl<Event> implements GlobalEventHandler {
    public GlobalEventHandlerImpl(ExceptionHandlerProvider exceptionHandlerProvider) {
        super(exceptionHandlerProvider, "global", EventFilter.ALL, null);
    }
}
