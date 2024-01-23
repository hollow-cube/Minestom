package net.minestom.server.entity.pathfinding;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.position.PositionUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLOutput;
import java.util.List;
import java.util.function.Consumer;

// TODO all pathfinding requests could be processed in another thread

/**
 * Necessary object for all {@link NavigableEntity}.
 */
public final class Navigator {
    private Point goalPosition;
    private final Entity entity;

    // Essentially a double buffer. Wait until a path is done computing before replpacing the old one.
    private PPath computingPath;
    private PPath path;

    private double minimumDistance;
    private float movementSpeed = 0.1f;

    public Navigator(@NotNull Entity entity) {
        this.entity = entity;
    }

    public PPath.PathState getState() {
        if (path == null && computingPath == null) return PPath.PathState.INVALID;
        if (path == null) return computingPath.getState();
        return path.getState();
    }

    /**
     * Used to move the entity toward {@code direction} in the X and Z axis
     * Gravity is still applied but the entity will not attempt to jump
     * Also update the yaw/pitch of the entity to look along 'direction'
     *
     * @param direction    the targeted position
     * @param speed        define how far the entity will move
     * @param capabilities
     */
    public void moveTowards(@NotNull Point direction, double speed, PPath.PathfinderCapabilities capabilities, Point lookAt) {
        final Pos position = entity.getPosition();
        final double dx = direction.x() - position.x();
        final double dy = direction.y() - position.y();
        final double dz = direction.z() - position.z();

        final double dxLook = lookAt.x() - position.x();
        final double dyLook = lookAt.y() - position.y();
        final double dzLook = lookAt.z() - position.z();

        // the purpose of these few lines is to slow down entities when they reach their destination
        final double distSquared = dx * dx + dy * dy + dz * dz;
        if (speed > distSquared) {
            speed = distSquared;
        }

        boolean inWater = false;
        var instance = entity.getInstance();
        if (instance != null)
            if (instance.getBlock(position).isLiquid()) {
                speed *= capabilities.swimSpeedModifier();
                inWater = true;
            }

        final double radians = Math.atan2(dz, dx);
        final double speedX = Math.cos(radians) * speed;
        final double speedZ = Math.sin(radians) * speed;
        final float yaw = PositionUtils.getLookYaw(dxLook, dzLook);
        final float pitch = PositionUtils.getLookPitch(dxLook, dyLook, dzLook);

        final double speedY = (capabilities.type() == PPath.PathfinderType.AQUATIC
                || capabilities.type() == PPath.PathfinderType.FLYING
                || (capabilities.type() == PPath.PathfinderType.AMPHIBIOUS && inWater))
                ? Math.signum(dy) * 0.5 * speed
                : 0;

        final var physicsResult = CollisionUtils.handlePhysics(entity, new Vec(speedX, speedY, speedZ));
        this.entity.refreshPosition(Pos.fromPoint(physicsResult.newPosition()).withView(yaw, pitch));
    }

    public void jump(float height) {
        // FIXME magic value
        this.entity.setVelocity(new Vec(0, height * 2.5f, 0));
    }

    public synchronized boolean setPathTo(@Nullable Point point) {
        BoundingBox bb = this.entity.getBoundingBox();
        double centerToCorner = Math.sqrt(bb.width() * bb.width() + bb.depth() * bb.depth()) / 2;
        return setPathTo(point, centerToCorner, null);
    }

    public synchronized boolean setPathTo(@Nullable Point point, double minimumDistance, Runnable onComplete) {
        return setPathTo(point, minimumDistance, 50, 20, PPath.PathfinderType.LAND, onComplete);
    }

    /**
     * Sets the path to {@code position} and ask the entity to follow the path.
     *
     * @param point the position to find the path to, null to reset the pathfinder
     * @param minimumDistance distance to target when completed
     * @param maxDistance maximum search distance
     * @param pathVariance how far to search off of the direct path. For open worlds, this can be low (around 20) and for large mazes this needs to be very high.
     * @param onComplete called when the path has been completed
     * @return true if a path has been found
     */
    public synchronized boolean setPathTo(@Nullable Point point, double minimumDistance, double maxDistance, double pathVariance, PPath.PathfinderType type, Runnable onComplete) {
        double previousDistance = point == null || goalPosition == null ? 0 : entity.getPosition().distance(goalPosition);
        double currentDistance = point == null ? 0 : point.distance(entity.getPosition());

        double percentageDifference = previousDistance == 0 ? 0 : Math.abs(currentDistance - previousDistance) / previousDistance;

        if (point != null && goalPosition != null && this.path != null && percentageDifference < 0.01) {
            return false;
        }

        final Instance instance = entity.getInstance();
        if (point == null) {
            this.path = null;
            return false;
        }

        // Can't path with a null instance.
        if (instance == null) {
            this.path = null;
            return false;
        }

        // Can't path outside the world border
        final WorldBorder worldBorder = instance.getWorldBorder();
        if (!worldBorder.isInside(point)) {
            return false;
        }
        // Can't path in an unloaded chunk
        final Chunk chunk = instance.getChunkAt(point);
        if (!ChunkUtils.isLoaded(chunk)) {
            return false;
        }

        this.minimumDistance = minimumDistance;
        if (this.entity.getPosition().distance(point) < minimumDistance) {
            if (onComplete != null) onComplete.run();
            return false;
        }

        if (goalPosition != null && point.sameBlock(goalPosition)) {
            if (onComplete != null) onComplete.run();
            return false;
        }

        if (this.computingPath != null) this.computingPath.setState(PPath.PathState.TERMINATING);

        this.computingPath = PathGenerator.generate(instance,
                        this.entity.getPosition(),
                        point,
                        minimumDistance, maxDistance,
                        pathVariance,
                this.entity.getBoundingBox(),
                new PPath.PathfinderCapabilities(type, true, true, 0.4f), onComplete);

        final boolean success = computingPath != null;
        this.goalPosition = success ? point : null;
        return success;
    }

