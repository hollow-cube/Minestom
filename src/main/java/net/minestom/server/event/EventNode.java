package net.minestom.server.event;

import net.minestom.server.event.handler.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public interface EventNode<T extends Event> {

    static <E extends Event> EventNode<E> type(@NotNull EventFilter<E, ? extends EventHandler> filter) {
        return new EventNodeImpl<>(filter);
    }

    static EventNode<Event> all() {
        return type(EventFilter.ALL);
    }

    static <E extends Event, H extends EventHandler> EventNodeConditional<E, H> conditional(@NotNull EventFilter<E, H> filter,
                                                                                            @NotNull Predicate<E> predicate) {
        return new EventNodeConditional<>(filter, predicate);
    }

    static <E extends Event, H extends EventHandler> EventNodeList<E, H> list(@NotNull EventFilter<E, H> filter) {
        return new EventNodeList<>(filter);
    }

    void call(@NotNull T event);

    void addChild(@NotNull EventNode<? extends T> child);

    void removeChild(@NotNull EventNode<? extends T> child);

    void addListener(@NotNull EventListener<? extends T> listener);

    void removeListener(@NotNull EventListener<? extends T> listener);

    @NotNull String getName();

    @NotNull List<@NotNull EventNode<T>> getChildren();
}
