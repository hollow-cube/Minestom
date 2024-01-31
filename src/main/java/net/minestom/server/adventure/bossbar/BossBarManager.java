package net.minestom.server.adventure.bossbar;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Manages all boss bars known to this Minestom instance. Although this class can be used
 * to show boss bars to players, it is preferable to use the boss bar methods in the
 * {@link Audience} class instead.
 *
 * <p>This implementation is heavily based on
 * <a href="https://github.com/VelocityPowered/Velocity">Velocity</a>'s boss bar
 * management system.</p>
 *
 * @see Audience#showBossBar(BossBar)
 * @see Audience#hideBossBar(BossBar)
 */
public interface BossBarManager {

    /**
     * Adds the specified player to the boss bar's viewers and spawns the boss bar, registering the
     * boss bar if needed.
     *
     * @param player the intended viewer
     * @param bar    the boss bar to show
     */
    void addBossBar(@NotNull Player player, @NotNull BossBar bar);

    /**
     * Removes the specified player from the boss bar's viewers and despawns the boss bar.
     *
     * @param player the intended viewer
     * @param bar    the boss bar to hide
     */
    void removeBossBar(@NotNull Player player, @NotNull BossBar bar);

    /**
     * Adds the specified players to the boss bar's viewers and spawns the boss bar, registering the
     * boss bar if needed.
     *
     * @param players the players
     * @param bar     the boss bar
     */
    void addBossBar(@NotNull Collection<Player> players, @NotNull BossBar bar);

    /**
     * Removes the specified players from the boss bar's viewers and despawns the boss bar.
     *
     * @param players the intended viewers
     * @param bar     the boss bar to hide
     */
    void removeBossBar(@NotNull Collection<Player> players, @NotNull BossBar bar);

    /**
     * Completely destroys a boss bar, removing it from all players.
     *
     * @param bossBar the boss bar
     */
    void destroyBossBar(@NotNull BossBar bossBar);

    /**
     * Removes a player from all of their boss bars. Note that this method does not
     * send any removal packets to the player. It is meant to be used when a player is
     * disconnecting from the server.
     *
     * @param player the player
     */
    void removeAllBossBars(@NotNull Player player);

    /**
     * Gets a collection of all boss bars currently visible to a given player.
     *
     * @param player the player
     * @return the boss bars
     */
    @NotNull Collection<BossBar> getPlayerBossBars(@NotNull Player player);

    /**
     * Gets all the players for whom the given boss bar is currently visible.
     *
     * @param bossBar the boss bar
     * @return the players
     */
    @NotNull Collection<Player> getBossBarViewers(@NotNull BossBar bossBar);
}
