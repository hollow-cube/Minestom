package net.minestom.server.event;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventNodeGraphTest {

    @Test
    public void single() {
        EventNode<Event> node = EventNode.all("main");
        verifyGraph(node, new EventNodeImpl.Graph("main", "Event", 0, List.of()));
    }

    @Test
    public void singleChild() {
        EventNode<Event> node = EventNode.all("main");
        node.addChild(EventNode.all("child"));
        verifyGraph(node, new EventNodeImpl.Graph("main", "Event", 0,
                List.of(new EventNodeImpl.Graph("child", "Event", 0, List.of())
                )));
    }

    @Test
    public void childrenPriority() {
        {
            EventNode<Event> node = EventNode.all("main");
            node.addChild(EventNode.all("child1").setPriority(5));
            node.addChild(EventNode.all("child2").setPriority(10));
            verifyGraph(node, new EventNodeImpl.Graph("main", "Event", 0,
                    List.of(new EventNodeImpl.Graph("child1", "Event", 5, List.of()),
                            new EventNodeImpl.Graph("child2", "Event", 10, List.of())
                    )));
        }
        {
            EventNode<Event> node = EventNode.all("main");
            node.addChild(EventNode.all("child2").setPriority(10));
            node.addChild(EventNode.all("child1").setPriority(5));
            verifyGraph(node, new EventNodeImpl.Graph("main", "Event", 0,
                    List.of(new EventNodeImpl.Graph("child1", "Event", 5, List.of()),
                            new EventNodeImpl.Graph("child2", "Event", 10, List.of())
                    )));
        }
    }

    @Test
    public void childrenRelativePriority() {
        {
            EventNode<Event> node = EventNode.all("main");
            EventNode<Event> child1 = EventNode.all("child1");
            EventNode<Event> child2 = EventNode.all("child2");
            EventNode<Event> child3 = EventNode.all("child3");
            EventNode<Event> child4 = EventNode.all("child4");
            EventNode<Event> child5 = EventNode.all("child5");
            EventNode<Event> child6 = EventNode.all("child6");
            EventNode<Event> child7 = EventNode.all("child7");

            node.addChild(child1);
            node.addChild(child2);
            node.addChild(child3);
            node.addChild(child4);
            node.addChild(child5);
            node.addChild(child6);
            node.addChild(child7);

            node.setChildPriority(child1, child2, EventNode.Relative.BEFORE);
            node.setChildPriority(child1, child3, EventNode.Relative.AFTER);
            node.setChildPriority(child3, child5, EventNode.Relative.BEFORE);
            node.setChildPriority(child4, child2, EventNode.Relative.AFTER);
            node.setChildPriority(child5, child4, EventNode.Relative.BEFORE);
            node.setChildPriority(child6, EventNode.Absolute.FIRST);
            node.setChildPriority(child7, EventNode.Absolute.LAST);

            assert (child1.getPriority() < child2.getPriority());
            assert (child1.getPriority() > child3.getPriority());
            assert (child3.getPriority() < child5.getPriority());
            assert (child4.getPriority() > child2.getPriority());
            assert (child5.getPriority() < child4.getPriority());
            assert (child6.getPriority() == 0);
            assert (child7.getPriority() == 6);
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
