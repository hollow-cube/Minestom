package net.minestom.server.thread;

import net.minestom.server.exception.ExceptionHandlerProvider;
import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.NotNull;

public interface ChunkDispatcher extends ThreadDispatcher<Chunk>{
    static @NotNull ChunkDispatcher of(ExceptionHandlerProvider exceptionHandlerProvider, @NotNull ThreadProvider<Chunk> provider, int threadCount) {
        return new ChunkDispatcherImpl(exceptionHandlerProvider, provider, threadCount);
    }

    static @NotNull ChunkDispatcher singleThread(ExceptionHandlerProvider exceptionHandlerProvider) {
        return of(exceptionHandlerProvider, ThreadProvider.counter(), 1);
    }
}
