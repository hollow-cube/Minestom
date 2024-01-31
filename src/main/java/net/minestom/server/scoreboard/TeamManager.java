package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * An object which manages all the {@link Team}'s
 */
public interface TeamManager {

    /**
     * Deletes a {@link Team}
     *
     * @param team The team to be deleted
     * @return {@code true} if the team was deleted, otherwise {@code false}
     */
    boolean deleteTeam(@NotNull Team team, ConnectionManager connectionManager);

    /**
     * Deletes a {@link Team}
     *
     * @param registryName The registry name of team
     * @return {@code true} if the team was deleted, otherwise {@code false}
     */
    default boolean deleteTeam(@NotNull String registryName, ConnectionManager connectionManager) {
        Team team = this.getTeam(registryName);
        if (team == null) return false;
        return this.deleteTeam(team, connectionManager);
    }

    /**
     * Initializes a new {@link TeamBuilder} for creating a team
     *
     * @param name The registry name of the team
     * @return the team builder
     */
    TeamBuilder createBuilder(@NotNull String name, ConnectionManager connectionManager);

    /**
     * Creates a {@link Team} with only the registry name
     *
     * @param name The registry name
     * @return the created {@link Team}
     */
    default Team createTeam(@NotNull String name, ConnectionManager connectionManager) {
        return this.createBuilder(name, connectionManager).build();
    }

    /**
     * Creates a {@link Team} with the registry name, prefix, suffix and the team format
     *
     * @param name      The registry name
     * @param prefix    The team prefix
     * @param teamColor The team format
     * @param suffix    The team suffix
     * @return the created {@link Team} with a prefix, teamColor and suffix
     */
    default Team createTeam(String name, Component prefix, NamedTextColor teamColor, Component suffix, ConnectionManager connectionManager) {
        return this.createBuilder(name, connectionManager).prefix(prefix).teamColor(teamColor).suffix(suffix).updateTeamPacket().build();
    }

    /**
     * Creates a {@link Team} with the registry name, display name, prefix, suffix and the team colro
     *
     * @param name        The registry name
     * @param displayName The display name
     * @param prefix      The team prefix
     * @param teamColor   The team color
     * @param suffix      The team suffix
     * @return the created {@link Team} with a prefix, teamColor, suffix and the display name
     */
    default Team createTeam(String name, Component displayName, Component prefix, NamedTextColor teamColor, Component suffix, ConnectionManager connectionManager) {
        return this.createBuilder(name, connectionManager).teamDisplayName(displayName).prefix(prefix).teamColor(teamColor).suffix(suffix).updateTeamPacket().build();
    }

    /**
     * Gets a {@link Team} with the given name
     *
     * @param teamName The registry name of the team
     * @return a registered {@link Team} or {@code null}
     */
    Team getTeam(String teamName);

    /**
     * Checks if the given name a registry name of a registered {@link Team}
     *
     * @param teamName The name of the team
     * @return {@code true} if the team is registered, otherwise {@code false}
     */
    default boolean exists(String teamName) {
        return getTeam(teamName) != null;
    }

    /**
     * Checks if the given {@link Team} registered
     *
     * @param team The searched team
     * @return {@code true} if the team is registered, otherwise {@code false}
     */
    default boolean exists(Team team) {
        return this.exists(team.getTeamName());
    }

    /**
     * Gets a {@link List} with all registered {@link Player} in the team
     * <br>
     * <b>Note:</b> The list exclude all entities. To get all entities of the team, you can use {@link #getEntities(Team)}
     *
     * @param team The team
     * @return a {@link List} with all registered {@link Player}
     */
    List<String> getPlayers(Team team);

    /**
     * Gets a {@link List} with all registered {@link LivingEntity} in the team
     * <br>
     * <b>Note:</b> The list exclude all players. To get all players of the team, you can use {@link #getPlayers(Team)}
     *
     * @param team The team
     * @return a {@link List} with all registered {@link LivingEntity}
     */
    List<String> getEntities(Team team);

    /**
     * Gets a {@link Set} with all registered {@link Team}'s
     *
     * @return a {@link Set} with all registered {@link Team}'s
     */
    Set<Team> getTeams();
}
