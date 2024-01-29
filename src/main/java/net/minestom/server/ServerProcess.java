package net.minestom.server;

import net.minestom.server.advancements.AdvancementManager;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.adventure.bossbar.BossBarManager;
import net.minestom.server.command.CommandManager;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.exception.ExceptionManager;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.gamedata.tags.TagManager;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.PacketProcessor;
import net.minestom.server.network.socket.Server;
import net.minestom.server.recipe.RecipeManager;
import net.minestom.server.scoreboard.TeamManager;
import net.minestom.server.snapshot.Snapshotable;
import net.minestom.server.thread.ThreadDispatcher;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.world.DimensionTypeManager;
import net.minestom.server.world.biomes.BiomeManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;

@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface ServerProcess extends Snapshotable {
    /**
     * Handles incoming connections/players.
     */
    @NotNull ConnectionManager getConnectionManager();

    /**
     * Handles registered instances.
     */
    @NotNull InstanceManager getInstanceManager();

    /**
     * Handles {@link net.minestom.server.instance.block.BlockHandler block handlers}
     * and {@link BlockPlacementRule placement rules}.
     */
    @NotNull BlockManager getBlockManager();

    /**
     * Handles registered commands.
     */
    @NotNull CommandManager getCommandManager();

    /**
     * Handles registered recipes shown to clients.
     */
    @NotNull RecipeManager getRecipeManager();

    /**
     * Handles registered teams.
     */
    @NotNull TeamManager getTeamManager();

    /**
     * Gets the global event handler.
     * <p>
     * Used to register event callback at a global scale.
     */
    @NotNull GlobalEventHandler getGlobalEventHandler();

    /**
     * Main scheduler ticked at the server rate.
     */
    @NotNull SchedulerManager getSchedulerManager();

    @NotNull BenchmarkManager getBenchmarkManager();

    /**
     * Handles registered dimensions.
     */
    @NotNull DimensionTypeManager getDimensionTypeManager();

    /**
     * Handles registered biomes.
     */
    @NotNull BiomeManager getBiomeManager();

    /**
     * Handles registered advancements.
     */
    @NotNull AdvancementManager getAdvancementManager();

    /**
     * Handles registered boss bars.
     */
    @NotNull BossBarManager getBossBarManager();

    /**
     * Handles registry tags.
     */
    @NotNull TagManager getTagManager();

    /**
     * Handles all thrown exceptions from the server.
     */
    @NotNull ExceptionManager getExceptionManager();

    /**
     * Handles incoming packets.
     */
    @NotNull PacketListenerManager getPacketListenerManager();

    /**
     * Gets the object handling the client packets processing.
     * <p>
     * Can be used if you want to convert a buffer to a client packet object.
     */
    @NotNull PacketProcessor getPacketProcessor();

    /**
     * Exposed socket server.
     */
    @NotNull Server getServer();

    /**
     * Dispatcher for tickable game objects.
     */
    @NotNull ThreadDispatcher<Chunk> dispatcher();

    /**
     * Handles the server ticks.
     */
    @NotNull Ticker ticker();

    void start(@NotNull SocketAddress socketAddress);

    void stop();

    boolean isAlive();

    Audiences getAudiences();

    MojangAuth getMojangAuth();

    @ApiStatus.NonExtendable
    interface Ticker {
        void tick(long nanoTime);
    }
}
