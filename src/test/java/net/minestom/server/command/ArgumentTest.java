package net.minestom.server.command;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ArgumentTest {

    @Test
    public void testParseSelf() {
        MinecraftServer minecraftServer = new MinecraftServer();
        assertEquals("example", Argument.parse(new ServerSender(minecraftServer), ArgumentType.String("example")));
        assertEquals(55, Argument.parse(new ServerSender(minecraftServer), ArgumentType.Integer("55")));
    }

    @Test
    public void testCallback() {
        var arg = ArgumentType.String("id");

        assertFalse(arg.hasErrorCallback());
        arg.setCallback((sender, exception) -> {
        });
        assertTrue(arg.hasErrorCallback());
    }

    @Test
    public void testDefaultValue() {
        MinecraftServer minecraftServer = new MinecraftServer();
        var arg = ArgumentType.String("id");

        assertFalse(arg.isOptional());
        arg.setDefaultValue("default value");
        assertTrue(arg.isOptional());
        assertEquals("default value", arg.getDefaultValue().apply(new ServerSender(minecraftServer)));
    }

    @Test
    public void testSuggestionCallback() {
        MinecraftServer minecraftServer = new MinecraftServer();
        var arg = ArgumentType.String("id");

        assertFalse(arg.hasSuggestion());

        arg.setSuggestionCallback((sender, context, suggestion) -> suggestion.addEntry(new SuggestionEntry("entry")));
        assertTrue(arg.hasSuggestion());

        Suggestion suggestion = new Suggestion("input", 2, 4);
        arg.getSuggestionCallback().apply(new ServerSender(minecraftServer), new CommandContext("input"), suggestion);

        assertEquals(suggestion.getEntries(), List.of(new SuggestionEntry("entry")));
    }
}