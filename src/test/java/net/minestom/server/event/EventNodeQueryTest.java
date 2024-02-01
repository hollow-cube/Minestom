package net.minestom.server.event;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerSettings;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.PlayerEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.testing.TestUtils.assertEqualsIgnoreOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventNodeQueryTest {

    @Test
    public void find() {
        MinecraftServer minecraftServer = MinecraftServer.of(ServerSettings.builder().build());
        var node = EventNode.all(minecraftServer, "main");
        assertEquals(List.of(), node.findChildren("test"));

        var child1 = EventNode.all(minecraftServer,"test");
        var child2 = EventNode.all(minecraftServer,"test");
        var child3 = EventNode.all(minecraftServer,"test3");

        node.addChild(child1);
        node.addChild(child2);
        node.addChild(child3);

        assertEqualsIgnoreOrder(List.of(child1, child2), node.findChildren("test"));
        assertEqualsIgnoreOrder(List.of(child3), node.findChildren("test3"));

        node.removeChild(child1);
        assertEqualsIgnoreOrder(List.of(child2), node.findChildren("test"));
        assertEqualsIgnoreOrder(List.of(child3), node.findChildren("test3"));
    }

    @Test
    public void findType() {
        MinecraftServer minecraftServer = MinecraftServer.of(ServerSettings.builder().build());
        var node = EventNode.all(minecraftServer,"main");
        assertEquals(List.of(), node.findChildren("test", Event.class));

        var child1 = EventNode.type(minecraftServer, "test", EventFilter.PLAYER);
        var child2 = EventNode.type(minecraftServer, "test", EventFilter.ENTITY);
        var child3 = EventNode.type(minecraftServer, "test3", EventFilter.ENTITY);

        node.addChild(child1);
        node.addChild(child2);
        node.addChild(child3);

        assertEqualsIgnoreOrder(List.of(child1, child2), node.findChildren("test", Event.class));
        assertEqualsIgnoreOrder(List.of(child1, child2), node.findChildren("test", EntityEvent.class));
        assertEqualsIgnoreOrder(List.of(child1), node.findChildren("test", PlayerEvent.class));
        assertEqualsIgnoreOrder(List.of(child3), node.findChildren("test3", EntityEvent.class));

        node.removeChild(child1);
        assertEqualsIgnoreOrder(List.of(child2), node.findChildren("test", Event.class));
        assertEqualsIgnoreOrder(List.of(child2), node.findChildren("test", EntityEvent.class));
        assertEqualsIgnoreOrder(List.of(), node.findChildren("test", PlayerEvent.class));
        assertEqualsIgnoreOrder(List.of(child3), node.findChildren("test3", EntityEvent.class));
    }

    @Test
    public void replace() {
        MinecraftServer minecraftServer = MinecraftServer.of(ServerSettings.builder().build());
        var node = EventNode.all(minecraftServer,"main");

        var child1 = EventNode.all(minecraftServer,"test");
        var child2 = EventNode.all(minecraftServer,"test");
        var child3 = EventNode.all(minecraftServer,"test3");

        node.addChild(child1);
        node.addChild(child2);
        node.addChild(child3);

        var tmp1 = EventNode.all(minecraftServer,"tmp1");
        var tmp2 = EventNode.all(minecraftServer,"tmp2");

        node.replaceChildren("test", tmp1);
        assertEqualsIgnoreOrder(List.of(child2), node.findChildren("test"));
        assertEqualsIgnoreOrder(List.of(tmp1), node.findChildren("tmp1"));

        node.replaceChildren("test3", tmp2);
        assertEqualsIgnoreOrder(List.of(child2), node.findChildren("test"));
        assertEqualsIgnoreOrder(List.of(tmp1), node.findChildren("tmp1"));
        assertEqualsIgnoreOrder(List.of(), node.findChildren("test3"));
        assertEqualsIgnoreOrder(List.of(tmp2), node.findChildren("tmp2"));
    }
}
