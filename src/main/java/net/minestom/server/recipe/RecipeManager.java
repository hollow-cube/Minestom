package net.minestom.server.recipe;

import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface RecipeManager {
    void addRecipes(@NotNull Recipe... recipe);

    void addRecipe(@NotNull Recipe recipe);

    void removeRecipe(@NotNull Recipe recipe);

    @NotNull Set<Recipe> getRecipes();

    @NotNull DeclareRecipesPacket getDeclareRecipesPacket();
}
