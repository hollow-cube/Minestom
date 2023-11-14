package net.bytemc.minestom.server.schematics.manager;

import net.bytemc.minestom.server.schematics.Schematic;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentBlockState;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.collections.ImmutableByteArray;
import org.jglrxavpok.hephaistos.nbt.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public final class SchematicReader {

    public static Schematic read(@NotNull InputStream stream) throws IOException {
        try (var reader = new NBTReader(stream, CompressedProcesser.GZIP)) {
            return read(reader);
        }
    }

    public static Schematic read(@NotNull Path path) throws IOException {
        try (var reader = new NBTReader(path, CompressedProcesser.GZIP)) {
            return read(reader);
        }
    }

    public static Schematic read(@NotNull NBTReader reader) throws IOException {
        try {
            NBTCompound tag = (NBTCompound) reader.read();

            Short width = tag.getShort("Width");
            Short height = tag.getShort("Height");
            Short length = tag.getShort("Length");

            NBTCompound metadata = tag.getCompound("Metadata");
            Integer offsetX = metadata.getInt("WEOffsetX");
            Integer offsetY = metadata.getInt("WEOffsetY");
            Integer offsetZ = metadata.getInt("WEOffsetZ");

            NBTCompound palette = tag.getCompound("Palette");
            ImmutableByteArray blockArray = tag.getByteArray("BlockData");

            Integer paletteSize = tag.getInt("PaletteMax");
            Block[] paletteBlocks = new Block[paletteSize];

            ArgumentBlockState state = new ArgumentBlockState("");
            palette.forEach((key, value) -> {
                int assigned = ((NBTInt) value).getValue();
                Block block = state.staticParse(key);
                paletteBlocks[assigned] = block;
            });

            return new Schematic(
                    new Vec(width, height, length),
                    new Vec(offsetX, offsetY, offsetZ),
                    paletteBlocks,
                    blockArray.copyArray()
            );
        } catch (NullPointerException | NBTException e) {
            e.printStackTrace();
            return null;
        }
    }
}

