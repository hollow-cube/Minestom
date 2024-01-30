package net.minestom.server;

import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.minestom.server.network.packet.server.play.ServerDifficultyPacket;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.Difficulty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

final class ServerSettingsImpl implements ServerSettings {
    private final int chunkViewDistance;
    private final int entityViewDistance;
    private final int compressionThreshold;
    private final int tickPerSecond;

    private @NotNull String brandName;
    private @NotNull Difficulty difficulty;

    private ServerSettingsImpl(int chunkViewDistance, int entityViewDistance, int compressionThreshold, int tickPerSecond, @NotNull String brandName, @NotNull Difficulty difficulty) {
        this.chunkViewDistance = chunkViewDistance;
        this.entityViewDistance = entityViewDistance;
        this.compressionThreshold = compressionThreshold;
        this.tickPerSecond = tickPerSecond;
        this.brandName = brandName;
        this.difficulty = difficulty;
    }

    @Override
    public int getTickPerSecond() {
        return tickPerSecond;
    }

    @Override
    @NotNull
    public String getBrandName() {
        return brandName;
    }

    @Override
    public void setBrandName(@NotNull String brandName, @NotNull ServerProcess serverProcess) {
        this.brandName = brandName;
        PacketUtils.broadcastPlayPacket(serverProcess, PluginMessagePacket.getBrandPacket(this));
    }


    @NotNull
    @Override
    public Difficulty getDifficulty() {
        return difficulty;
    }

    @Override
    public void setDifficulty(@NotNull Difficulty difficulty, @NotNull ServerProcess serverProcess) {
        this.difficulty = difficulty;
        PacketUtils.broadcastPlayPacket(serverProcess, new ServerDifficultyPacket(difficulty, true));
    }

    @Override
    public int getChunkViewDistance() {
        return chunkViewDistance;
    }

    @Override
    public int getEntityViewDistance() {
        return entityViewDistance;
    }

    @Override
    public int getCompressionThreshold() {
        return compressionThreshold;
    }

    public static class Builder {
        private int chunkViewDistance = 8;
        private int entityViewDistance = 5;
        private int compressionThreshold = 256;
        private int tickPerSecond = 20;

        private String brandName = "Minestom";
        private Difficulty difficulty = Difficulty.NORMAL;

        @Range(from = 2, to = 32)
        public Builder chunkViewDistance(int chunkViewDistance) {
            Check.argCondition(!MathUtils.isBetween(chunkViewDistance, 2, 32), "The chunk view distance must be between 2 and 32");
            this.chunkViewDistance = chunkViewDistance;
            return this;
        }

        @Range(from = 0, to = 32)
        public Builder entityViewDistance(int entityViewDistance) {
            Check.argCondition(!MathUtils.isBetween(entityViewDistance, 0, 32), "The entity view distance must be between 0 and 32");
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

        public Builder brandName(@NotNull String brandName) {
            this.brandName = brandName;
            return this;
        }

        public Builder difficulty(@NotNull Difficulty difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public ServerSettings build() {
            return new ServerSettingsImpl(chunkViewDistance, entityViewDistance, compressionThreshold, tickPerSecond, brandName, difficulty);
        }
    }
}
