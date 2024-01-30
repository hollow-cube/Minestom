package net.minestom.server.exception;

/**
 * Used when you want to implement your own exception handling, instead of just printing the stack trace.
 */
@FunctionalInterface
public interface ExceptionHandler {

    /**
     * Called when an exception was caught.
     *
     * @param e the thrown exception
     */
    void handleException(Throwable e);
}
