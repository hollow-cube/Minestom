package net.minestom.server.adventure.audience;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public interface AudienceManager {
    @NotNull AudienceProvider<Audience> single();

    @NotNull AudienceProvider<Iterable<? extends Audience>> iterable();

    @NotNull Audience all();

    @NotNull Audience players();

    @NotNull Audience players(@NotNull Predicate<Player> filter);

    @NotNull Audience console();

    @NotNull Audience server();

    @NotNull Audience customs();

    @NotNull Audience custom(@NotNull Keyed keyed);

    @NotNull Audience custom(@NotNull Key key);

    @NotNull Audience custom(@NotNull Keyed keyed, Predicate<Audience> filter);

    @NotNull Audience custom(@NotNull Key key, Predicate<Audience> filter);

    @NotNull Audience customs(@NotNull Predicate<Audience> filter);

    @NotNull Audience all(@NotNull Predicate<Audience> filter);

    @NotNull AudienceRegistry registry();
}
