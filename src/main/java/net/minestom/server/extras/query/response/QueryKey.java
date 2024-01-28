package net.minestom.server.extras.query.response;

import net.minestom.server.MinecraftServer;
import net.minestom.server.network.ConnectionState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An enum of default query keys.
 */
public enum QueryKey {
    HOSTNAME((minecraftServer) -> "A Minestom Server"),
    GAME_TYPE((minecraftServer) -> "SMP"),
    GAME_ID("game_id", (minecraftServer) -> "MINECRAFT"),
    VERSION((minecraftServer) -> MinecraftServer.VERSION_NAME),
    PLUGINS(FullQueryResponse::generatePluginsValue),
    MAP((minecraftServer) -> "world"),
    NUM_PLAYERS("numplayers", (minecraftServer) -> String.valueOf(minecraftServer.process().getConnectionManager().getOnlinePlayerCount())),
    MAX_PLAYERS("maxplayers", (minecraftServer) -> String.valueOf(minecraftServer.process().getConnectionManager().getOnlinePlayerCount() + 1)),
    HOST_PORT("hostport", (minecraftServer) -> String.valueOf(minecraftServer.process().getServer().getPort())),
    HOST_IP("hostip", (minecraftServer) -> Objects.requireNonNullElse(minecraftServer.process().getServer().getAddress(), "localhost"));

    static QueryKey[] VALUES = QueryKey.values();

    private final String key;
    private final Function<MinecraftServer, String> value;

    QueryKey(@NotNull Function<MinecraftServer, String> value) {
        this(null, value);
    }

    QueryKey(@Nullable String key, @NotNull Function<MinecraftServer, String> value) {
        this.key = Objects.requireNonNullElse(key, this.name().toLowerCase(Locale.ROOT).replace('_', ' '));
        this.value = value;
    }

    /**
     * Gets the key of this query key.
     *
     * @return the key
     */
    public @NotNull String getKey() {
        return this.key;
    }

    /**
     * Gets the value of this query key.
     *
     * @return the value
     */
    public @NotNull String getValue(MinecraftServer minecraftServer) {
        return this.value.apply(minecraftServer);
    }
}
