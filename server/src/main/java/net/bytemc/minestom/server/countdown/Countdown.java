package net.bytemc.minestom.server.countdown;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Countdown {

    private Task task;

    private final int maxValue;
    private int value;

    private final List<Predicate<Void>> predicates = new ArrayList<>();

    @Nullable
    private Runnable stopCallback;

    public Countdown(int maxValue) {
        this.maxValue = maxValue;
    }

    public void start() {
        task = MinecraftServer.getSchedulerManager().submitTask(() -> {
            if(!checkAllPredicates()) {
                this.value = maxValue;
                return TaskSchedule.seconds(1);
            }

            if (value <= 0) {
                if (stopCallback != null) {
                    stopCallback.run();
                    return TaskSchedule.stop();
                }
            }
            value--;
            return TaskSchedule.seconds(1);
        });
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void reduce(int amount) {
        this.value -= amount;

        if(value <= 0) {
            if(stopCallback != null) {
                stopCallback.run();
                this.stop();
            }
        }
    }

    private boolean checkAllPredicates() {
        return predicates.stream().allMatch(predicate -> predicate.test(null));
    }

    public Countdown setStopCallback() {
        this.stopCallback = stopCallback;
        return this;
    }

    public Countdown addPredicate(Predicate<Void> predicate) {
        this.predicates.add(predicate);
        return this;
    }
}