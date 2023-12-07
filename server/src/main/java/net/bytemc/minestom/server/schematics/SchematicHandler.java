package net.bytemc.minestom.server.schematics;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.*;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

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
        Schematic schematic = new Schematic();
        try (var reader = new NBTReader(file.toPath(), CompressedProcesser.GZIP)) {
            NBTCompound tag = (NBTCompound) reader.read();

            for (NBT block : Objects.requireNonNull(tag.getList("blocks"))) {
                var compound = (NBTCompound) block;
                var x = compound.getInt("x");
                var y = compound.getInt("y");
                var z = compound.getInt("z");
                var name = Block.fromNamespaceId(compound.getString("block"));
                var hasData = compound.getBoolean("hasData");
                if(hasData) {
                    var data = compound.getCompound("data");
                    schematic.addBlock(new Vec(x, y, z), name.withNbt(data));
                } else {
                    schematic.addBlock(new Vec(x, y, z), name);
                }
            }
        } catch (IOException | NBTException e) {
            throw new RuntimeException(e);
        }
        return schematic;
    }
}