    @ApiStatus.Internal
    public synchronized void tick() {
        if (goalPosition == null) return; // No path
        if (entity instanceof LivingEntity && ((LivingEntity) entity).isDead()) return; // No pathfinding tick for dead entities
        if (computingPath != null && computingPath.getState() == PPath.PathState.COMPUTED) {
            if (path != null && path.getCurrentType() != PNode.NodeType.REPATH) {
                var currentNode = path.getCurrent();
                if (currentNode != null) {
                    for (int i = 0; i < computingPath.getNodes().size(); ++i) {
                        var node = computingPath.getNodes().get(i);
                        if (node.point.sameBlock(currentNode)) {
                            computingPath.getNodes().subList(0, i).clear();
                            break;
                        }
                    }
                }
            }

            path = computingPath;
            computingPath = null;
        }

        if (path == null) return;

        // If the path is computed start following it
        if (path.getState() == PPath.PathState.COMPUTED) {
            path.setState(PPath.PathState.FOLLOWING);
            // Remove nodes that are too close to the start. Prevents doubling back to hit points that have already been hit
            for (int i = 0; i < path.getNodes().size(); i++) {
                if (path.getNodes().get(i).point.sameBlock(entity.getPosition())) {
                    path.getNodes().subList(0, i).clear();
                    break;
                }
            }
        }

        // If the state is not following, wait until it is
        if (path.getState() != PPath.PathState.FOLLOWING) return;

        // If we're near the entity, we're done
        if (this.entity.getPosition().distance(goalPosition) < minimumDistance) {
            path.runComplete();
            path = null;

            return;
        }

        Point currentTarget = path.getCurrent();
        Point nextTarget = path.getNext();

        // If we're at the end of the path, navigate directly to the entity
        if (nextTarget == null) nextTarget = goalPosition;

        // Repath
        if (currentTarget == null || path.getCurrentType() == PNode.NodeType.REPATH || path.getCurrentType() == null) {
            computingPath = PathGenerator.generate(entity.getInstance(),
                    entity.getPosition(),
                    Pos.fromPoint(goalPosition),
                    minimumDistance, path.maxDistance(),
                    path.pathVariance(), entity.getBoundingBox(), path.capabilities(), null);

            return;
        }

        if (entity instanceof LivingEntity living) {
            movementSpeed = living.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue();
        }

        boolean nextIsRepath = nextTarget.sameBlock(Pos.ZERO);

        // drawPath(path);
        moveTowards(currentTarget, movementSpeed, path.capabilities(), nextIsRepath ? currentTarget : nextTarget);

        if ((path.getCurrentType() == PNode.NodeType.JUMP || currentTarget.y() > entity.getPosition().y() + 0.1)
                && entity.isOnGround()
                && path.capabilities().canJump()
        ) {
            jump(4f);
        }

        if (entity.getPosition().sameBlock(currentTarget)) path.next();
    }

    /**
     * Gets the target pathfinder position.
     *
     * @return the target pathfinder position, null if there is no one
     */
    public @Nullable Point getGoalPosition() {
        return goalPosition;
    }

    public @NotNull Entity getEntity() {
        return entity;
    }

    public void reset() {
        if (this.path != null) this.path.setState(PPath.PathState.TERMINATING);
        this.goalPosition = null;
        this.path = null;

        if (this.computingPath != null) this.computingPath.setState(PPath.PathState.TERMINATING);
        this.computingPath = null;
    }

    public boolean isComplete() {
        if (this.path == null) return true;
        return goalPosition == null || entity.getPosition().sameBlock(goalPosition);
    }

    public List<PNode> getNodes() {
        if (this.path == null && computingPath == null) return null;
        if (this.path == null) return computingPath.getNodes();
        return this.path.getNodes();
    }

    public Point getPathPosition() {
        return goalPosition;
    }

    /**
     * Visualise path for debugging
     * @param path the path to draw
     */
    private void drawPath(PPath path) {
        if (path == null) return;

        for (PNode point : path.getNodes()) {
            Point pos = point.point();
            var packet = ParticleCreator.createParticlePacket(Particle.COMPOSTER, pos.x(), pos.y() + 0.5, pos.z(), 0, 0, 0, 1);
            entity.sendPacketToViewers(packet);
        }
    }
}
