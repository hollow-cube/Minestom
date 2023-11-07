package net.bytemc.minestom.server.clickable;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerStartDiggingEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClickableBlock {

    private static final List<ClickableBlock> CLICKABLE_BLOCKS_LIST = new ArrayList<>();

    static {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockBreakEvent.class, event -> {
            for (ClickableBlock block : CLICKABLE_BLOCKS_LIST) {
                if (block.getBlock() == event.getBlock()) {
                    block.breakCallback.forEach(callback -> callback.accept(block.getBlock()));
                    break;
                }
            }
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerStartDiggingEvent.class, event -> {
            for (ClickableBlock block : CLICKABLE_BLOCKS_LIST) {
                if (block.getBlock() == event.getBlock()) {
                    block.diggingCallback.forEach(callback -> callback.accept(block.getBlock()));
                    break;
                }
            }
        });
    }

    private Block block;

    private List<Consumer<Block>> breakCallback = new ArrayList<>();
    private List<Consumer<Block>> diggingCallback = new ArrayList<>();

    public ClickableBlock(Pos pos, Instance instance) {
        this();
        this.block = instance.getBlock(pos);
    }

    public ClickableBlock(Pos pos, Instance instance, Block block) {
        this();
        instance.setBlock(pos, block);
        this.block = instance.getBlock(pos);

    }

    public ClickableBlock(Pos pos, Instance instance, Block block, boolean blockUpdate) {
        this();
        instance.setBlock(pos, block, blockUpdate);
        this.block = instance.getBlock(pos);
    }

    public ClickableBlock addBreakCallback(Consumer<Block> callback) {
        breakCallback.add(callback);
        return this;
    }

    public ClickableBlock addDiggingCallback(Consumer<Block> callback) {
        diggingCallback.add(callback);
        return this;
    }

    public void remove() {
        CLICKABLE_BLOCKS_LIST.remove(this);
    }

    public ClickableBlock() {
        CLICKABLE_BLOCKS_LIST.add(this);
    }

    public Block getBlock() {
        return block;
    }
}
