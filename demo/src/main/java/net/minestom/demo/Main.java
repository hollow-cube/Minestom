package net.minestom.demo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.demo.block.TestBlockHandler;
import net.minestom.demo.block.placement.DripstonePlacementRule;
import net.minestom.demo.commands.*;
import net.minestom.server.ServerProcess;
import net.minestom.server.ServerSettings;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.lan.OpenToLAN;
import net.minestom.server.extras.lan.OpenToLANConfig;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;
import net.minestom.server.ping.ResponseData;
import net.minestom.server.recipe.RecipeCategory;
import net.minestom.server.recipe.ShapedRecipe;
import net.minestom.server.utils.identity.NamedAndIdentified;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.setProperty("minestom.experiment.pose-updates", "true");

        ServerSettings serverSettings = ServerSettings.builder().compressionThreshold(0).build();

        ServerProcess serverProcess = ServerProcess.of(serverSettings);


        BlockManager blockManager = serverProcess.getBlockManager();
        blockManager.registerBlockPlacementRule(new DripstonePlacementRule());
        blockManager.registerHandler(TestBlockHandler.INSTANCE.getNamespaceId(), () -> TestBlockHandler.INSTANCE);

        CommandManager commandManager = serverProcess.getCommandManager();
        commandManager.register(new TestCommand());
        commandManager.register(new EntitySelectorCommand());
        commandManager.register(new HealthCommand());
        commandManager.register(new LegacyCommand());
        commandManager.register(new DimensionCommand(serverProcess));
        commandManager.register(new ShutdownCommand(serverProcess));
        commandManager.register(new TeleportCommand());
        commandManager.register(new PlayersCommand());
        commandManager.register(new FindCommand());
        commandManager.register(new TitleCommand());
        commandManager.register(new BookCommand());
        commandManager.register(new ShootCommand());
        commandManager.register(new HorseCommand());
        commandManager.register(new EchoCommand());
        commandManager.register(new SummonCommand());
        commandManager.register(new RemoveCommand());
        commandManager.register(new GiveCommand());
        commandManager.register(new SetBlockCommand());
        commandManager.register(new AutoViewCommand());
        commandManager.register(new SaveCommand());
        commandManager.register(new GamemodeCommand());
        commandManager.register(new ExecuteCommand());
        commandManager.register(new RedirectTestCommand());
        commandManager.register(new DisplayCommand());
        commandManager.register(new NotificationCommand());
        commandManager.register(new TestCommand2());
        commandManager.register(new ConfigCommand());
        commandManager.register(new SidebarCommand(serverProcess));
        commandManager.register(new SetEntityType());

        commandManager.setUnknownCommandCallback((sender, command) -> sender.sendMessage(Component.text("Unknown command", NamedTextColor.RED)));

        serverProcess.getBenchmarkManager().enable(Duration.of(10, TimeUnit.SECOND));

        serverProcess.getSchedulerManager().buildShutdownTask(() -> System.out.println("Good night"));

        serverProcess.getGlobalEventHandler().addListener(ServerListPingEvent.class, event -> {
            ResponseData responseData = event.getResponseData();
            responseData.addEntry(NamedAndIdentified.named("The first line is separated from the others"));
            responseData.addEntry(NamedAndIdentified.named("Could be a name, or a message"));

            // on modern versions, you can obtain the player connection directly from the event
            if (event.getConnection() != null) {
                responseData.addEntry(NamedAndIdentified.named("IP test: " + event.getConnection().getRemoteAddress().toString()));

                responseData.addEntry(NamedAndIdentified.named("Connection Info:"));
                String ip = event.getConnection().getServerAddress();
                responseData.addEntry(NamedAndIdentified.named(Component.text('-', NamedTextColor.DARK_GRAY)
                        .append(Component.text(" IP: ", NamedTextColor.GRAY))
                        .append(Component.text(ip != null ? ip : "???", NamedTextColor.YELLOW))));
                responseData.addEntry(NamedAndIdentified.named(Component.text('-', NamedTextColor.DARK_GRAY)
                        .append(Component.text(" PORT: ", NamedTextColor.GRAY))
                        .append(Component.text(event.getConnection().getServerPort()))));
                responseData.addEntry(NamedAndIdentified.named(Component.text('-', NamedTextColor.DARK_GRAY)
                        .append(Component.text(" VERSION: ", NamedTextColor.GRAY))
                        .append(Component.text(event.getConnection().getProtocolVersion()))));
            }
            responseData.addEntry(NamedAndIdentified.named(Component.text("Time", NamedTextColor.YELLOW)
                    .append(Component.text(": ", NamedTextColor.GRAY))
                    .append(Component.text(System.currentTimeMillis(), Style.style(TextDecoration.ITALIC)))));

            // components will be converted the legacy section sign format so they are displayed in the client
            responseData.addEntry(NamedAndIdentified.named(Component.text("You can use ").append(Component.text("styling too!", NamedTextColor.RED, TextDecoration.BOLD))));

            // the data will be automatically converted to the correct format on response, so you can do RGB and it'll be downsampled!
            // on legacy versions, colors will be converted to the section format so it'll work there too
            responseData.setDescription(Component.text("This is a Minestom Server", TextColor.color(0x66b3ff)));
            //responseData.setPlayersHidden(true);
        });

        var ironBlockRecipe = new ShapedRecipe(
                "minestom:test", 2, 2, "",
                RecipeCategory.Crafting.MISC,
                List.of(
                        new DeclareRecipesPacket.Ingredient(List.of(ItemStack.of(Material.IRON_INGOT))),
                        new DeclareRecipesPacket.Ingredient(List.of(ItemStack.of(Material.IRON_INGOT))),
                        new DeclareRecipesPacket.Ingredient(List.of(ItemStack.of(Material.IRON_INGOT))),
                        new DeclareRecipesPacket.Ingredient(List.of(ItemStack.of(Material.IRON_INGOT)))
                ), ItemStack.of(Material.IRON_BLOCK), true) {
            @Override
            public boolean shouldShow(@NotNull Player player) {
                return true;
            }
        };
        serverProcess.getRecipeManager().addRecipe(ironBlockRecipe);

        new PlayerInit(serverProcess).init();

//        VelocityProxy.enable("abcdef");
        //BungeeCordProxy.enable();

        //MojangAuth.init();

        // useful for testing - we don't need to worry about event calls so just set this to a long time
        new OpenToLAN(serverProcess).open(new OpenToLANConfig().eventCallDelay(Duration.of(1, TimeUnit.DAY)));

        serverProcess.start("0.0.0.0", 25565);
//        minecraftServer.start(java.net.UnixDomainSocketAddress.of("minestom-demo.sock"));
        //Runtime.getRuntime().addShutdownHook(new Thread(MinecraftServer::stopCleanly));
    }
}
