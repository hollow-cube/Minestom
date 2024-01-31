package net.minestom.demo.commands;

import net.minestom.server.ServerFacade;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentCommand;

public class ExecuteCommand extends Command {

    public ExecuteCommand(ServerFacade serverFacade) {
        super("execute");
        ArgumentCommand run = new ArgumentCommand(serverFacade.getCommandManager(), "run");

        addSyntax(((sender, context) -> {}), run);
    }
}
