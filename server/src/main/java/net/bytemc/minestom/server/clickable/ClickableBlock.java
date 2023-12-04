package net.bytemc.minestom.server.clickable;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerStartDiggingEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class ClickableBlock {
    private static final List<ClickableBlock> CLICKABLE_BLOCKS_LIST = new ArrayList<>();

    static {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockBreakEvent.class, event -> {
            for (ClickableBlock block : CLICKABLE_BLOCKS_LIST) {
                if (block.instance.equals(event.getInstance()) && block.pos.sameBlock(event.getBlockPosition())) {
                    block.breakCallback.forEach(callback -> callback.accept(event.getPlayer()));
                    break;
                }
            }
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerStartDiggingEvent.class, event -> {
            for (ClickableBlock block : CLICKABLE_BLOCKS_LIST) {
                if (block.instance.equals(event.getInstance()) && block.pos.sameBlock(event.getBlockPosition())) {
                    block.diggingCallback.forEach(callback -> callback.accept(event.getPlayer()));
                    break;
                }
            }
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockInteractEvent.class, event -> {
            if (event.getHand() != Player.Hand.MAIN) {
                return;
            }
            for (ClickableBlock block : CLICKABLE_BLOCKS_LIST) {
                if (block.instance.equals(event.getInstance()) && block.pos.sameBlock(event.getBlockPosition())) {
                    block.interactCallback.forEach(callback -> callback.accept(event.getPlayer()));
                    break;
                }
            }
        });
    }

    @NotNull
    private final Instance instance;
    @NotNull
    private final Point pos;

    private final List<Consumer<Player>> breakCallback = new ArrayList<>();
    private final List<Consumer<Player>> diggingCallback = new ArrayList<>();
    private final List<Consumer<Player>> interactCallback = new ArrayList<>();

    public ClickableBlock(@NotNull Point pos, @NotNull Instance instance) {
        CLICKABLE_BLOCKS_LIST.add(this);
        this.pos = pos;
        this.instance = instance;
    }

    public ClickableBlock(@NotNull Point pos, @NotNull Instance instance, @NotNull Block block) {
        this(pos, instance);
        instance.setBlock(pos, block);

    }

    public ClickableBlock(@NotNull Point pos, @NotNull Instance instance, @NotNull Block block, boolean blockUpdate) {
        this(pos, instance);
        instance.setBlock(pos, block, blockUpdate);
    }

    public ClickableBlock addBreakCallback(Consumer<Player> callback) {
        breakCallback.add(callback);
        return this;
    }

    public ClickableBlock addDiggingCallback(Consumer<Player> callback) {
        diggingCallback.add(callback);
        return this;
    }

    public ClickableBlock addInteractCallback(Consumer<Player> callback) {
        interactCallback.add(callback);
        return this;
    }

    public void remove() {
        CLICKABLE_BLOCKS_LIST.remove(this);
    }
}
