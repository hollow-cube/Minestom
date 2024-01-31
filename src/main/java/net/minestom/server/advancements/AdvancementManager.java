package net.minestom.server.advancements;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Used to manage all the registered {@link AdvancementTab}.
 * <p>
 * Use {@link #createTab(String, AdvancementRoot)} to create a tab with the appropriate {@link AdvancementRoot}.
 */
public interface AdvancementManager {

    /**
     * Creates a new {@link AdvancementTab} with a single {@link AdvancementRoot}.
     *
     * @param rootIdentifier the root identifier
     * @param root           the root advancement
     * @return the newly created {@link AdvancementTab}
     * @throws IllegalStateException if a tab with the identifier {@code rootIdentifier} already exists
     */
    @NotNull AdvancementTab createTab(@NotNull String rootIdentifier, @NotNull AdvancementRoot root);

    /**
     * Gets an advancement tab by its root identifier.
     *
     * @param rootIdentifier the root identifier of the tab
     * @return the {@link AdvancementTab} associated with the identifier, null if not any
     */
    @Nullable AdvancementTab getTab(@NotNull String rootIdentifier);

    /**
     * Gets all the created {@link AdvancementTab}.
     *
     * @return the collection containing all created {@link AdvancementTab}
     */
    @NotNull Collection<AdvancementTab> getTabs();
}
