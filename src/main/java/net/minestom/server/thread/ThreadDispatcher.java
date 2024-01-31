package net.minestom.server.thread;

import net.minestom.server.Tickable;
import net.minestom.server.exception.ExceptionHandlerProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface ThreadDispatcher<P> {
    static <P> @NotNull ThreadDispatcher<P> of(ExceptionHandlerProvider exceptionHandlerProvider, @NotNull ThreadProvider<P> provider, int threadCount) {
        return new ThreadDispatcherImpl<>(exceptionHandlerProvider, provider, threadCount);
    }

    static <P> @NotNull ThreadDispatcher<P> singleThread(ExceptionHandlerProvider exceptionHandlerProvider) {
        return of(exceptionHandlerProvider, ThreadProvider.counter(), 1);
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
