package net.minestom.server;

import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.minestom.server.network.packet.server.play.ServerDifficultyPacket;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.world.Difficulty;
import org.jetbrains.annotations.NotNull;

public final class ServerSettings {
    private final int chunkViewDistance;
    private final int entityViewDistance;
    private final int compressionThreshold;
    private final int tickPerSecond;
    private final boolean shutdownOnSignal;
    private final int workers;
    private final int maxPacketSize; // 3 bytes var-int
    private final int sendBufferSize;
    private final int receiveBufferSize;
    private final int pooledBufferSize;
    private final boolean tcpNoDelay;

    private @NotNull String brandName;
    private @NotNull Difficulty difficulty;

    public ServerSettings(int chunkViewDistance, int entityViewDistance, int compressionThreshold, int tickPerSecond, boolean shutdownOnSignal, int workers, int maxPacketSize, int sendBufferSize, int receiveBufferSize, int pooledBufferSize, boolean tcpNoDelay, @NotNull String brandName, @NotNull Difficulty difficulty) {
        this.chunkViewDistance = chunkViewDistance;
        this.entityViewDistance = entityViewDistance;
        this.compressionThreshold = compressionThreshold;
        this.tickPerSecond = tickPerSecond;
        this.shutdownOnSignal = shutdownOnSignal;
        this.workers = workers;
        this.maxPacketSize = maxPacketSize;
        this.sendBufferSize = sendBufferSize;
        this.receiveBufferSize = receiveBufferSize;
        this.pooledBufferSize = pooledBufferSize;
        this.tcpNoDelay = tcpNoDelay;
        this.brandName = brandName;
        this.difficulty = difficulty;
    }

    public int getTickMs() {
        return 1000 / tickPerSecond;
    }

    public void updateBrandName(@NotNull String brandName, @NotNull ConnectionManager connectionManager, ServerSettingsProvider serverSettingsProvider) {
        setBrandName(brandName);
        PacketUtils.broadcastPlayPacket(connectionManager, serverSettingsProvider, PluginMessagePacket.getBrandPacket(this));
    }

    public void updateDifficulty(@NotNull Difficulty difficulty, @NotNull ConnectionManager connectionManager, ServerSettingsProvider serverSettingsProvider) {
        setDifficulty(difficulty);
        PacketUtils.broadcastPlayPacket(connectionManager, serverSettingsProvider, new ServerDifficultyPacket(difficulty, true));
    }

    public int getChunkViewDistance() {
        return this.chunkViewDistance;
    }

    public int getEntityViewDistance() {
        return this.entityViewDistance;
    }

    public int getCompressionThreshold() {
        return this.compressionThreshold;
    }

    public int getTickPerSecond() {
        return this.tickPerSecond;
    }

    public boolean isShutdownOnSignal() {
        return this.shutdownOnSignal;
    }

    public int getWorkers() {
        return this.workers;
    }

    public int getMaxPacketSize() {
        return this.maxPacketSize;
    }

    public int getSendBufferSize() {
        return this.sendBufferSize;
    }

    public int getReceiveBufferSize() {
        return this.receiveBufferSize;
    }

    public int getPooledBufferSize() {
        return this.pooledBufferSize;
    }

    public boolean isTcpNoDelay() {
        return this.tcpNoDelay;
    }

    public @NotNull String getBrandName() {
        return this.brandName;
    }

    public @NotNull Difficulty getDifficulty() {
        return this.difficulty;
    }

    public void setBrandName(@NotNull String brandName) {
        this.brandName = brandName;
    }

    public void setDifficulty(@NotNull Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int chunkViewDistance = 8;
        private int entityViewDistance = 5;
        private int compressionThreshold = 256;
        private int tickPerSecond = 20;
        private boolean shutdownOnSignal = true;
        private int workers = Runtime.getRuntime().availableProcessors();
        private int maxPacketSize = 2_097_151; // 3 bytes var-int
        private int sendBufferSize = 262_143;
        private int receiveBufferSize = 32_767;
        private int pooledBufferSize = 262_143;
        private boolean tcpNoDelay = true;

        private @NotNull String brandName = "Minestom";
        private @NotNull Difficulty difficulty = Difficulty.NORMAL;

        public Builder chunkViewDistance(int chunkViewDistance) {
            this.chunkViewDistance = chunkViewDistance;
            return this;
        }

        public Builder entityViewDistance(int entityViewDistance) {
            this.entityViewDistance = entityViewDistance;
            return this;
        }

        public Builder compressionThreshold(int compressionThreshold) {
            this.compressionThreshold = compressionThreshold;
            return this;
        }

        public Builder tickPerSecond(int tickPerSecond) {
            this.tickPerSecond = tickPerSecond;
            return this;
        }

        public Builder shutdownOnSignal(boolean shutdownOnSignal) {
            this.shutdownOnSignal = shutdownOnSignal;
            return this;
        }

        public Builder workers(int workers) {
            this.workers = workers;
            return this;
        }

        public Builder maxPacketSize(int maxPacketSize) {
            this.maxPacketSize = maxPacketSize;
            return this;
        }

        public Builder sendBufferSize(int sendBufferSize) {
            this.sendBufferSize = sendBufferSize;
            return this;
        }

        public Builder receiveBufferSize(int receiveBufferSize) {
            this.receiveBufferSize = receiveBufferSize;
            return this;
        }

        public Builder pooledBufferSize(int pooledBufferSize) {
            this.pooledBufferSize = pooledBufferSize;
            return this;
        }

        public Builder tcpNoDelay(boolean tcpNoDelay) {
            this.tcpNoDelay = tcpNoDelay;
            return this;
        }

        public Builder brandName(@NotNull String brandName) {
            this.brandName = brandName;
            return this;
        }

        public Builder difficulty(@NotNull Difficulty difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public ServerSettings build() {
            return new ServerSettings(
                    chunkViewDistance,
                    entityViewDistance,
                    compressionThreshold,
                    tickPerSecond,
                    shutdownOnSignal,
                    workers,
                    maxPacketSize,
                    sendBufferSize,
                    receiveBufferSize,
                    pooledBufferSize,
                    tcpNoDelay,
                    brandName,
                    difficulty
            );
        }
    }
}
