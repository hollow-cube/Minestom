package net.minestom.server.scoreboard;

import lombok.RequiredArgsConstructor;
import net.minestom.server.ServerSettings;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.UniqueIdUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@RequiredArgsConstructor
public final class TeamManagerImpl implements TeamManager {

    /**
     * Represents all registered teams
     */
    private final Set<Team> teams = new CopyOnWriteArraySet<>();

    private final ServerSettings serverSettings;

    /**
     * Registers a new {@link Team}
     *
     * @param team The team to be registered
     */
    void registerNewTeam(@NotNull Team team, ConnectionManager connectionManager) {
        this.teams.add(team);
        PacketUtils.broadcastPlayPacket(connectionManager, serverSettings, team.createTeamsCreationPacket());
    }

    @Override
    public boolean deleteTeam(@NotNull Team team, ConnectionManager connectionManager) {
        // Sends to all online players a team destroy packet
        PacketUtils.broadcastPlayPacket(connectionManager, serverSettings, team.createTeamDestructionPacket());
        return this.teams.remove(team);
    }


    @Override
    public TeamBuilder createBuilder(@NotNull String name, ConnectionManager connectionManager) {
        return new TeamBuilder(connectionManager, serverSettings, name, this);
    }

    @Override
    public Team getTeam(String teamName) {
        for (Team team : this.teams) {
            if (team.getTeamName().equals(teamName)) return team;
        }
        return null;
    }

    @Override
    public List<String> getPlayers(Team team) {
        List<String> players = new ArrayList<>();
        for (String member : team.getMembers()) {
            boolean match = UniqueIdUtils.isUniqueId(member);

            if (!match) players.add(member);
        }
        return players;
    }

    @Override
    public List<String> getEntities(Team team) {
        List<String> entities = new ArrayList<>();
        for (String member : team.getMembers()) {
            boolean match = UniqueIdUtils.isUniqueId(member);

            if (match) entities.add(member);
        }
        return entities;
    }

    @Override
    public Set<Team> getTeams() {
        return this.teams;
    }
}
