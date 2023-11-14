package net.minestom.server.entity.playerspecific;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

final class Helper extends Entity {
    public Helper(@NotNull Entity entity) {
        super(entity.getEntityType(), entity.getUuid());
    }

    Metadata getMetadata() {
        return metadata;
    }
}
