package net.minestom.server.timer;

import org.jetbrains.annotations.NotNull;

public interface SchedulerManager extends Scheduler {
    void shutdown();

    void buildShutdownTask(@NotNull Runnable runnable);
}
