package net.minestom.demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Entity;
import net.minestom.server.utils.entity.EntityFinder;

public class RemoveCommand extends Command {


    public RemoveCommand(MinecraftServer minecraftServer) {
        super("remove");
        addSubcommand(new RemoveEntities(minecraftServer));
    }

    static class RemoveEntities extends Command {
        private final ArgumentEntity entity;

        public RemoveEntities(MinecraftServer minecraftServer) {
            super("entities");
            setCondition(Conditions::playerOnly);
            entity = ArgumentType.Entity("entity", minecraftServer.getInstanceManager(), minecraftServer.getConnectionManager());
            addSyntax(this::remove, entity);
        }

        private void remove(CommandSender commandSender, CommandContext commandContext) {
            final EntityFinder entityFinder = commandContext.get(entity);
            entityFinder.find(commandSender).forEach(Entity::remove);
        }
    }
}