package net.bytemc.minestom.server.particle;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

public abstract class AbstractParticleQueue {

    private Task task;
    private final ParticleBuilder builder;
    private final int speed;
    private final Instance instance;

    public AbstractParticleQueue(ParticleBuilder builder, int speed, Instance instance) {
        this.builder = builder;
        this.speed = speed;
        this.instance = instance;
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
}
