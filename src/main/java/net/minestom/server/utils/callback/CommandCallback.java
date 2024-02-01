package net.minestom.server.utils.callback;

import net.minestom.server.command.CommandManagerImpl;
import net.minestom.server.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Functional interface used by the {@link CommandManagerImpl}
 * to execute a callback if an unknown command is run.
 * You can set it with {@link CommandManagerImpl#setUnknownCommandCallback(CommandCallback)}.
 */
@FunctionalInterface
public interface CommandCallback {

    /**
     * Executed if an unknown command is run.
     *
     * @param sender  the command sender
     * @param command the complete command string
     */
    void apply(@NotNull CommandSender sender, @NotNull String command);

}
