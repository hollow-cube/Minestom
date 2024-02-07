package net.minestom.server.utils.time;

import net.minestom.server.ServerSettings;

public enum TickTimeUnit {
    DAY(24 * 60 * 60 * 1000),
    HOUR(60 * 60 * 1000),
    MINUTE(60 * 1000),
    SECOND(1000),
    MILLISECOND(1);

    private final long millis;

    TickTimeUnit(long millis) {
        this.millis = millis;
    }

    public long millis(int value) {
        return millis * value;
    }

    public long ticks(int value, ServerSettings serverSettings) {
        int tps = serverSettings.getTickPerSecond();
        return millis * value * tps / 1000;
    }
}