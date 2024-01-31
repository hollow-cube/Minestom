package net.minestom.server.thread;

import net.minestom.server.exception.ExceptionHandlerProvider;
import net.minestom.server.instance.Chunk;

public class ChunkDispatcherImpl extends ThreadDispatcherImpl<Chunk> implements ChunkDispatcher{
    ChunkDispatcherImpl(ExceptionHandlerProvider exceptionHandlerProvider, ThreadProvider<Chunk> provider, int threadCount) {
        super(exceptionHandlerProvider, provider, threadCount);
    }
}
