package net.bytemc.minestom.server.schematics;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.batch.BatchOption;
import net.minestom.server.instance.batch.RelativeBlockBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Schematic {
    private final Point size;
    private final Point offset;
    private final Block[] palette;
    private final byte[] blocks;

    public Schematic(Point size, Point offset, Block[] palette, byte[] blocks) {
        this.size = size;
        this.offset = offset;
        this.palette = Arrays.copyOf(palette, palette.length);;
        this.blocks = Arrays.copyOf(blocks, blocks.length);
    }

    public Block[] palette() {
        return Arrays.copyOf(palette, palette.length);
    }

    public byte[] blocks() {
        return Arrays.copyOf(blocks, blocks.length);
    }

    public Point size(Rotation rotation) {
        final Point rotatedPos = rotatePos(size, rotation);

        return new Vec(Math.abs(rotatedPos.blockX()), Math.abs(rotatedPos.blockY()), Math.abs(rotatedPos.blockZ()));
    }

    public Point offset(Rotation rotation) {
        return rotatePos(offset, rotation);
    }

    public @NotNull RelativeBlockBatch build(@NotNull Rotation rotation, @Nullable Function<Block, Block> blockModifier) {
        RelativeBlockBatch batch = new RelativeBlockBatch(new BatchOption().setCalculateInverse(true));
        apply(rotation, (pos, block) -> batch.setBlock(pos, blockModifier == null ? block : blockModifier.apply(block)));
        return batch;
    }

    public void apply(@NotNull Rotation rotation, @NotNull BiConsumer<Point, Block> applicator) {
        ByteBuffer blocks = ByteBuffer.wrap(this.blocks);
        for (int y = 0; y < size(rotation).y(); y++) {
            for (int z = 0; z < size.z(); z++) {
                for (int x = 0; x < size.x(); x++) {
                    int blockVal = Utils.readVarInt(blocks);
                    Block b = palette[blockVal];

                    if (b == null || b.isAir()) {
                        continue;
                    }

                    Vec blockPos = new Vec(x + offset.x(), y + offset.y(), z + offset.z());
                    applicator.accept(rotatePos(blockPos, rotation), rotateBlock(b, rotation));
                }
            }
        }
    }

    private static @NotNull Point rotatePos(@NotNull Point point, @NotNull Rotation rotation) {
        return switch (rotation) {
            case NONE -> point;
            case CLOCKWISE_90 -> new Vec(-point.z(), point.y(), point.x());
            case CLOCKWISE_180 -> new Vec(-point.x(), point.y(), -point.z());
            case CLOCKWISE_270 -> new Vec(point.z(), point.y(), -point.x());
        };
    }

    public static @NotNull Block rotateBlock(@NotNull Block block, @NotNull Rotation rotation) {
        if (rotation == Rotation.NONE) return block;

        Block newBlock = block;

        if (block.getProperty("facing") != null) {
            newBlock = rotateFacing(block, rotation);
        }
        if (block.getProperty("north") != null) {
            newBlock = rotateFence(block, rotation);
        }

        return newBlock;
    }

    private static Block rotateFacing(Block block, Rotation rotation) {
        return switch (rotation) {
            case NONE -> block;
            case CLOCKWISE_90 -> block.withProperty("facing", rotate90(block.getProperty("facing")));
            case CLOCKWISE_180 -> block.withProperty("facing", rotate90(rotate90(block.getProperty("facing"))));
            case CLOCKWISE_270 ->
                    block.withProperty("facing", rotate90(rotate90(rotate90(block.getProperty("facing")))));
        };
    }

    private static Block rotateFence(Block block, Rotation rotation) {
        return switch (rotation) {
            case NONE -> block;
            case CLOCKWISE_90 -> block.withProperties(Map.of(
                    "north", block.getProperty("west"),
                    "east", block.getProperty("north"),
                    "south", block.getProperty("east"),
                    "west", block.getProperty("south")
            ));
            case CLOCKWISE_180 -> block.withProperties(Map.of(
                    "north", block.getProperty("south"),
                    "east", block.getProperty("west"),
                    "south", block.getProperty("north"),
                    "west", block.getProperty("east")
            ));
            case CLOCKWISE_270 -> block.withProperties(Map.of(
                    "north", block.getProperty("east"),
                    "east", block.getProperty("south"),
                    "south", block.getProperty("west"),
                    "west", block.getProperty("north")
            ));
        };
    }

    private static String rotate90(String in) {
        return switch (in) {
            case "north" -> "east";
            case "east" -> "south";
            case "south" -> "west";
            default -> "north";
        };
    }
}

