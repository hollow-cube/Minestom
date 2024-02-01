package net.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerSettings;
import net.minestom.server.tag.Tag;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.DimensionTypeManagerImpl;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InstanceContainerTest {

    static {
        new DimensionTypeManagerImpl().addDimension(DimensionType.OVERWORLD);
    }

    @Test
    public void copyPreservesTag() {
        MinecraftServer minecraftServer = MinecraftServer.of(ServerSettings.builder().build());
        var tag = Tag.String("test");
        var instance = new InstanceContainer(minecraftServer, UUID.randomUUID(), DimensionType.OVERWORLD);
        instance.setTag(tag, "123");

        var copyInstance = instance.copy();
        var result = copyInstance.getTag(tag);
        assertEquals("123", result);
    }
}
