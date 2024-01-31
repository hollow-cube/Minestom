package net.minestom.demo.entity;

import net.minestom.server.ServerFacade;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.EntityAIGroupBuilder;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;

public class ZombieCreature extends EntityCreature {

    public ZombieCreature(ServerFacade serverFacade) {
        super(serverFacade, EntityType.ZOMBIE);
        addAIGroup(
                new EntityAIGroupBuilder()
                        .addGoalSelector(new RandomLookAroundGoal(this, 20))
                        .build()
        );
    }
}
