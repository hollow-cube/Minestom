package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.entity.projectile.ProjectileUncollideEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class EntityProjectileCollisionIntegrationTest {

    @Test
    public void blockShootAndBlockRemoval(Env env) {
        final Instance instance = env.createFlatInstance();
        instance.getWorldBorder().setDiameter(1000.0);

        final Entity shooter = new Entity(env.process(), EntityType.SKELETON);
        shooter.setInstance(instance, new Pos(0, 40, 0)).join();

        final EntityProjectile projectile = new EntityProjectile(env.process(), shooter, EntityType.ARROW);
        projectile.setInstance(instance, shooter.getPosition().withY(y -> y + shooter.getEyeHeight())).join();

        final Point blockPosition = new Vec(5, 40, 0);
        final Block block = Block.GRASS_BLOCK;
        instance.setBlock(blockPosition, block);
        projectile.shoot(blockPosition, 1, 0);

        final var eventRef = new AtomicReference<ProjectileCollideWithBlockEvent>();
        env.process().getGlobalEventHandler().addListener(ProjectileCollideWithBlockEvent.class, eventRef::set);

        final long tick = TimeUnit.getServerTick(env.process().getServerSettings()).getDuration().toMillis();
        for (int i = 0; i < env.process().getServerSettings().getTickPerSecond(); ++i) {
            projectile.tick(i * tick);
        }

        var event = eventRef.get();
        assertNotNull(event);
        assertEquals(blockPosition, new Vec(event.getCollisionPosition().blockX(), event.getCollisionPosition().blockY(), event.getCollisionPosition().blockZ()));
        assertEquals(block, event.getBlock());

        final var eventRef2 = new AtomicReference<ProjectileUncollideEvent>();
        env.process().getGlobalEventHandler().addListener(ProjectileUncollideEvent.class, eventRef2::set);
        eventRef.set(null);
        instance.setBlock(blockPosition, Block.AIR);

        for (int i = 0; i < env.process().getServerSettings().getTickPerSecond(); ++i) {
            projectile.tick((env.process().getServerSettings().getTickPerSecond() + i) * tick);
        }
        event = eventRef.get();
        final var event2 = eventRef2.get();
        assertNotNull(event);
        assertNotNull(event2);
        assertEquals(blockPosition.withY(y -> y - 1), new Vec(event.getCollisionPosition().blockX(), event.getCollisionPosition().blockY(), event.getCollisionPosition().blockZ()));
    }

    @Test
    public void entityShoot(Env env) {
        final Instance instance = env.createFlatInstance();
        instance.getWorldBorder().setDiameter(1000.0);

        final Entity shooter = new Entity(env.process(), EntityType.SKELETON);
        shooter.setInstance(instance, new Pos(0, 40, 0)).join();

        for (double dx = 1; dx <= 3; dx += .2) {
            singleEntityShoot(instance, shooter, new Vec(dx, 40, 0));
        }
    }

    private void singleEntityShoot(
            Instance instance,
            Entity shooter,
            final Point targetPosition
    ) {
        final EntityProjectile projectile = new EntityProjectile(shooter.getServerProcess(), shooter, EntityType.ARROW);
        projectile.setInstance(instance, shooter.getPosition().withY(y -> y + shooter.getEyeHeight())).join();

        final LivingEntity target = new LivingEntity(shooter.getServerProcess(), EntityType.RABBIT);
        target.setInstance(instance, Pos.fromPoint(targetPosition)).join();
        projectile.shoot(targetPosition, 1, 0);

        final var eventRef = new AtomicReference<ProjectileCollideWithEntityEvent>();
        final var eventNode = EventNode.all(shooter.getServerProcess(), "projectile-test");
        eventNode.addListener(ProjectileCollideWithEntityEvent.class, event -> {
            event.getEntity().remove();
            eventRef.set(event);
            shooter.getServerProcess().getGlobalEventHandler().removeChild(eventNode);
        });
        shooter.getServerProcess().getGlobalEventHandler().addChild(eventNode);

        final long tick = TimeUnit.getServerTick(instance.getServerProcess().getServerSettings()).getDuration().toMillis();
        for (int i = 0; i < instance.getServerProcess().getServerSettings().getTickPerSecond(); ++i) {
            if (!projectile.isRemoved()) {
                projectile.tick(i * tick);
            }
        }

        final var event = eventRef.get();
        assertNotNull(event, "Could not hit entity at " + targetPosition);
        assertSame(target, event.getTarget());
        assertTrue(projectile.getBoundingBox().intersectEntity(event.getCollisionPosition(), target));
        target.remove();
    }

    @Test
    public void entitySelfShoot(Env env) {
        final Instance instance = env.createFlatInstance();
        instance.getWorldBorder().setDiameter(1000.0);

        final LivingEntity shooter = new LivingEntity(env.process(), EntityType.SKELETON);
        shooter.setInstance(instance, new Pos(0, 40, 0)).join();

        final EntityProjectile projectile = new EntityProjectile(env.process(), shooter, EntityType.ARROW);
        projectile.setInstance(instance, shooter.getPosition().withY(y -> y + shooter.getEyeHeight())).join();

        projectile.shoot(new Vec(0, 60, 0), 1, 0);

        final var eventRef = new AtomicReference<ProjectileCollideWithEntityEvent>();
        env.process().getGlobalEventHandler().addListener(ProjectileCollideWithEntityEvent.class, event -> {
            event.getEntity().remove();
            eventRef.set(event);
        });

        final long tick = TimeUnit.getServerTick(env.process().getServerSettings()).getDuration().toMillis();
        for (int i = 0; i < env.process().getServerSettings().getTickPerSecond() * 5; ++i) {
            if (!projectile.isRemoved()) {
                projectile.tick(i * tick);
            }
        }

        final var event = eventRef.get();
        assertNotNull(event);
        assertSame(shooter, event.getTarget());
        assertTrue(shooter.getBoundingBox().intersectEntity(shooter.getPosition(), projectile));
    }

}
