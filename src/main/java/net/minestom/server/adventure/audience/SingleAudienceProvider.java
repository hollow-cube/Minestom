package net.minestom.server.adventure.audience;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.minestom.server.ServerProcess;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * A provider of audiences. For complex returns, this instance is backed by
 * {@link IterableAudienceProvider}.
 */
class SingleAudienceProvider implements AudienceProvider<Audience> {

    protected final IterableAudienceProvider collection;
    protected final Audience players;
    protected final Audience server;
    @NotNull
    private final ServerProcess serverProcess;

    protected SingleAudienceProvider(ServerProcess serverProcess, ConnectionManager connectionManager, CommandManager commandManager) {
        this.serverProcess = serverProcess;
        this.collection = new IterableAudienceProvider(serverProcess, commandManager);
        this.players = PacketGroupingAudience.of(serverProcess, connectionManager.getOnlinePlayers());
        this.server = Audience.audience(this.players, commandManager.getConsoleSender());
    }

    /**
     * Gets the {@link IterableAudienceProvider} instance.
     *
     * @return the instance
     */
    public @NotNull IterableAudienceProvider iterable() {
        return this.collection;
    }

    @Override
    public @NotNull Audience all() {
        return Audience.audience(this.server, this.customs());
    }

    @Override
    public @NotNull Audience players() {
        return this.players;
    }

    @Override
    public @NotNull Audience players(@NotNull Predicate<Player> filter) {
        return PacketGroupingAudience.of(serverProcess, serverProcess.getConnectionManager().getOnlinePlayers().stream().filter(filter).toList());
    }

    @Override
    public @NotNull Audience console() {
        return serverProcess.getCommandManager().getConsoleSender();
    }

    @Override
    public @NotNull Audience server() {
        return this.server;
    }

    @Override
    public @NotNull Audience customs() {
        return Audience.audience(this.iterable().customs());
    }

    @Override
    public @NotNull Audience custom(@NotNull Key key) {
        return Audience.audience(this.iterable().custom(key));
    }

    @Override
    public @NotNull Audience custom(@NotNull Key key, Predicate<Audience> filter) {
        return Audience.audience(this.iterable().custom(key, filter));
    }

    @Override
    public @NotNull Audience customs(@NotNull Predicate<Audience> filter) {
        return Audience.audience(this.iterable().customs(filter));
    }

    @Override
    public @NotNull Audience all(@NotNull Predicate<Audience> filter) {
        return Audience.audience(this.iterable().all(filter));
    }

    @Override
    public @NotNull AudienceRegistry registry() {
        return this.iterable().registry();
    }
}
