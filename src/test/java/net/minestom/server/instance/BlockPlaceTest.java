package net.minestom.server.instance;

import net.minestom.server.Env;
import net.minestom.server.EnvTest;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@EnvTest
public class BlockPlaceTest {

    @Test
    void testPlacementOutOfLimit(Env env) {
        Instance instance = env.createFlatInstance();
        assertDoesNotThrow(() -> instance.setBlock(0, instance.getDimensionType().getMaxY() + 1, 0, Block.STONE));
        assertDoesNotThrow(() -> instance.setBlock(0, instance.getDimensionType().getMinY() - 1, 0, Block.STONE));
    }
}
