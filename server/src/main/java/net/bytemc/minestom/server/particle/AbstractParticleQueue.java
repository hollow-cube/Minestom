package net.bytemc.minestom.server.particle;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.util.function.Predicate;

public abstract class AbstractParticleQueue {
    private final ParticleBuilder builder;
    private final int speed;
    private final Instance instance;
    private Predicate<Player> visibility;

    private Task task;

    public AbstractParticleQueue(ParticleBuilder builder, int speed, Instance instance, Predicate<Player> visibility) {
        this.builder = builder;
        this.speed = speed;
        this.instance = instance;
        this.visibility = visibility;
    }

    public AbstractParticleQueue(ParticleBuilder builder, int speed, Instance instance) {
        this(builder, speed, instance, p -> true);
    }

    public Instance getInstance() {
        return instance;
    }

    public ParticleBuilder getBuilder() {
        return builder;
    }

    public abstract void tick();

    public void stop() {
        if(task != null) {
            task.cancel();
            task = null;
        }
    }

    public void run() {
        this.task = MinecraftServer.getSchedulerManager().submitTask(() -> {
            tick();
            return TaskSchedule.millis(speed);
        });
    }

    public void setVisibility(Predicate<Player> visibility) {
        this.visibility = visibility;
    }

    public void sendParticle(Point point) {
        instance.getPlayers().stream().filter(visibility).forEach(p -> getBuilder().send(p, point));
    }
}
