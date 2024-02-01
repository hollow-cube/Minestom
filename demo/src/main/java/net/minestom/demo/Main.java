package net.minestom.demo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.demo.block.TestBlockHandler;
import net.minestom.demo.block.placement.DripstonePlacementRule;
import net.minestom.demo.commands.*;
import net.minestom.server.MinecraftServer;
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

        MinecraftServer minecraftServer = MinecraftServer.of(serverSettings);


        BlockManager blockManager = minecraftServer.getBlockManager();
        blockManager.registerBlockPlacementRule(new DripstonePlacementRule());
        blockManager.registerHandler(TestBlockHandler.INSTANCE.getNamespaceId(), () -> TestBlockHandler.INSTANCE);

        CommandManager commandManager = minecraftServer.getCommandManager();
        commandManager.register(new TestCommand());
        commandManager.register(new EntitySelectorCommand(minecraftServer));
        commandManager.register(new HealthCommand());
        commandManager.register(new LegacyCommand());
        commandManager.register(new DimensionCommand(minecraftServer));
        commandManager.register(new ShutdownCommand(minecraftServer));
        commandManager.register(new TeleportCommand(minecraftServer));
        commandManager.register(new PlayersCommand(minecraftServer));
        commandManager.register(new FindCommand());
        commandManager.register(new TitleCommand());
        commandManager.register(new BookCommand());
        commandManager.register(new ShootCommand());
        commandManager.register(new HorseCommand(minecraftServer));
        commandManager.register(new EchoCommand());
        commandManager.register(new SummonCommand(minecraftServer));
        commandManager.register(new RemoveCommand(minecraftServer));
        commandManager.register(new GiveCommand(minecraftServer));
        commandManager.register(new SetBlockCommand());
        commandManager.register(new AutoViewCommand(minecraftServer));
        commandManager.register(new SaveCommand(minecraftServer));
        commandManager.register(new GamemodeCommand(minecraftServer));
        commandManager.register(new ExecuteCommand(minecraftServer));
        commandManager.register(new RedirectTestCommand());
        commandManager.register(new DisplayCommand(minecraftServer));
        commandManager.register(new NotificationCommand());
        commandManager.register(new TestCommand2());
        commandManager.register(new ConfigCommand());
        commandManager.register(new SidebarCommand(minecraftServer));
        commandManager.register(new SetEntityType());

        commandManager.setUnknownCommandCallback((sender, command) -> sender.sendMessage(Component.text("Unknown command", NamedTextColor.RED)));

        minecraftServer.getBenchmarkManager().enable(Duration.of(10, TimeUnit.SECOND));

        minecraftServer.getSchedulerManager().buildShutdownTask(() -> System.out.println("Good night"));

        minecraftServer.getGlobalEventHandler().addListener(ServerListPingEvent.class, event -> {
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
        minecraftServer.getRecipeManager().addRecipe(ironBlockRecipe);

        new PlayerInit(minecraftServer).init();

//        VelocityProxy.enable("abcdef");
        //BungeeCordProxy.enable();

        //MojangAuth.init();

        // useful for testing - we don't need to worry about event calls so just set this to a long time
        new OpenToLAN(minecraftServer.getConnectionManager(), minecraftServer.getServer(), minecraftServer.getSchedulerManager(), minecraftServer.getGlobalEventHandler()).open(new OpenToLANConfig().eventCallDelay(Duration.of(1, TimeUnit.DAY)));

        minecraftServer.getServerStarter().start("0.0.0.0", 25565);
//        minecraftServer.start(java.net.UnixDomainSocketAddress.of("minestom-demo.sock"));
        //Runtime.getRuntime().addShutdownHook(new Thread(MinecraftServer::stopCleanly));
    }
}
