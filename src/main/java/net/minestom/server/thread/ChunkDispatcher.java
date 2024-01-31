package net.minestom.server.thread;

import net.minestom.server.exception.ExceptionHandler;
import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.NotNull;

public interface ChunkDispatcher extends ThreadDispatcher<Chunk>{
    static @NotNull ChunkDispatcher of(ExceptionHandler exceptionHandler, @NotNull ThreadProvider<Chunk> provider, int threadCount) {
        return new ChunkDispatcherImpl(exceptionHandler, provider, threadCount);
    }

    static @NotNull ChunkDispatcher singleThread(ExceptionHandler exceptionHandler) {
        return of(exceptionHandler, ThreadProvider.counter(), 1);
    }
}
