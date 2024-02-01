package net.minestom.server.network;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Manages the connected clients.
 */
public interface ConnectionManager {

    /**
     * Gets the number of "online" players, eg for the query response.
     *
     * <p>Only includes players in the play state, not players in configuration.</p>
     */
    int getOnlinePlayerCount();

    /**
     * Returns an unmodifiable set containing the players currently in the play state.
     */
    @NotNull Collection<@NotNull Player> getOnlinePlayers();

    /**
     * Returns an unmodifiable set containing the players currently in the configuration state.
     */
    @NotNull Collection<@NotNull Player> getConfigPlayers();

    /**
     * Gets the {@link Player} linked to a {@link PlayerConnection}.
     *
     * <p>The player will be returned whether they are in the play or config state,
     * so be sure to check before sending packets to them.</p>
     *
     * @param connection the player connection
     * @return the player linked to the connection
     */
    Player getPlayer(@NotNull PlayerConnection connection);

    /**
     * Gets the first player in the play state which validates {@link String#equalsIgnoreCase(String)}.
     * <p>
     * This can cause issue if two or more players have the same username.
     *
     * @param username the player username (case-insensitive)
     * @return the first player who validate the username condition, null if none was found
     */
    @Nullable Player getOnlinePlayerByUsername(@NotNull String username);

    /**
     * Gets the first player in the play state which validates {@link UUID#equals(Object)}.
     * <p>
     * This can cause issue if two or more players have the same UUID.
     *
     * @param uuid the player UUID
     * @return the first player who validate the UUID condition, null if none was found
     */
    @Nullable Player getOnlinePlayerByUuid(@NotNull UUID uuid);

    /**
     * Finds the closest player in the play state matching a given username.
     *
     * @param username the player username (can be partial)
     * @return the closest match, null if no players are online
     */
    @Nullable Player findOnlinePlayer(@NotNull String username);

    /**
     * Changes how {@link UUID} are attributed to players.
     * <p>
     * Shouldn't be override if already defined.
     * <p>
     * Be aware that it is possible for an UUID provider to be ignored, for example in the case of a proxy (eg: velocity).
     *
     * @param uuidProvider the new player connection uuid provider,
     *                     setting it to null would apply a random UUID for each player connection
     * @see #getPlayerConnectionUuid(PlayerConnection, String)
     */
    void setUuidProvider(@Nullable UuidProvider uuidProvider);

    /**
     * Computes the UUID of the specified connection.
     * Used in {@link ClientLoginStartPacket} in order
     * to give the player the right {@link UUID}.
     *
     * @param playerConnection the player connection
     * @param username         the username given by the connection
     * @return the uuid based on {@code playerConnection}
     * return a random UUID if no UUID provider is defined see {@link #setUuidProvider(UuidProvider)}
     */
    @NotNull UUID getPlayerConnectionUuid(@NotNull PlayerConnection playerConnection, @NotNull String username);

    /**
     * Changes the {@link Player} provider, to change which object to link to him.
     *
     * @param playerProvider the new {@link PlayerProvider}, can be set to null to apply the default provider
     */
    void setPlayerProvider(@Nullable PlayerProvider playerProvider);

    /**
     * Creates a player object and begins the transition from the login state to the config state.
     */
    @ApiStatus.Internal
    @NotNull Player createPlayer(@NotNull PlayerConnection connection, @NotNull UUID uuid, @NotNull String username);

    @ApiStatus.Internal
    @NotNull CompletableFuture<Void> transitionLoginToConfig(@NotNull Player player);

    @ApiStatus.Internal
    void transitionPlayToConfig(@NotNull Player player);

    @ApiStatus.Internal
    void doConfiguration(@NotNull Player player, boolean isFirstConfig);

    @ApiStatus.Internal
    void transitionConfigToPlay(@NotNull Player player);

    /**
     * Removes a {@link Player} from the players list.
     * <p>
     * Used during disconnection, you shouldn't have to do it manually.
     *
     * @param connection the player connection
     * @see PlayerConnection#disconnect() to properly disconnect a player
     */
    @ApiStatus.Internal
    void removePlayer(@NotNull PlayerConnection connection);

    void shutdown();

    void tick(long tickStart);

    @ApiStatus.Internal
    void updateWaitingPlayers();
}
