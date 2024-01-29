package net.minestom.server.adventure.audience;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
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
    private final MinecraftServer minecraftServer;

    protected SingleAudienceProvider(MinecraftServer minecraftServer) {
        this.minecraftServer = minecraftServer;
        this.collection = new IterableAudienceProvider(minecraftServer);
        this.players = PacketGroupingAudience.of(minecraftServer, minecraftServer.process().getConnectionManager().getOnlinePlayers());
        this.server = Audience.audience(this.players, minecraftServer.process().getCommandManager().getConsoleSender());
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
        return PacketGroupingAudience.of(minecraftServer, minecraftServer.process().getConnectionManager().getOnlinePlayers().stream().filter(filter).toList());
    }

    @Override
    public @NotNull Audience console() {
        return minecraftServer.process().getCommandManager().getConsoleSender();
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
