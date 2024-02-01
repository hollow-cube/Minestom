package net.minestom.server.gamedata.tags;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Handles loading and caching of tags.
 */
public interface TagManager {
    @Nullable Tag getTag(Tag.BasicType type, String namespace);

    Map<Tag.BasicType, List<Tag>> getTagMap();
}
