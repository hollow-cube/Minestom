package net.minestom.server.thread;

import net.minestom.server.exception.ExceptionHandler;
import net.minestom.server.instance.Chunk;

public class ChunkDispatcherImpl extends ThreadDispatcherImpl<Chunk> implements ChunkDispatcher{
    ChunkDispatcherImpl(ExceptionHandler exceptionHandler, ThreadProvider<Chunk> provider, int threadCount) {
        super(exceptionHandler, provider, threadCount);
    }
}
