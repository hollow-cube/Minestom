package net.minestom.server.instance.light;

import it.unimi.dsi.fastutil.shorts.ShortArrayFIFOQueue;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static net.minestom.server.instance.light.LightCompute.*;

final class SkyLight implements Light {
    private final Palette blockPalette;

    private byte[] content;
    private byte[] contentPropagation;
    private byte[] contentPropagationSwap;

    private boolean isValidBorders = true;
    private boolean needsSend = true;

    private Set<Point> toUpdateSet = new HashSet<>();

    private boolean fullyLit = false;
    private static final byte[][] bordersFullyLit = new byte[6][SIDE_LENGTH];
    private static final byte[] contentFullyLit = new byte[LIGHT_LENGTH];

    static {
        Arrays.fill(contentFullyLit, (byte) -1);
        for (byte[] border : bordersFullyLit) {
            Arrays.fill(border, (byte) 14);
        }
    }

    SkyLight(Palette blockPalette) {
        this.blockPalette = blockPalette;
    }

    @Override
    public Set<Point> flip() {
        if (this.contentPropagationSwap != null)
            this.contentPropagation = this.contentPropagationSwap;

        this.contentPropagationSwap = null;

        if (toUpdateSet == null) return Set.of();
        return toUpdateSet;
    }

    static ShortArrayFIFOQueue buildInternalQueue(Chunk c, int sectionY) {
        ShortArrayFIFOQueue lightSources = new ShortArrayFIFOQueue();

        if (c instanceof LightingChunk lc) {
            int[] heightmap = lc.calculateHeightMap();
            int maxY = c.getInstance().getDimensionType().getMinY() + c.getInstance().getDimensionType().getHeight();
            int sectionMaxY = (sectionY + 1) * 16 - 1;
            int sectionMinY = sectionY * 16;

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int height = heightmap[z << 4 | x];

                    for (int y = Math.min(sectionMaxY, maxY); y >= Math.max(height, sectionMinY); y--) {
                        int index = x | (z << 4) | ((y % 16) << 8);
                        lightSources.enqueue((short) (index | (15 << 12)));
                    }
                }
            }
        }

        return lightSources;
    }

    private static Block getBlock(Palette palette, int x, int y, int z) {
        return Block.fromStateId((short)palette.get(x, y, z));
    }

    private static ShortArrayFIFOQueue buildExternalQueue(Instance instance, Palette blockPalette, Map<BlockFace, Point> neighbors, byte[] content) {
        ShortArrayFIFOQueue lightSources = new ShortArrayFIFOQueue();

        for (BlockFace face : BlockFace.values()) {
            Point neighborSection = neighbors.get(face);
            if (neighborSection == null) continue;

            Chunk chunk = instance.getChunk(neighborSection.blockX(), neighborSection.blockZ());
            if (chunk == null) continue;

            byte[] neighborFace = chunk.getSection(neighborSection.blockY()).skyLight().getBorderPropagation(face.getOppositeFace());
            if (neighborFace == null) continue;

            for (int bx = 0; bx < 16; bx++) {
                for (int by = 0; by < 16; by++) {
                    final int borderIndex = bx * SECTION_SIZE + by;
                    byte lightEmission = neighborFace[borderIndex];

                    if (content != null) {
                        final int internalEmission = computeBorders(content, face)[borderIndex];
                        if (lightEmission <= internalEmission) continue;
                    }

                    final int k = switch (face) {
                        case WEST, BOTTOM, NORTH -> 0;
                        case EAST, TOP, SOUTH -> 15;
                    };

                    Section otherSection = chunk.getSection(neighborSection.blockY());

                    final int posTo = switch (face) {
                        case NORTH, SOUTH -> bx | (k << 4) | (by << 8);
                        case WEST, EAST -> k | (by << 4) | (bx << 8);
                        default -> bx | (by << 4) | (k << 8);
                    };

                    final Block blockTo = switch(face) {
                        case NORTH, SOUTH -> getBlock(blockPalette, bx, by, k);
                        case WEST, EAST -> getBlock(blockPalette, k, bx, by);
                        default -> getBlock(blockPalette, bx, k, by);
                    };

                    final Block blockFrom = (switch (face) {
                        case NORTH, SOUTH -> getBlock(otherSection.blockPalette(), bx, by, 15 - k);
                        case WEST, EAST -> getBlock(otherSection.blockPalette(), 15 - k, bx, by);
                        default -> getBlock(otherSection.blockPalette(), bx, 15 - k, by);
                    });

                    if (blockTo == null && blockFrom != null) {
                        if (blockFrom.registry().collisionShape().isOccluded(Block.AIR.registry().collisionShape(), face.getOppositeFace()))
                            continue;
                    } else if (blockTo != null && blockFrom == null) {
                        if (Block.AIR.registry().collisionShape().isOccluded(blockTo.registry().collisionShape(), face))
                            continue;
                    } else if (blockTo != null && blockFrom != null) {
                        if (blockFrom.registry().collisionShape().isOccluded(blockTo.registry().collisionShape(), face.getOppositeFace()))
                            continue;
                    }

                    final int index = posTo | (lightEmission << 12);

                    if (lightEmission > 0) {
                        lightSources.enqueue((short) index);
                    }
                }
            }
        }

        return lightSources;
    }

    @Override
    public void copyFrom(byte @NotNull [] array) {
        if (array.length == 0) this.content = null;
        else this.content = array.clone();
    }

    @Override
    public Light calculateInternal(Instance instance, int chunkX, int sectionY, int chunkZ) {
        Chunk chunk = instance.getChunk(chunkX, chunkZ);
        if (chunk == null) {
            this.toUpdateSet = Set.of();
            return this;
        }
        this.isValidBorders = true;

        // Update single section with base lighting changes
        int queueSize = SECTION_SIZE * SECTION_SIZE * SECTION_SIZE;
        ShortArrayFIFOQueue queue = new ShortArrayFIFOQueue(0);
        if (!fullyLit) {
            queue = buildInternalQueue(chunk, sectionY);
            queueSize = queue.size();
        }

        if (queueSize == SECTION_SIZE * SECTION_SIZE * SECTION_SIZE) {
            this.fullyLit = true;
            this.content = contentFullyLit;
        } else {
            Result result = LightCompute.compute(blockPalette, queue);
            this.content = result.light();
        }

        Set<Point> toUpdate = new HashSet<>();

        // Propagate changes to neighbors and self
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Chunk neighborChunk = instance.getChunk(chunkX + i, chunkZ + j);
                if (neighborChunk == null) continue;

                for (int k = -1; k <= 1; k++) {
                    Vec neighborPos = new Vec(chunkX + i, sectionY + k, chunkZ + j);

                    if (neighborPos.blockY() >= neighborChunk.getMinSection() && neighborPos.blockY() < neighborChunk.getMaxSection()) {
                        toUpdate.add(new Vec(neighborChunk.getChunkX(), neighborPos.blockY(), neighborChunk.getChunkZ()));
                        neighborChunk.getSection(neighborPos.blockY()).skyLight().invalidatePropagation();
                    }
                }
            }
        }

        toUpdate.add(new Vec(chunk.getChunkX(), sectionY, chunk.getChunkZ()));
        this.toUpdateSet = toUpdate;

        return this;
    }

    @Override
    public void invalidate() {
        invalidatePropagation();
    }

    @Override
    public boolean requiresUpdate() {
        return !isValidBorders;
    }

    @Override
    public void set(byte[] copyArray) {
        this.content = copyArray.clone();
    }

    @Override
    public boolean requiresSend() {
        boolean res = needsSend;
        needsSend = false;
        return res;
    }

    private void clearCache() {
        this.contentPropagation = null;
        isValidBorders = true;
        needsSend = true;
        fullyLit = false;
    }

    @Override
    public byte[] array() {
        if (content == null) return new byte[0];
        if (contentPropagation == null) return content;
        var res = bake(contentPropagation, content);
        if (res == emptyContent) return new byte[0];
        return res;
    }

    private boolean compareBorders(byte[] a, byte[] b) {
        if (b == null && a == null) return true;
        if (b == null || a == null) return false;

        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] > b[i]) return false;
        }
        return true;
    }

    @Override
    public Light calculateExternal(Instance instance, Chunk chunk, int sectionY) {
        if (!isValidBorders) clearCache();

        Map<BlockFace, Point> neighbors = Light.getNeighbors(chunk, sectionY);
        Set<Point> toUpdate = new HashSet<>();

        ShortArrayFIFOQueue queue;

        byte[][] borderTemp = bordersFullyLit;
        if (!fullyLit) {
            queue = buildExternalQueue(instance, blockPalette, neighbors, content);
            LightCompute.Result result = LightCompute.compute(blockPalette, queue);

            byte[] contentPropagationTemp = result.light();
            borderTemp = result.borders();
            this.contentPropagationSwap = bake(contentPropagationSwap, contentPropagationTemp);
        } else {
            this.contentPropagationSwap = null;
        }

        // Propagate changes to neighbors and self
        for (var entry : neighbors.entrySet()) {
            var neighbor = entry.getValue();
            var face = entry.getKey();

            byte[] next = borderTemp[face.ordinal()];
            byte[] current = getBorderPropagation(face);

            if (!compareBorders(next, current)) {
                toUpdate.add(neighbor);
            }
        }

        this.toUpdateSet = toUpdate;
        return this;
    }

    private byte[][] combineBorders(byte[][] b1, byte[][] b2) {
        if (b1 == null) return b2;

        byte[][] newBorder = new byte[FACES.length][];
        Arrays.setAll(newBorder, i -> new byte[SIDE_LENGTH]);
        for (int i = 0; i < FACES.length; i++) {
            newBorder[i] = combineBorders(b1[i], b2[i]);
        }
        return newBorder;
    }

    private byte[] bake(byte[] content1, byte[] content2) {
        if (content1 == null && content2 == null) return emptyContent;
        if (content1 == emptyContent && content2 == emptyContent) return emptyContent;

        if (content1 == null) return content2;
        if (content2 == null) return content1;

        byte[] lightMax = new byte[LIGHT_LENGTH];
        for (int i = 0; i < content1.length; i++) {
            // Lower
            byte l1 = (byte) (content1[i] & 0x0F);
            byte l2 = (byte) (content2[i] & 0x0F);

            // Upper
            byte u1 = (byte) ((content1[i] >> 4) & 0x0F);
            byte u2 = (byte) ((content2[i] >> 4) & 0x0F);

            byte lower = (byte) Math.max(l1, l2);
            byte upper = (byte) Math.max(u1, u2);

            lightMax[i] = (byte) (lower | (upper << 4));
        }
        return lightMax;
    }

    private static byte[] computeBorders(byte[] content, BlockFace face) {
        byte[] border = new byte[SECTION_SIZE * SECTION_SIZE];

        final int k = switch (face) {
            case WEST, BOTTOM, NORTH -> 0;
            case EAST, TOP, SOUTH -> 15;
        };

        for (int bx = 0; bx < SECTION_SIZE; ++bx) {
            for (int by = 0; by < SECTION_SIZE; ++by) {
                final int posTo = switch (face) {
                    case NORTH, SOUTH -> bx | (k << 4) | (by << 8);
                    case WEST, EAST -> k | (by << 4) | (bx << 8);
                    default -> bx | (by << 4) | (k << 8);
                };

                border[bx * SECTION_SIZE + by] = (byte) (Math.max(getLight(content, posTo) - 1, 0));
            }
        }

        return border;
    }

    @Override
    public byte[] getBorderPropagation(BlockFace face) {
        if (!isValidBorders) clearCache();

        if (content == null && contentPropagation == null) return new byte[SIDE_LENGTH];
        if (content == null) return computeBorders(contentPropagation, face);
        if (contentPropagation == null) return computeBorders(content, face);

        return combineBorders(computeBorders(contentPropagation, face), computeBorders(content, face));
    }

    @Override
    public void invalidatePropagation() {
        this.isValidBorders = false;
        this.needsSend = false;
        this.contentPropagation = null;
    }

    @Override
    public int getLevel(int x, int y, int z) {
        var array = array();
        int index = x | (z << 4) | (y << 8);
        return LightCompute.getLight(array, index);
    }

    private byte[] combineBorders(byte[] b1, byte[] b2) {
        byte[] newBorder = new byte[SIDE_LENGTH];
        for (int i = 0; i < newBorder.length; i++) {
            var previous = b2[i];
            var current = b1[i];
            newBorder[i] = (byte) Math.max(previous, current);
        }
        return newBorder;
    }
}