package net.minestom.server;

import lombok.Getter;
import net.minestom.server.advancements.AdvancementManager;
import net.minestom.server.advancements.AdvancementManagerImpl;
import net.minestom.server.adventure.audience.AudienceManager;
import net.minestom.server.adventure.audience.AudienceManagerImpl;
import net.minestom.server.adventure.bossbar.BossBarManager;
import net.minestom.server.adventure.bossbar.BossBarManagerImpl;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.CommandManagerImpl;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.GlobalEventHandlerImpl;
import net.minestom.server.exception.ExceptionHandler;
import net.minestom.server.exception.ExceptionHandlerImpl;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.gamedata.tags.TagManager;
import net.minestom.server.gamedata.tags.TagManagerImpl;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.InstanceManagerImpl;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.BlockManagerImpl;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.listener.manager.PacketListenerManagerImpl;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.monitoring.BenchmarkManagerImpl;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.ConnectionManagerImpl;
import net.minestom.server.network.PacketProcessor;
import net.minestom.server.network.PacketProcessorImpl;
import net.minestom.server.network.socket.Server;
import net.minestom.server.network.socket.ServerImpl;
import net.minestom.server.recipe.RecipeManager;
import net.minestom.server.recipe.RecipeManagerImpl;
import net.minestom.server.scoreboard.TeamManager;
import net.minestom.server.scoreboard.TeamManagerImpl;
import net.minestom.server.thread.ChunkDispatcher;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.timer.SchedulerManagerImpl;
import net.minestom.server.world.DimensionTypeManager;
import net.minestom.server.world.DimensionTypeManagerImpl;
import net.minestom.server.world.biomes.BiomeManager;
import net.minestom.server.world.biomes.BiomeManagerImpl;

@Getter
final class ServerFacadeImpl implements ServerFacade {

    private final ExceptionHandler exceptionHandler;
    private final ConnectionManager connectionManager;
    private final PacketListenerManager packetListenerManager;
    private final PacketProcessor packetProcessor;
    private final InstanceManager instanceManager;
    private final BlockManager blockManager;
    private final CommandManager commandManager;
    private final RecipeManager recipeManager;
    private final TeamManager teamManager;
    private final GlobalEventHandler globalEventHandler;
    private final SchedulerManager schedulerManager;
    private final BenchmarkManager benchmarkManager;
    private final DimensionTypeManager dimensionTypeManager;
    private final BiomeManager biomeManager;
    private final AdvancementManager advancementManager;
    private final BossBarManager bossBarManager;
    private final TagManager tagManager;
    private final Server server;

    private final ChunkDispatcher chunkDispatcher;
    private final Ticker ticker;

    private final ServerSettings serverSettings;
    private final AudienceManager audienceManager;
    private final MojangAuth mojangAuth;
    private final ServerStarter serverStarter;


    public ServerFacadeImpl(ServerSettings serverSettings) {
        this.serverSettings = serverSettings;
        this.exceptionHandler = new ExceptionHandlerImpl();
        this.globalEventHandler = new GlobalEventHandlerImpl(this);
        this.packetListenerManager = new PacketListenerManagerImpl(this);
        this.packetProcessor = new PacketProcessorImpl(this);
        this.chunkDispatcher = ChunkDispatcher.singleThread(this);
        this.blockManager = new BlockManagerImpl();
        this.bossBarManager = new BossBarManagerImpl(this);
        this.biomeManager = new BiomeManagerImpl();
        this.instanceManager = new InstanceManagerImpl(this);

        this.commandManager = new CommandManagerImpl(this, this);
        this.recipeManager = new RecipeManagerImpl();

        this.schedulerManager = new SchedulerManagerImpl();
        this.benchmarkManager = new BenchmarkManagerImpl(this);
        this.dimensionTypeManager = new DimensionTypeManagerImpl();
        this.advancementManager = new AdvancementManagerImpl(serverSettings);
        this.tagManager = new TagManagerImpl();
        this.teamManager = new TeamManagerImpl(serverSettings);
        this.connectionManager = new ConnectionManagerImpl(tagManager, this);
        this.server = new ServerImpl(serverSettings, this);
        this.audienceManager = new AudienceManagerImpl(commandManager, this, this);
        this.ticker = new TickerImpl(this);
        this.serverStarter = new ServerStarter(this);
        this.mojangAuth = new MojangAuth(this, this);
    }
}
