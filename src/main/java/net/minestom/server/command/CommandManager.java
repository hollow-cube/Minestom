package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandDispatcher;
import net.minestom.server.command.builder.CommandResult;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.callback.CommandCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Manager used to register {@link Command commands}.
 * <p>
 * It is also possible to simulate a command using {@link #execute(CommandSender, String)}.
 */
public interface CommandManager {
    String COMMAND_PREFIX = "/";

    /**
     * Registers a {@link Command}.
     *
     * @param command the command to register
     * @throws IllegalStateException if a command with the same name already exists
     */
    void register(@NotNull Command command);

    /**
     * Removes a command from the currently registered commands.
     * Does nothing if the command was not registered before
     *
     * @param command the command to remove
     */
    void unregister(@NotNull Command command);

    /**
     * Gets the {@link Command} registered by {@link #register(Command)}.
     *
     * @param commandName the command name
     * @return the command associated with the name, null if not any
     */
    @Nullable Command getCommand(@NotNull String commandName);

    /**
     * Gets if a command with the name {@code commandName} already exists or not.
     *
     * @param commandName the command name to check
     * @return true if the command does exist
     */
    default boolean commandExists(@NotNull String commandName) {
        return getCommand(commandName) != null;
    }

    /**
     * Executes a command for a {@link CommandSender}.
     *
     * @param sender  the sender of the command
     * @param rawCommand the raw command string (without the command prefix)
     * @return the execution result
     */
    @NotNull CommandResult execute(@NotNull CommandSender sender, @NotNull String rawCommand);

    /**
     * Executes the command using a {@link ServerSender}. This can be used
     * to run a silent command (nothing is printed to console).
     *
     * @see #execute(CommandSender, String)
     */
    @NotNull CommandResult executeServerCommand(@NotNull String command);

    @NotNull CommandDispatcher getDispatcher();

    /**
     * Gets the callback executed once an unknown command is run.
     *
     * @return the unknown command callback, null if not any
     */
    @Nullable CommandCallback getUnknownCommandCallback();

    /**
     * Sets the callback executed once an unknown command is run.
     *
     * @param unknownCommandCallback the new unknown command callback,
     *                               setting it to null mean that nothing will be executed
     */
    void setUnknownCommandCallback(@Nullable CommandCallback unknownCommandCallback);

    /**
     * Gets the {@link ConsoleSender} (which is used as a {@link CommandSender}).
     *
     * @return the {@link ConsoleSender}
     */
    @NotNull ConsoleSender getConsoleSender();

    /**
     * Gets the {@link DeclareCommandsPacket} for a specific player.
     * <p>
     * Can be used to update a player auto-completion list.
     *
     * @param player the player to get the commands packet
     * @return the {@link DeclareCommandsPacket} for {@code player}
     */
    @NotNull DeclareCommandsPacket createDeclareCommandsPacket(@NotNull Player player);

    @NotNull Set<@NotNull Command> getCommands();

    /**
     * Parses the command based on the registered commands
     *
     * @param input commands string without prefix
     * @return the parsing result
     */
    CommandParser.Result parseCommand(@NotNull CommandSender sender, String input);
}
