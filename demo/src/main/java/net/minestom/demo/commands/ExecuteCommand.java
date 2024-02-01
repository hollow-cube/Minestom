package net.minestom.demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentCommand;

public class ExecuteCommand extends Command {

    public ExecuteCommand(MinecraftServer minecraftServer) {
        super("execute");
        ArgumentCommand run = new ArgumentCommand(minecraftServer.getCommandManager(), "run");

        addSyntax(((sender, context) -> {}), run);
    }
}
