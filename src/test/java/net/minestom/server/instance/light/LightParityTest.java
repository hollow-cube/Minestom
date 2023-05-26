package net.minestom.server.instance.light;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.jglrxavpok.hephaistos.mca.AnvilException;
import org.jglrxavpok.hephaistos.mca.BlockState;
import org.jglrxavpok.hephaistos.mca.ChunkSection;
import org.jglrxavpok.hephaistos.mca.RegionFile;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

@EnvTest
public class LightParityTest {

    // TODO: This test is currently disabled because the tested vanilla world has incorrect lighting.
    // When a vanilla world with valid lighting is found it can be put here
    @Test
    public void test(Env env) throws URISyntaxException, IOException, AnvilException {
        Map<Vec, SectionEntry> sections = retrieveSections();
        System.out.println("Loaded chunks");
        // Generate our own light
        Map<Vec, LightCompute.Result> results = new HashMap<>();
        for (var entry : sections.entrySet()) {
            var vec = entry.getKey();
            var palette = entry.getValue().blocks;
            results.put(vec, LightCompute.compute(palette));
        }

        System.out.println("Relighting done");

        int differences = 0;
        int differencesZero = 0;
        int blocks = 0;

        Set<Vec> invalidVec = new HashSet<>();

        for (var result : results.entrySet()) {
            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < 16; ++y) {
                    for (int z = 0; z < 16; ++z) {
                        int index = x | (z << 4) | (y << 8);

                        var mca = sections.get(result.getKey());

                        int serverValue = LightCompute.getLight(result.getValue().light(), index);
                        int mcaValue = mca.block.length == 0 ? 0 : LightCompute.getLight(mca.block, index);

                        if (serverValue != mcaValue) {
                            if (serverValue == 0) {
                                differencesZero++;
                            } else {
                                differences++;
                                System.out.println("Difference at " + (x + result.getKey().x() * 16) + " " + y + " " + (z + result.getKey().z() * 16) + " in chunk " + result.getKey().x() + " " + result.getKey().z() + " section " + result.getKey().y() + " server: " + serverValue + " mca: " + mcaValue);
                            }
                            blocks++;
                        }
                    }
                }
            }
        }

        System.out.println("Differences: " + differences);
        System.out.println("DifferencesZero: " + differencesZero);
        System.out.println("Blocks: " + blocks);

        for (Vec chunk : invalidVec) {
            System.out.println("Invalid chunk: " + chunk.blockX() + ", " + chunk.blockY() + ", " + chunk.blockZ());
        }
    }

    record SectionEntry(Palette blocks, byte[] sky, byte[] block) {
    }

    private static Map<Vec, SectionEntry> retrieveSections() throws IOException, URISyntaxException, AnvilException {
        URL defaultImage = LightParityTest.class.getResource("/net/minestom/server/instance/lighting/r.0.0.mca");
        assert defaultImage != null;
        File imageFile = new File(defaultImage.toURI());
        var regionFile = new RegionFile(new RandomAccessFile(imageFile, "rw"),
                0, 0, -64, 384);

        Map<Vec, SectionEntry> sections = new HashMap<>();
        // Read from anvil
        // TODO: read all 32x32 chunks
        for (int x = 1; x < 31; x++) {
            for (int z = 1; z < 31; z++) {
                var chunk = regionFile.getChunk(x, z);
                if (chunk == null) continue;

                for (int yLevel = chunk.getMinY(); yLevel <= chunk.getMaxY(); yLevel += 16) {
                    var section = chunk.getSection((byte) (yLevel/16));
                    var palette = loadBlocks(section);
                    var sky = section.getSkyLights();
                    var block = section.getBlockLights();
                    sections.put(new Vec(x, section.getY(), z), new SectionEntry(palette, sky, block));
                }
            }
        }
        return sections;
    }

    private static Palette loadBlocks(ChunkSection section) throws AnvilException {
        var palette = Palette.blocks();
        for (int x = 0; x < Chunk.CHUNK_SECTION_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SECTION_SIZE; z++) {
                for (int y = 0; y < Chunk.CHUNK_SECTION_SIZE; y++) {
                    final BlockState blockState = section.get(x, y, z);
                    final String blockName = blockState.getName();
                    Block block = Objects.requireNonNull(Block.fromNamespaceId(blockName))
                            .withProperties(blockState.getProperties());
                    palette.set(x, y, z, block.stateId());
                }
            }
        }
        return palette;
    }
}
