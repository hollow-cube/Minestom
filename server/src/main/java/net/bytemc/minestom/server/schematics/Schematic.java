package net.bytemc.minestom.server.schematics;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jglrxavpok.hephaistos.nbt.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Schematic {

    private final Map<Vec, Block> blocks = new HashMap<>();

    public void addBlock(Vec pos, Block block) {
        this.blocks.put(pos, block);
    }

    public void save(File file) {
        var level = NBT.Compound(root -> {
            var compounds = new ArrayList<NBTCompound>();
            for (Vec vec : blocks.keySet()) {
                var block = blocks.get(vec);
                compounds.add(NBT.Compound(it -> {
                    it.setInt("x", vec.blockX()).setInt("y", vec.blockY()).setInt("z", vec.blockZ()).setString("block", block.namespace().namespace());
                    it.set("hasData", NBT.Boolean(block.nbt() != null));
                    if (block.nbt() != null) {
                        it.set("data", block.nbt());
                    }
                }));
            }
            root.set("blocks", NBT.List(NBTType.TAG_Compound, compounds));
        });
        try (NBTWriter writer = new NBTWriter(file, CompressedProcesser.GZIP)) {
            writer.writeNamed("", level);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void paste(Pos pos, Instance instance) {
        for (var vec : blocks.keySet()) {
            instance.setBlock(pos.add(vec), blocks.get(vec));
        }
    }
}