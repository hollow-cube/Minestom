package net.minestom.server.entity.pathfinding;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class PPath {
    private final Consumer<Void> onComplete;
    private final List<PNode> nodes = new ArrayList<>();

    private final double pathVariance;
    private final double maxDistance;
    private final PathfinderCapabilities capabilities;
    private int index = 0;
    private final AtomicReference<PathState> state = new AtomicReference<>(PathState.CALCULATING);

    public PathfinderCapabilities capabilities() {
        return capabilities;
    }

    public enum PathfinderType {
        LAND, AQUATIC, FLYING, AMPHIBIOUS
    }

    public record PathfinderCapabilities (PathfinderType type, boolean canJump, boolean canClimbAnything, float swimSpeedModifier) {
    }

    public void setState(PathState newState) {
        state.set(newState);
    }

    enum PathState {
        CALCULATING,
        FOLLOWING,
        TERMINATING, TERMINATED, COMPUTED, INVALID
    }

    PathState getState() {
        return state.get();
    }

    public List<PNode> getNodes() {
        return nodes;
    }

    public PPath(double maxDistance, double pathVariance, PathfinderCapabilities capabilities, Consumer<Void> onComplete) {
        this.onComplete = onComplete;
        this.maxDistance = maxDistance;
        this.pathVariance = pathVariance;
        this.capabilities = capabilities;
    }

    void runComplete() {
        if (onComplete != null) onComplete.accept(null);
    }

    @Override
    public String toString() {
        return nodes.toString();
    }

    PNode.NodeType getCurrentType() {
        if (index >= nodes.size()) return null;
        var current = nodes.get(index);
        return current.getType();
    }

    @Nullable
    Point getCurrent() {
        if (index >= nodes.size()) return null;
        var current = nodes.get(index);
        return current.point;
    }

    void next() {
        if (index >= nodes.size()) return;
        index++;
    }

    double maxDistance() {
        return maxDistance;
    }

    double pathVariance() {
        return pathVariance;
    }
}
