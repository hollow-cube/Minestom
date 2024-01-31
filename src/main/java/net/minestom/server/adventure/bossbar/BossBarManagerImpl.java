package net.minestom.server.adventure.bossbar;

import net.kyori.adventure.bossbar.BossBar;
import net.minestom.server.ServerSettingsProvider;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BossBarManagerImpl implements BossBarManager {
    private final BossBarListener listener;
    private final Map<UUID, Set<BossBarHolder>> playerBars = new ConcurrentHashMap<>();
    final Map<BossBar, BossBarHolder> bars = new ConcurrentHashMap<>();
    private final ServerSettingsProvider serverSettingsProvider;

    /**
     * Creates a new boss bar manager.
     */
    public BossBarManagerImpl(ServerSettingsProvider serverSettingsProvider) {
        this.serverSettingsProvider = serverSettingsProvider;
        this.listener = new BossBarListener(serverSettingsProvider,this);
    }

    @Override
    public void addBossBar(@NotNull Player player, @NotNull BossBar bar) {
        BossBarHolder holder = this.getOrCreateHandler(bar);
        if (holder.addViewer(player)) {
            player.sendPacket(holder.createAddPacket());
            this.playerBars.computeIfAbsent(player.getUuid(), uuid -> new HashSet<>()).add(holder);
        }
    }

    @Override
    public void removeBossBar(@NotNull Player player, @NotNull BossBar bar) {
        BossBarHolder holder = this.bars.get(bar);
        if (holder != null && holder.removeViewer(player)) {
            player.sendPacket(holder.createRemovePacket());
            this.removePlayer(player, holder);
        }
    }

    @Override
    public void addBossBar(@NotNull Collection<Player> players, @NotNull BossBar bar) {
        BossBarHolder holder = this.getOrCreateHandler(bar);
        Collection<Player> addedPlayers = players.stream().filter(holder::addViewer).toList();
        if (!addedPlayers.isEmpty()) {
            PacketUtils.sendGroupedPacket(serverSettingsProvider, addedPlayers, holder.createAddPacket());
        }
    }

    @Override
    public void removeBossBar(@NotNull Collection<Player> players, @NotNull BossBar bar) {
        BossBarHolder holder = this.bars.get(bar);
        if (holder != null) {
            Collection<Player> removedPlayers = players.stream().filter(holder::removeViewer).toList();
            if (!removedPlayers.isEmpty()) {
                PacketUtils.sendGroupedPacket(serverSettingsProvider, removedPlayers, holder.createRemovePacket());
            }
        }
    }

    @Override
    public void destroyBossBar(@NotNull BossBar bossBar) {
        BossBarHolder holder = this.bars.remove(bossBar);
        if (holder != null) {
            PacketUtils.sendGroupedPacket(serverSettingsProvider, holder.players, holder.createRemovePacket());
            for (Player player : holder.players) {
                this.removePlayer(player, holder);
            }
        }
    }

    @Override
    public void removeAllBossBars(@NotNull Player player) {
        Set<BossBarHolder> holders = this.playerBars.remove(player.getUuid());
        if (holders != null) {
            for (BossBarHolder holder : holders) {
                holder.removeViewer(player);
            }
        }
    }

    @Override
    public @NotNull Collection<BossBar> getPlayerBossBars(@NotNull Player player) {
        Collection<BossBarHolder> holders = this.playerBars.get(player.getUuid());
        return holders != null ?
                holders.stream().map(holder -> holder.bar).toList() : List.of();
    }

    @Override
    public @NotNull Collection<Player> getBossBarViewers(@NotNull BossBar bossBar) {
        BossBarHolder holder = this.bars.get(bossBar);
        return holder != null ?
                Collections.unmodifiableCollection(holder.players) : List.of();
    }

    /**
     * Gets or creates a handler for this bar.
     *
     * @param bar the bar
     * @return the handler
     */
    private @NotNull BossBarHolder getOrCreateHandler(@NotNull BossBar bar) {
        return this.bars.computeIfAbsent(bar, bossBar -> {
            BossBarHolder holder = new BossBarHolder(bossBar);
            bossBar.addListener(this.listener);
            return holder;
        });
    }

    private void removePlayer(Player player, BossBarHolder holder) {
        Set<BossBarHolder> holders = this.playerBars.get(player.getUuid());
        if (holders != null) {
            holders.remove(holder);
            if (holders.isEmpty()) {
                this.playerBars.remove(player.getUuid());
            }
        }
    }
}
