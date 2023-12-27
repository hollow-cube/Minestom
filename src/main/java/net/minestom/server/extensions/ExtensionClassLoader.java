package net.minestom.server.extensions;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.net.URLClassLoader;

public final class ExtensionClassLoader extends URLClassLoader {
    private EventNode<Event> eventNode;
    private ComponentLogger logger;

    public ExtensionClassLoader() {
        super(new URL[]{}, MinecraftServer.class.getClassLoader());
    }

    @Override
    public void addURL(@NotNull URL url) {
        super.addURL(url);
    }

    public EventNode<Event> getEventNode() {
        if (eventNode == null) {
            eventNode = EventNode.all("global");
            MinecraftServer.getGlobalEventHandler().addChild(eventNode);
        }
        return eventNode;
    }

    public ComponentLogger getLogger() {
        if (logger == null) {
            logger = ComponentLogger.logger();
        }
        return logger;
    }

    void terminate() {
        if (eventNode != null) {
            MinecraftServer.getGlobalEventHandler().removeChild(eventNode);
        }
    }
}