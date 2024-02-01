package net.minestom.server.event;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerSettings;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.minestom.testing.TestUtils.waitUntilCleared;
import static org.junit.jupiter.api.Assertions.*;

public class EventNodeMapTest {

    @Test
    public void uniqueMapping() {
        MinecraftServer minecraftServer = MinecraftServer.of(ServerSettings.builder().build());
        var item = ItemStack.of(Material.DIAMOND);
        var node = EventNode.all(minecraftServer,"main");
        var itemNode1 = node.map(item, EventFilter.ITEM);
        var itemNode2 = node.map(item, EventFilter.ITEM);
        assertNotNull(itemNode1);
        assertSame(itemNode1, itemNode2);

        // Node should still keep track of the mapping until GCed
        // This is to ensure that we do not end up with multiple nodes theoretically mapping the same object
        node.unmap(item);
        assertSame(itemNode1, node.map(item, EventFilter.ITEM));
    }

    @Test
    public void lazyRegistration() {
        MinecraftServer minecraftServer = MinecraftServer.of(ServerSettings.builder().build());
        var item = ItemStack.of(Material.DIAMOND);
        var node = (EventNodeImpl<Event>) EventNode.all(minecraftServer,"main");
        var itemNode = node.map(item, EventFilter.ITEM);
        assertFalse(node.registeredMappedNode.containsKey(item));
        itemNode.addListener(EventNodeTest.ItemTestEvent.class, event -> {
        });
        assertTrue(node.registeredMappedNode.containsKey(item));
    }

    @Test
    public void secondMap() {
        MinecraftServer minecraftServer = MinecraftServer.of(ServerSettings.builder().build());
        var item = ItemStack.of(Material.DIAMOND);
        var node = (EventNodeImpl<Event>) EventNode.all(minecraftServer,"main");
        var itemNode = node.map(item, EventFilter.ITEM);
        assertSame(itemNode, itemNode.map(item, EventFilter.ITEM));
        assertThrows(Exception.class, () -> itemNode.map(ItemStack.AIR, EventFilter.ITEM));
    }

    @Test
    public void map() {
        MinecraftServer minecraftServer = MinecraftServer.of(ServerSettings.builder().build());
        var item = ItemStack.of(Material.DIAMOND);
        var node = EventNode.all(minecraftServer, "main");

        AtomicBoolean result = new AtomicBoolean(false);
        var itemNode = node.map(item, EventFilter.ITEM);

        assertFalse(node.hasListener(EventNodeTest.ItemTestEvent.class));
        itemNode.addListener(EventNodeTest.ItemTestEvent.class, event -> result.set(true));
        assertTrue(node.hasListener(EventNodeTest.ItemTestEvent.class));

        node.call(new EventNodeTest.ItemTestEvent(item));
        assertTrue(result.get());

        result.set(false);
        node.call(new EventNodeTest.ItemTestEvent(ItemStack.of(Material.GOLD_INGOT)));
        assertFalse(result.get());

        result.set(false);
        node.unmap(item);
        node.call(new EventNodeTest.ItemTestEvent(item));
        assertFalse(result.get());
    }

    @Test
    public void entityLocal() {
        MinecraftServer minecraftServer = MinecraftServer.of(ServerSettings.builder().build());
        var node = minecraftServer.getGlobalEventHandler();
        var entity = new Entity(minecraftServer, EntityType.ZOMBIE);

        AtomicBoolean result = new AtomicBoolean(false);
        var listener = EventListener.of(EventNodeTest.EntityTestEvent.class, event -> result.set(true));

        var handle = node.getHandle(EventNodeTest.EntityTestEvent.class);
        assertFalse(handle.hasListener());
        entity.getEventNode().addListener(listener);
        assertTrue(handle.hasListener());

        assertFalse(result.get());

        handle.call(new EventNodeTest.EntityTestEvent(entity));
        assertTrue(result.get());

        result.set(false);
        entity.getEventNode().removeListener(listener);

        handle.call(new EventNodeTest.EntityTestEvent(entity));
        assertFalse(result.get());
    }

    @Test
    public void ownerGC() {
        MinecraftServer minecraftServer = MinecraftServer.of(ServerSettings.builder().build());
        // Ensure that the mapped object gets GCed
        var item = ItemStack.of(Material.DIAMOND);
        var node = EventNode.all(minecraftServer,"main");
        var itemNode = node.map(item, EventFilter.ITEM);
        itemNode.addListener(EventNodeTest.ItemTestEvent.class, event -> {
        });
        node.call(new EventNodeTest.ItemTestEvent(item));

        var ref = new WeakReference<>(item);
        //noinspection UnusedAssignment
        item = null;
        waitUntilCleared(ref);
    }
}
