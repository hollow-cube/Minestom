package net.minestom.demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import org.jetbrains.annotations.NotNull;

/**
 * A simple shutdown command.
 */
public class ShutdownCommand extends Command {

    private final MinecraftServer minecraftServer;

    public ShutdownCommand(MinecraftServer minecraftServer) {
        super("shutdown");
        this.minecraftServer = minecraftServer;
        addSyntax(this::execute);
    }

    private void execute(@NotNull CommandSender commandSender, @NotNull CommandContext commandContext) {
        minecraftServer.stopCleanly();
    }
}
