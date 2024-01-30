package net.minestom.server;

import net.minestom.server.world.Difficulty;
import org.jetbrains.annotations.NotNull;

public interface ServerSettings {
    static ServerSettingsImpl.Builder builder() {
        return new ServerSettingsImpl.Builder();
    }

    int getTickPerSecond();
    default int getTickMs() {
        return 1000 / getTickPerSecond();
    }

    /**
     * Gets the current server brand name.
     *
     * @return the server brand name
     */
    @NotNull String getBrandName();

    /**
     * Changes the server brand name and send the change to all connected players.
     *
     * @param brandName the server brand name
     * @throws NullPointerException if {@code brandName} is null
     */
    void setBrandName(@NotNull String brandName, @NotNull ServerProcess serverProcess);

    /**
     * Gets the server difficulty showed in game option.
     *
     * @return the server difficulty
     */
    @NotNull Difficulty getDifficulty();

    /**
     * Changes the server difficulty and send the appropriate packet to all connected clients.
     *
     * @param difficulty the new server difficulty
     */
    void setDifficulty(@NotNull Difficulty difficulty, @NotNull ServerProcess serverProcess);

    /**
     * Gets the chunk view distance of the server.
     *
     * @return the chunk view distance
     */
    int getChunkViewDistance();

    /**
     * Gets the entity view distance of the server.
     *
     * @return the entity view distance
     */
    int getEntityViewDistance();

    /**
     * Gets the compression threshold of the server.
     *
     * @return the compression threshold, 0 means that compression is disabled
     */
    int getCompressionThreshold();
}
