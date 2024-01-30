package net.minestom.server.instance;

import net.minestom.server.ServerProcess;
import net.minestom.server.ServerSettings;
import net.minestom.server.tag.Tag;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.DimensionTypeManager;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InstanceContainerTest {

    static {
        new DimensionTypeManager().addDimension(DimensionType.OVERWORLD);
    }

    @Test
    public void copyPreservesTag() {
        ServerProcess serverProcess = ServerProcess.of(ServerSettings.builder().build());
        var tag = Tag.String("test");
        var instance = new InstanceContainer(serverProcess, UUID.randomUUID(), DimensionType.OVERWORLD);
        instance.setTag(tag, "123");

        var copyInstance = instance.copy();
        var result = copyInstance.getTag(tag);
        assertEquals("123", result);
    }
}
