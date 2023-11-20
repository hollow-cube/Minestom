package net.bytemc.minestom.server.particle;

import net.minestom.server.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractParticleQueue {
    private final ParticleBuilder builder;
    private final List<Player> players;

    private int count;

    public AbstractParticleQueue(ParticleBuilder builder) {
        this.builder = builder;
        this.players = new ArrayList<>();
    }

    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public void addPlayers(Collection<Player> collection) {
        this.players.addAll(collection);
    }

    public ParticleBuilder getBuilder() {
        return builder;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public abstract void tick();
}
