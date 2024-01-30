package net.minestom.server.exception;

import net.minestom.server.ServerProcess;

public final class ExceptionHandlerImpl implements ExceptionHandler {

    private final ServerProcess serverProcess;

    public ExceptionHandlerImpl(ServerProcess serverProcess) {
        this.serverProcess = serverProcess;
    }

    @Override
    public void handleException(Throwable e) {
        e.printStackTrace();
        if (e instanceof OutOfMemoryError) {
            // OOM should be handled manually
            serverProcess.stop();
        }
    }
}
