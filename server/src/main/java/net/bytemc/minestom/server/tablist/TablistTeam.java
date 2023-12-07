package net.bytemc.minestom.server.tablist;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.scoreboard.TeamBuilder;
import net.minestom.server.scoreboard.TeamManager;

import java.util.function.Predicate;

public class TablistTeam {

    private final String name;
    private final int id;
    private final String prefix;
    private final NamedTextColor textColor;
    private final Predicate<Player> predicate;
    private Team team;

    public TablistTeam(String name, int id, String prefix, NamedTextColor textColor, Predicate<Player> predicate) {
        this.name = name;
        this.id = id;
        this.prefix = prefix;
        this.textColor = textColor;
        this.predicate = predicate;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getPrefix() {
        return prefix;
    }

    public NamedTextColor getTextColor() {
        return textColor;
    }

    public Predicate<Player> getPredicate() {
        return predicate;
    }

    public Team getTeam() {
        return team;
    }

    public void toTeam(TeamManager teamManager) {
        this.team = new TeamBuilder(name, teamManager).updateTeamColor(textColor).updatePrefix(Component.text(prefix)).collisionRule(TeamsPacket.CollisionRule.NEVER).build();
    }

}
