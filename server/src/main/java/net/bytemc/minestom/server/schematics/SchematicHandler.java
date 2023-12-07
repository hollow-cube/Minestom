package net.bytemc.minestom.server.schematics;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class SchematicHandler {

    public static Schematic scan(Instance instance, @NotNull Pos pos, @NotNull Pos pos2, Pos copyPosition) {
        var schematic = new Schematic();
        for (int x = Math.min(pos.blockX(), pos2.blockX()); x <= Math.max(pos.blockX(), pos2.blockX()); x++) {
            for (int z = Math.min(pos.blockZ(), pos2.blockZ()); z <= Math.max(pos.blockZ(), pos2.blockZ()); z++) {
                for (int y = Math.min(pos.blockY(), pos2.blockY()); y <= Math.max(pos.blockY(), pos2.blockY()); y++) {
                    var block = instance.getBlock(x, y, z, Block.Getter.Condition.TYPE);
                    if (block != null && !block.isAir()) {
                        schematic.addBlock(new Vec(x, y, z).sub(copyPosition), block);
                    }
                }
            }
        }
        return schematic;
    }

    public static Schematic read(File file) {
        //TODO
        return new Schematic();
    }

}
