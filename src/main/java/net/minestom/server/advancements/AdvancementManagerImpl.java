package net.minestom.server.advancements;

import net.minestom.server.ServerSettings;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AdvancementManagerImpl implements AdvancementManager {
    private final ServerSettings serverSettings;

    // root identifier = its advancement tab
    private final Map<String, AdvancementTab> advancementTabMap = new ConcurrentHashMap<>();

    public AdvancementManagerImpl(ServerSettings serverSettings) {
        this.serverSettings = serverSettings;
    }

    @Override
    @NotNull
    public AdvancementTab createTab(@NotNull String rootIdentifier, @NotNull AdvancementRoot root) {
        Check.stateCondition(advancementTabMap.containsKey(rootIdentifier),
                "A tab with the identifier '" + rootIdentifier + "' already exists");
        final AdvancementTab advancementTab = new AdvancementTab(serverSettings, rootIdentifier, root);
        this.advancementTabMap.put(rootIdentifier, advancementTab);
        return advancementTab;
    }

    @Override
    @Nullable
    public AdvancementTab getTab(@NotNull String rootIdentifier) {
        return advancementTabMap.get(rootIdentifier);
    }

    @Override
    @NotNull
    public Collection<AdvancementTab> getTabs() {
        return advancementTabMap.values();
    }
}
