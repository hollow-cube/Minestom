package net.minestom.server;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.advancements.AdvancementManagerProvider;
import net.minestom.server.adventure.audience.AudienceManagerProvider;
import net.minestom.server.adventure.bossbar.BossBarManagerProvider;
import net.minestom.server.command.CommandManagerProvider;
import net.minestom.server.event.GlobalEventHandlerProvider;
import net.minestom.server.exception.ExceptionHandlerProvider;
import net.minestom.server.extras.MojangAuthProvider;
import net.minestom.server.gamedata.tags.TagManagerProvider;
import net.minestom.server.instance.InstanceManagerProvider;
import net.minestom.server.instance.block.BlockManagerProvider;
import net.minestom.server.listener.manager.PacketListenerManagerProvider;
import net.minestom.server.monitoring.BenchmarkManagerProvider;
import net.minestom.server.network.ConnectionManagerProvider;
import net.minestom.server.network.PacketProcessorProvider;
import net.minestom.server.network.socket.ServerProvider;
import net.minestom.server.recipe.RecipeManagerProvider;
import net.minestom.server.scoreboard.TeamManagerProvider;
import net.minestom.server.thread.ChunkDispatcherProvider;
import net.minestom.server.timer.SchedulerManagerProvider;
import net.minestom.server.world.DimensionTypeManagerProvider;
import net.minestom.server.world.biomes.BiomeManagerProvider;

public interface ServerFacade extends
        ExceptionHandlerProvider,
        ConnectionManagerProvider,
        PacketListenerManagerProvider,
        PacketProcessorProvider,
        InstanceManagerProvider,
        BlockManagerProvider,
        CommandManagerProvider,
        RecipeManagerProvider,
        TeamManagerProvider,
        GlobalEventHandlerProvider,
        SchedulerManagerProvider,
        BenchmarkManagerProvider,
        DimensionTypeManagerProvider,
        BiomeManagerProvider,
        AdvancementManagerProvider,
        BossBarManagerProvider,
        TagManagerProvider,
        ServerProvider,
        ChunkDispatcherProvider,
        TickerProvider,
        ServerSettingsProvider,
        AudienceManagerProvider,
        MojangAuthProvider,
        ServerStarterProvider
{
    ComponentLogger LOGGER = ComponentLogger.logger(ServerFacade.class);
    static ServerFacade of(ServerSettings serverSettings) {
        return new ServerFacadeImpl(serverSettings);
    }
}
