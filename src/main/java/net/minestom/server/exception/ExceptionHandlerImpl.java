package net.minestom.server.exception;

public final class ExceptionHandlerImpl implements ExceptionHandler {

    @Override
    public void handleException(Throwable e) {
        e.printStackTrace();
    }
}
