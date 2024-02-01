package net.minestom.server.event;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerSettings;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventNodeGraphTest {

    @Test
    public void single() {
        MinecraftServer minecraftServer = MinecraftServer.of(ServerSettings.builder().build());
        EventNode<Event> node = EventNode.all(minecraftServer,"main");
        verifyGraph(node, new EventNodeImpl.Graph("main", "Event", 0, List.of()));
    }

    @Test
    public void singleChild() {
        MinecraftServer minecraftServer = MinecraftServer.of(ServerSettings.builder().build());
        EventNode<Event> node = EventNode.all(minecraftServer,"main");
        node.addChild(EventNode.all(minecraftServer,"child"));
        verifyGraph(node, new EventNodeImpl.Graph("main", "Event", 0,
                List.of(new EventNodeImpl.Graph("child", "Event", 0, List.of())
                )));
    }

    @Test
    public void childrenPriority() {
        MinecraftServer minecraftServer = MinecraftServer.of(ServerSettings.builder().build());
        {
            EventNode<Event> node = EventNode.all(minecraftServer,"main");
            node.addChild(EventNode.all(minecraftServer,"child1").setPriority(5));
            node.addChild(EventNode.all(minecraftServer,"child2").setPriority(10));
            verifyGraph(node, new EventNodeImpl.Graph("main", "Event", 0,
                    List.of(new EventNodeImpl.Graph("child1", "Event", 5, List.of()),
                            new EventNodeImpl.Graph("child2", "Event", 10, List.of())
                    )));
        }
        {
            EventNode<Event> node = EventNode.all(minecraftServer, "main");
            node.addChild(EventNode.all(minecraftServer,"child2").setPriority(10));
            node.addChild(EventNode.all(minecraftServer,"child1").setPriority(5));
            verifyGraph(node, new EventNodeImpl.Graph("main", "Event", 0,
                    List.of(new EventNodeImpl.Graph("child1", "Event", 5, List.of()),
                            new EventNodeImpl.Graph("child2", "Event", 10, List.of())
                    )));
        }
    }

    void verifyGraph(EventNode<?> n, EventNodeImpl.Graph graph) {
        EventNodeImpl<?> node = (EventNodeImpl<?>) n;
        var nodeGraph = node.createGraph();
        assertEquals(graph, nodeGraph, "Graphs are not equals");
        assertEquals(EventNodeImpl.createStringGraph(graph), EventNodeImpl.createStringGraph(nodeGraph), "String graphs are not equals");
        assertEquals(n.toString(), EventNodeImpl.createStringGraph(nodeGraph), "The node does not use createStringGraph");
    }
}
