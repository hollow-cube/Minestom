package net.minestom.server.command.builder.parser;

import net.minestom.server.ServerSettings;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.*;
import net.minestom.server.command.builder.arguments.minecraft.*;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentEnchantment;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentEntityType;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentParticle;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.minestom.server.command.builder.arguments.number.ArgumentFloat;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec2;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec3;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.utils.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ArgumentParser {

    record ArgumentMapKey(String id, CommandManager commandManager, ServerSettings serverSettings, InstanceManager instanceManager, ConnectionManager connectionManager) {}

    private static final Map<String, Function<ArgumentMapKey, Argument<?>>> ARGUMENT_FUNCTION_MAP = new ConcurrentHashMap<>();

    static {
        ARGUMENT_FUNCTION_MAP.put("literal", key -> new ArgumentLiteral(key.id));
        ARGUMENT_FUNCTION_MAP.put("boolean", key -> new ArgumentBoolean(key.id));
        ARGUMENT_FUNCTION_MAP.put("integer", key -> new ArgumentInteger(key.id));
        ARGUMENT_FUNCTION_MAP.put("double", key -> new ArgumentDouble(key.id));
        ARGUMENT_FUNCTION_MAP.put("float", key -> new ArgumentFloat(key.id));
        ARGUMENT_FUNCTION_MAP.put("string", key -> new ArgumentString(key.id));
        ARGUMENT_FUNCTION_MAP.put("word", key -> new ArgumentWord(key.id));
        ARGUMENT_FUNCTION_MAP.put("stringarray", key -> new ArgumentStringArray(key.id));
        ARGUMENT_FUNCTION_MAP.put("command", key -> new ArgumentCommand(key.commandManager, key.id));
        // TODO enum
        ARGUMENT_FUNCTION_MAP.put("color", key -> new ArgumentColor(key.id));
        ARGUMENT_FUNCTION_MAP.put("time", key -> new ArgumentTime(key.id, key.serverSettings));
        ARGUMENT_FUNCTION_MAP.put("enchantment", key -> new ArgumentEnchantment(key.id));
        ARGUMENT_FUNCTION_MAP.put("particle", key -> new ArgumentParticle(key.id));
        ARGUMENT_FUNCTION_MAP.put("resourcelocation", key -> new ArgumentResourceLocation(key.id));
        ARGUMENT_FUNCTION_MAP.put("entitytype", key -> new ArgumentEntityType(key.id));
        ARGUMENT_FUNCTION_MAP.put("blockstate", key -> new ArgumentBlockState(key.id));
        ARGUMENT_FUNCTION_MAP.put("intrange", key -> new ArgumentIntRange(key.id));
        ARGUMENT_FUNCTION_MAP.put("floatrange", key -> new ArgumentFloatRange(key.id));

        ARGUMENT_FUNCTION_MAP.put("entity", key -> new ArgumentEntity(key.id, key.instanceManager, key.connectionManager).singleEntity(true));
        ARGUMENT_FUNCTION_MAP.put("entities", key -> new ArgumentEntity(key.id, key.instanceManager, key.connectionManager));
        ARGUMENT_FUNCTION_MAP.put("player", key -> new ArgumentEntity(key.id, key.instanceManager, key.connectionManager).singleEntity(true).onlyPlayers(true));
        ARGUMENT_FUNCTION_MAP.put("players", key -> new ArgumentEntity(key.id, key.instanceManager, key.connectionManager).onlyPlayers(true));

        ARGUMENT_FUNCTION_MAP.put("itemstack", key -> new ArgumentItemStack(key.id));
        ARGUMENT_FUNCTION_MAP.put("component", key -> new ArgumentComponent(key.id));
        ARGUMENT_FUNCTION_MAP.put("uuid", key -> new ArgumentUUID(key.id));
        ARGUMENT_FUNCTION_MAP.put("nbt", key -> new ArgumentNbtTag(key.id));
        ARGUMENT_FUNCTION_MAP.put("nbtcompound", key -> new ArgumentNbtCompoundTag(key.id));
        ARGUMENT_FUNCTION_MAP.put("relativeblockposition", key -> new ArgumentRelativeBlockPosition(key.id));
        ARGUMENT_FUNCTION_MAP.put("relativevec3", key -> new ArgumentRelativeVec3(key.id));
        ARGUMENT_FUNCTION_MAP.put("relativevec2", key -> new ArgumentRelativeVec2(key.id));
    }

    @ApiStatus.Experimental
    public static @NotNull Argument<?>[] generate(@NotNull String format, CommandManager commandManager, ServerSettings serverSettings, InstanceManager instanceManager, ConnectionManager connectionManager) {
        List<Argument<?>> result = new ArrayList<>();

        // 0 = no state
        // 1 = inside angle bracket <>
        int state = 0;
        // function to create an argument from its identifier
        // not null during state 1
        Function<ArgumentMapKey, Argument<?>> argumentFunction = null;

        StringBuilder builder = new StringBuilder();

        // test: Integer<name> String<hey>
        for (int i = 0; i < format.length(); i++) {
            char c = format.charAt(i);

            // No state
            if (state == 0) {
                if (c == ' ') {
                    // Use literal as the default argument
                    final String argument = builder.toString();
                    if (argument.length() != 0) {
                        result.add(new ArgumentLiteral(argument));
                        builder = new StringBuilder();
                    }
                } else if (c == '<') {
                    // Retrieve argument type
                    final String argument = builder.toString();
                    argumentFunction = ARGUMENT_FUNCTION_MAP.get(argument.toLowerCase(Locale.ROOT));
                    if (argumentFunction == null) {
                        throw new IllegalArgumentException("error invalid argument name: " + argument);
                    }

                    builder = new StringBuilder();
                    state = 1;
                } else {
                    // Append to builder
                    builder.append(c);
                }

                continue;
            }

            // Inside bracket <>
            if (state == 1) {
                if (c == '>') {
                    final String param = builder.toString();
                    // TODO argument options
                    Argument<?> argument = argumentFunction.apply(new ArgumentMapKey(param, commandManager, serverSettings, instanceManager, connectionManager));
                    result.add(argument);

                    builder = new StringBuilder();
                    state = 0;
                } else {
                    builder.append(c);
                }

                continue;
            }

        }

        // Use remaining as literal if present
        if (state == 0) {
            final String argument = builder.toString();
            if (argument.length() != 0) {
                result.add(new ArgumentLiteral(argument));
            }
        }

        return result.toArray(Argument[]::new);
    }

    @Nullable
    public static ArgumentResult validate(@NotNull CommandSender sender,
                                          @NotNull Argument<?> argument,
                                          @NotNull Argument<?>[] arguments, int argIndex,
                                          @NotNull String[] inputArguments, int inputIndex) {
        final boolean end = inputIndex == inputArguments.length;
        if (end) // Stop if there is no input to analyze left
            return null;

        // the parsed argument value, null if incorrect
        Object parsedValue = null;
        // the argument exception, null if the input is correct
        ArgumentSyntaxException argumentSyntaxException = null;
        // true if the arg is valid, false otherwise
        boolean correct = false;
        // The raw string value of the argument
        String rawArg = null;

        if (argument.useRemaining()) {
            final boolean hasArgs = inputArguments.length > inputIndex;
            // Verify if there is any string part available
            if (hasArgs) {
                StringBuilder builder = new StringBuilder();
                // Argument is supposed to take the rest of the command input
                for (int i = inputIndex; i < inputArguments.length; i++) {
                    final String arg = inputArguments[i];
                    if (builder.length() > 0)
                        builder.append(StringUtils.SPACE);
                    builder.append(arg);
                }

                rawArg = builder.toString();

                try {
                    parsedValue = argument.parse(sender, rawArg);
                    correct = true;
                } catch (ArgumentSyntaxException exception) {
                    argumentSyntaxException = exception;
                }
            }
        } else {
            // Argument is either single-word or can accept optional delimited space(s)
            StringBuilder builder = new StringBuilder();
            for (int i = inputIndex; i < inputArguments.length; i++) {
                builder.append(inputArguments[i]);

                rawArg = builder.toString();

                try {
                    parsedValue = argument.parse(sender, rawArg);

                    // Prevent quitting the parsing too soon if the argument
                    // does not allow space
                    final boolean lastArgumentIteration = argIndex + 1 == arguments.length;
                    if (lastArgumentIteration && i + 1 < inputArguments.length) {
                        if (!argument.allowSpace())
                            break;
                        builder.append(StringUtils.SPACE);
                        continue;
                    }

                    correct = true;

                    inputIndex = i + 1;
                    break;
                } catch (ArgumentSyntaxException exception) {
                    argumentSyntaxException = exception;

                    if (!argument.allowSpace()) {
                        // rawArg should be the remaining
                        for (int j = i + 1; j < inputArguments.length; j++) {
                            final String arg = inputArguments[j];
                            if (builder.length() > 0)
                                builder.append(StringUtils.SPACE);
                            builder.append(arg);
                        }
                        rawArg = builder.toString();
                        break;
                    }
                    builder.append(StringUtils.SPACE);
                }
            }
        }

        ArgumentResult argumentResult = new ArgumentResult();
        argumentResult.argument = argument;
        argumentResult.correct = correct;
        argumentResult.inputIndex = inputIndex;
        argumentResult.argumentSyntaxException = argumentSyntaxException;

        argumentResult.useRemaining = argument.useRemaining();

        argumentResult.rawArg = rawArg;

        argumentResult.parsedValue = parsedValue;
        return argumentResult;
    }

    public static class ArgumentResult {
        public Argument<?> argument;
        public boolean correct;
        public int inputIndex;
        public ArgumentSyntaxException argumentSyntaxException;

        public boolean useRemaining;

        public String rawArg;

        // If correct
        public Object parsedValue;
    }

}
