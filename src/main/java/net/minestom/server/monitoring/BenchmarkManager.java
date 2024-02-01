package net.minestom.server.monitoring;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Map;

/**
 * Small monitoring tools that can be used to check the current memory usage and Minestom threads CPU usage.
 * <p>
 * Needs to be enabled with {@link #enable(Duration)}. Memory can then be accessed with {@link #getUsedMemory()}
 * and the CPUs usage with {@link #getResultMap()} or {@link #getCpuMonitoringMessage(BenchmarkManager)}.
 * <p>
 * Be aware that this is not the most accurate method, you should use a proper java profiler depending on your needs.
 */
public interface BenchmarkManager {
    boolean isEnabled();

    void enable(@NotNull Duration duration);

    void disable();

    void addThreadMonitor(@NotNull String threadName);

    /**
     * Gets the heap memory used by the server in bytes.
     *
     * @return the memory used by the server
     */
    long getUsedMemory();

    @NotNull Map<String, ThreadResult> getResultMap();


    static @NotNull Component getCpuMonitoringMessage(BenchmarkManager benchmarkManager) {
        if (!benchmarkManager.isEnabled()) return Component.text("CPU monitoring is disabled");
        TextComponent.Builder benchmarkMessage = Component.text();
        for (var resultEntry : benchmarkManager.getResultMap().entrySet()) {
            final String name = resultEntry.getKey();
            final ThreadResult result = resultEntry.getValue();

            benchmarkMessage.append(Component.text(name, NamedTextColor.GRAY));
            benchmarkMessage.append(Component.text(": "));
            benchmarkMessage.append(Component.text(MathUtils.round(result.getCpuPercentage(), 2), NamedTextColor.YELLOW));
            benchmarkMessage.append(Component.text("% CPU ", NamedTextColor.YELLOW));
            benchmarkMessage.append(Component.text(MathUtils.round(result.getUserPercentage(), 2), NamedTextColor.RED));
            benchmarkMessage.append(Component.text("% USER ", NamedTextColor.RED));
            benchmarkMessage.append(Component.text(MathUtils.round(result.getBlockedPercentage(), 2), NamedTextColor.LIGHT_PURPLE));
            benchmarkMessage.append(Component.text("% BLOCKED ", NamedTextColor.LIGHT_PURPLE));
            benchmarkMessage.append(Component.text(MathUtils.round(result.getWaitedPercentage(), 2), NamedTextColor.GREEN));
            benchmarkMessage.append(Component.text("% WAITED ", NamedTextColor.GREEN));
            benchmarkMessage.append(Component.newline());
        }
        return benchmarkMessage.build();
    }
}
