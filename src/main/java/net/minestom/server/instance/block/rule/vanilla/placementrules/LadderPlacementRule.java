package net.minestom.server.instance.block.rule.vanilla.placementrules;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LadderPlacementRule extends BlockPlacementRule {
    private static final List<BlockFace> HORIZONTAL_FACES = List.of(
            BlockFace.NORTH,
            BlockFace.WEST,
            BlockFace.SOUTH,
            BlockFace.EAST
    );

    private static final String PROP_FACING = "facing";

    public LadderPlacementRule() {
        super(Block.LADDER);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        var blockFace = placementState.blockFace();
        return switch (blockFace) {
            case NORTH, SOUTH, EAST, WEST -> block.withProperty(PROP_FACING, blockFace.name().toLowerCase());
            case TOP, BOTTOM -> {
                var instance = placementState.instance();

                var facingFace = BlockFace.fromYaw(placementState.playerPosition().yaw());
                for (var neighborFace : getFaceOrder(facingFace)) {
                    var neighbor = instance.getBlock(placementState.placePosition().relative(neighborFace));
                    if (neighbor.isSolid()) {
                        yield block.withProperty(PROP_FACING, neighborFace.getOppositeFace().name().toLowerCase());
                    }
                }

                yield null;
            }
        };
    }

    private @NotNull BlockFace[] getFaceOrder(@NotNull BlockFace facingFace) {
        return new BlockFace[]{
                facingFace, // Front
                HORIZONTAL_FACES.get((HORIZONTAL_FACES.indexOf(facingFace) + 1) % HORIZONTAL_FACES.size()), // CW
                HORIZONTAL_FACES.get((HORIZONTAL_FACES.indexOf(facingFace) + 3) % HORIZONTAL_FACES.size()), // CCW
                facingFace.getOppositeFace(), // Opposite
        };
    }

}
