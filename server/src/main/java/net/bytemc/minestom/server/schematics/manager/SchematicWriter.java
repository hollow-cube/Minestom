package net.bytemc.minestom.server.schematics.manager;

import net.bytemc.minestom.server.schematics.Rotation;
import net.bytemc.minestom.server.schematics.Schematic;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTWriter;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.io.IOException;
import java.nio.file.Path;

public final class SchematicWriter {

    public static void write(@NotNull Schematic schematic, @NotNull Path schemPath) throws IOException {
        MutableNBTCompound schematicNBT = new MutableNBTCompound();
        Point size = schematic.size(Rotation.NONE);
        schematicNBT.setShort("Width", (short) size.x());
        schematicNBT.setShort("Height", (short) size.y());
        schematicNBT.setShort("Length", (short) size.z());

        Point offset = schematic.offset(Rotation.NONE);
        NBTCompound schematicMetadata = NBT.Compound(root -> {
            root.setInt("WEOffsetX", offset.blockX());
            root.setInt("WEOffsetY", offset.blockY());
            root.setInt("WEOffsetZ", offset.blockZ());
        });

        schematicNBT.set("Metadata", schematicMetadata);

        schematicNBT.setByteArray("BlockData", schematic.blocks());
        Block[] blocks = schematic.palette();

        schematicNBT.setInt("PaletteMax", blocks.length);

        NBTCompound palette = NBT.Compound(root -> {
            for (int i = 0; i < blocks.length; i++) {
                root.setInt(blocks[i].name(), i);
            }
        });
        schematicNBT.set("Palette", palette);

        try (NBTWriter writer = new NBTWriter(schemPath)) {
            writer.writeRaw(schematicNBT.toCompound());
        }
    }
}

