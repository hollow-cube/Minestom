package net.minestom.server.command;

import lombok.RequiredArgsConstructor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandDispatcher;
import net.minestom.server.command.builder.CommandResult;
import net.minestom.server.command.builder.ParsedCommand;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerCommandEvent;
import net.minestom.server.exception.ExceptionHandler;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.callback.CommandCallback;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Callable;

@RequiredArgsConstructor
public final class CommandManagerImpl implements CommandManager {

    private final ExceptionHandler exceptionHandler;
    private final EventNode<Event> globalEventHandler;

    private static final boolean ASYNC_VIRTUAL = Boolean.getBoolean("minestom.command.async-virtual");

    private final ServerSender serverSender = new ServerSender();
    private final ConsoleSender consoleSender = new ConsoleSender();
    private final CommandParser parser = CommandParser.parser();
    private final CommandDispatcher dispatcher = new CommandDispatcher(this);
    private final Map<String, Command> commandMap = new HashMap<>();
    private final Set<Command> commands = new HashSet<>();

    private CommandCallback unknownCommandCallback;

    @Override
    public synchronized void register(@NotNull Command command) {
        Check.stateCondition(commandExists(command.getName()),
                "A command with the name " + command.getName() + " is already registered!");
        if (command.getAliases() != null) {
            for (String alias : command.getAliases()) {
                Check.stateCondition(commandExists(alias),
                        "A command with the name " + alias + " is already registered!");
            }
        }
        commands.add(command);
        for (String name : command.getNames()) {
            commandMap.put(name, command);
        }
    }

    @Override
    public void unregister(@NotNull Command command) {
        commands.remove(command);
        for (String name : command.getNames()) {
            commandMap.remove(name);
        }
    }

    @Override
    public @Nullable Command getCommand(@NotNull String commandName) {
        return commandMap.get(commandName.toLowerCase(Locale.ROOT));
    }

    @Override
    public @NotNull CommandResult execute(@NotNull CommandSender sender, @NotNull String rawCommand) {
        Callable<CommandResult> callable = () -> {
            var command = rawCommand.trim();
            // Command event
            if (sender instanceof Player player) {
                PlayerCommandEvent playerCommandEvent = new PlayerCommandEvent(player, command);
                globalEventHandler.call(playerCommandEvent);
                if (playerCommandEvent.isCancelled())
                    return CommandResult.of(CommandResult.Type.CANCELLED, command);
                command = playerCommandEvent.getCommand();
            }
            // Process the command
            final CommandParser.Result parsedCommand = parseCommand(sender, command);
            final ExecutableCommand executable = parsedCommand.executable();
            final ExecutableCommand.Result executeResult = executable.execute(sender);
            final CommandResult result = resultConverter(executable, executeResult, command);
            if (result.getType() == CommandResult.Type.UNKNOWN) {
                if (unknownCommandCallback != null) {
                    this.unknownCommandCallback.apply(sender, command);
                }
            }
            return result;
        };


        try {
            if (ASYNC_VIRTUAL) {
                class Reflection {
                    static Method startVirtualThread = null;
                }
                if (Reflection.startVirtualThread == null) {
                    Reflection.startVirtualThread = Thread.class.getDeclaredMethod("startVirtualThread", Runnable.class);
                    Reflection.startVirtualThread.setAccessible(true);
                }

                Reflection.startVirtualThread.invoke(null, (Runnable) () -> {
                    try {
                        callable.call();
                    } catch (Exception e) {
                        exceptionHandler.handleException(e);
                    }
                });
                return CommandResult.of(CommandResult.Type.UNKNOWN, rawCommand);
            } else {
                return callable.call();
            }
        } catch (Exception e) {
            exceptionHandler.handleException(e);
            return CommandResult.of(CommandResult.Type.UNKNOWN, rawCommand);
        }
    }


    @Override
    public @NotNull CommandResult executeServerCommand(@NotNull String command) {
        return execute(serverSender, command);
    }

    @Override
    public @NotNull CommandDispatcher getDispatcher() {
        return dispatcher;
    }

    @Override
    public @Nullable CommandCallback getUnknownCommandCallback() {
        return unknownCommandCallback;
    }

    @Override
    public void setUnknownCommandCallback(@Nullable CommandCallback unknownCommandCallback) {
        this.unknownCommandCallback = unknownCommandCallback;
    }

    @Override
    public @NotNull ConsoleSender getConsoleSender() {
        return consoleSender;
    }

    @Override
    public @NotNull DeclareCommandsPacket createDeclareCommandsPacket(@NotNull Player player) {
        return GraphConverter.createPacket(getGraph(), player);
    }

    @Override
    public @NotNull Set<@NotNull Command> getCommands() {
        return Collections.unmodifiableSet(commands);
    }

    @Override
    public CommandParser.Result parseCommand(@NotNull CommandSender sender, String input) {
        return parser.parse(sender, getGraph(), input);
    }

    private Graph getGraph() {
        //todo cache
        return Graph.merge(commands);
    }

    private static CommandResult resultConverter(ExecutableCommand executable, ExecutableCommand.Result newResult, String input) {
        return CommandResult.of(switch (newResult.type()) {
            case SUCCESS -> CommandResult.Type.SUCCESS;
            case CANCELLED, PRECONDITION_FAILED, EXECUTOR_EXCEPTION -> CommandResult.Type.CANCELLED;
            case INVALID_SYNTAX -> CommandResult.Type.INVALID_SYNTAX;
            case UNKNOWN -> CommandResult.Type.UNKNOWN;
        }, input, ParsedCommand.fromExecutable(executable), newResult.commandData());
    }
}
