package net.minestom.server;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Setter;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.minestom.server.network.packet.server.play.ServerDifficultyPacket;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.world.Difficulty;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@Builder(builderClassName = "Builder")
public final class ServerSettings {
    @Default
    private final int chunkViewDistance = 8;
    @Default
    private final int entityViewDistance = 5;
    @Default
    private final int compressionThreshold = 256;
    @Default
    private final int tickPerSecond = 20;
    @Default
    private final boolean shutdownOnSignal = true;
    @Default
    private final int workers = Runtime.getRuntime().availableProcessors();
    @Default
    private final int maxPacketSize = 2_097_151; // 3 bytes var-int
    @Default
    private final int sendBufferSize = 262_143;
    @Default
    private final int receiveBufferSize = 32_767;
    @Default
    private final int pooledBufferSize = 262_143;
    @Default
    private final boolean tcpNoDelay = true;

    @Default
    private @NotNull String brandName = "Minestom";
    @Default
    private @NotNull Difficulty difficulty = Difficulty.NORMAL;

    public int getTickMs() {
        return 1000 / tickPerSecond;
    }

    public void updateBrandName(@NotNull String brandName, @NotNull ConnectionManager connectionManager, ServerSettings serverSettings) {
        setBrandName(brandName);
        PacketUtils.broadcastPlayPacket(connectionManager, serverSettings, PluginMessagePacket.getBrandPacket(this));
    }

    public void updateDifficulty(@NotNull Difficulty difficulty, @NotNull ConnectionManager connectionManager, ServerSettings serverSettings) {
        setDifficulty(difficulty);
        PacketUtils.broadcastPlayPacket(connectionManager, serverSettings, new ServerDifficultyPacket(difficulty, true));
    }
}
