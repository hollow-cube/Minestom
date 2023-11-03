package net.minestom.server.instance.generator;

import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class FlatGenerator implements Generator {

    @Override
    public void generate(@NotNull GenerationUnit unit) {
        unit.modifier().fillHeight(-1,0, Block.GRASS_BLOCK);
    }
}
