package net.minestom.demo.commands;

import net.minestom.server.ServerFacade;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import org.jetbrains.annotations.NotNull;

/**
 * A simple shutdown command.
 */
public class ShutdownCommand extends Command {

    private final ServerFacade serverFacade;

    public ShutdownCommand(ServerFacade serverFacade) {
        super("shutdown");
        this.serverFacade = serverFacade;
        addSyntax(this::execute);
    }

    private void execute(@NotNull CommandSender commandSender, @NotNull CommandContext commandContext) {
        serverFacade.getServerStarter().stop();
    }
}
