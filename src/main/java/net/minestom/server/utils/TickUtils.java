package net.minestom.server.utils;

import net.minestom.server.ServerSettings;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * Tick related utilities.
 */
public final class TickUtils {
    /**
     * Number of ticks per second for the default Java-edition client.
     */
    public static final int CLIENT_TPS = 20;

    /**
     * Length of time per tick for the default Java-edition client.
     */
    public static final int CLIENT_TICK_MS = 50;

    /**
     * Creates a number of ticks from a given duration, based on {@link ServerSettings#getTickMs()}.
     *
     * @param duration the duration
     * @return the number of ticks
     * @throws IllegalArgumentException if duration is negative
     */
    public static int fromDuration(ServerSettings serverSettings, @NotNull Duration duration) {
        return TickUtils.fromDuration(duration, serverSettings.getTickMs());
    }

    /**
     * Creates a number of ticks from a given duration.
     *
     * @param duration  the duration
     * @param msPerTick the number of milliseconds per tick
     * @return the number of ticks
     * @throws IllegalArgumentException if duration is negative
     */
    public static int fromDuration(@NotNull Duration duration, int msPerTick) {
        Check.argCondition(duration.isNegative(), "Duration cannot be negative");
        return (int) (duration.toMillis() / msPerTick);
    }
}
