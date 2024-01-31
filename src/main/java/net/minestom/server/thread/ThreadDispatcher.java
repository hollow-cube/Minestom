package net.minestom.server.thread;

import net.minestom.server.Tickable;
import net.minestom.server.exception.ExceptionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface ThreadDispatcher<P> {
    static <P> @NotNull ThreadDispatcher<P> of(ExceptionHandler exceptionHandler, @NotNull ThreadProvider<P> provider, int threadCount) {
        return new ThreadDispatcherImpl<>(exceptionHandler, provider, threadCount);
    }

    static <P> @NotNull ThreadDispatcher<P> singleThread(ExceptionHandler exceptionHandler) {
        return of(exceptionHandler, ThreadProvider.counter(), 1);
    }

    @Unmodifiable
    @NotNull List<@NotNull TickThread> threads();

    void updateAndAwait(long time);

    void refreshThreads(long nanoTimeout);

    void refreshThreads();

    void createPartition(P partition);

    void deletePartition(P partition);

    void updateElement(Tickable tickable, P partition);

    void removeElement(Tickable tickable);

    void shutdown();
}
