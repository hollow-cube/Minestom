package net.minestom.server.adventure.audience;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.ServerSettingsProvider;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManagerProvider;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Utility class to access Adventure audiences.
 */
public class AudienceManagerImpl implements AudienceManager {

    private final SingleAudienceProvider audience;
    private final CommandManager commandManager;
    private final ServerSettingsProvider serverSettingsProvider;
    private final ConnectionManagerProvider connectionManagerProvider;

    public AudienceManagerImpl(CommandManager commandManager, ServerSettingsProvider serverSettingsProvider, ConnectionManagerProvider connectionManagerProvider) {
        this.commandManager = commandManager;
        this.serverSettingsProvider = serverSettingsProvider;
        this.connectionManagerProvider = connectionManagerProvider;
        this.audience = new SingleAudienceProvider(commandManager, serverSettingsProvider, connectionManagerProvider);
    }

    /**
     * Gets the {@link AudienceProvider} that provides forwarding audiences.
     *
     * @return the instance
     */
    @Override
    public @NotNull AudienceProvider<Audience> single() {
        return audience;
    }

    /**
     * Gets the {@link AudienceProvider} that provides iterables of audience members.
     *
     * @return the instance
     */
    @Override
    public @NotNull AudienceProvider<Iterable<? extends Audience>> iterable() {
        return audience.collection;
    }

    /**
     * Gets all audience members. This returns {@link #players()} combined with
     * {@link #customs()} and {@link #console()}. This can be a costly operation, so it
     * is often preferable to use {@link #server()} instead.
     *
     * @return all audience members
     */
    @Override
    public @NotNull Audience all() {
        return Audience.audience(audience.server, audience.customs());
    }

    /**
     * Gets all audience members that are of type {@link Player}.
     *
     * @return all players
     */
    @Override
    public @NotNull Audience players() {
        return audience.players;
    }

    /**
     * Gets all audience members that are of type {@link Player} and match the predicate.
     *
     * @param filter the predicate
     * @return all players matching the predicate
     */
    @Override
    public @NotNull Audience players(@NotNull Predicate<Player> filter) {
        return PacketGroupingAudience.of(serverSettingsProvider, connectionManagerProvider.getConnectionManager().getOnlinePlayers().stream().filter(filter).toList());
    }

    /**
     * Gets the console as an audience.
     *
     * @return the console
     */
    @Override
    public @NotNull Audience console() {
        return commandManager.getConsoleSender();
    }

    /**
     * Gets the combination of {@link #players()} and {@link #console()}.
     *
     * @return the audience of all players and the console
     */
    @Override
    public @NotNull Audience server() {
        return audience.server;
    }

    /**
     * Gets all custom audience members.
     *
     * @return all custom audience members
     */
    @Override
    public @NotNull Audience customs() {
        return Audience.audience(audience.iterable().customs());
    }

    /**
     * Gets all custom audience members stored using the given keyed object.
     *
     * @param keyed the keyed object
     * @return all custom audience members stored using the key of the object
     */
    @Override
    public @NotNull Audience custom(@NotNull Keyed keyed) {
        return custom(keyed.key());
    }

    /**
     * Gets all custom audience members stored using the given key.
     *
     * @param key the key
     * @return all custom audience members stored using the key
     */
    @Override
    public @NotNull Audience custom(@NotNull Key key) {
        return Audience.audience(audience.iterable().custom(key));
    }

    /**
     * Gets all custom audience members stored using the given keyed object that match
     * the given predicate.
     *
     * @param keyed  the keyed object
     * @param filter the predicate
     * @return all custom audience members stored using the key
     */
    @Override
    public @NotNull Audience custom(@NotNull Keyed keyed, Predicate<Audience> filter) {
        return custom(keyed.key(), filter);
    }

    /**
     * Gets all custom audience members stored using the given key that match the
     * given predicate.
     *
     * @param key    the key
     * @param filter the predicate
     * @return all custom audience members stored using the key
     */
    @Override
    public @NotNull Audience custom(@NotNull Key key, Predicate<Audience> filter) {
        return Audience.audience(audience.iterable().custom(key, filter));
    }

    /**
     * Gets all custom audience members matching the given predicate.
     *
     * @param filter the predicate
     * @return all matching custom audience members
     */
    @Override
    public @NotNull Audience customs(@NotNull Predicate<Audience> filter) {
        return Audience.audience(audience.iterable().customs(filter));
    }

    /**
     * Gets all audience members that match the given predicate.
     *
     * @param filter the predicate
     * @return all matching audience members
     */
    @Override
    public @NotNull Audience all(@NotNull Predicate<Audience> filter) {
        return Audience.audience(audience.iterable().all(filter));
    }

    /**
     * Gets the audience registry used to register custom audiences.
     *
     * @return the registry
     */
    @Override
    public @NotNull AudienceRegistry registry() {
        return audience.iterable().registry();
    }
}
