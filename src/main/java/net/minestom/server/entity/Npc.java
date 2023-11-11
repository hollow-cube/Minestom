package net.minestom.server.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.fakeplayer.FakePlayer;
import net.minestom.server.entity.fakeplayer.FakePlayerOption;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class Npc extends FakePlayer {
    public Npc(@NotNull Instance instance,
               @NotNull Pos position,
               @NotNull String username,
               @Nullable PlayerSkin skin) {
        super(UUID.randomUUID(), username, getFakePlayerOption(), fp -> {
            if (!instance.equals(fp.getInstance())){
                fp.setInstance(instance, position);
            } else {
                fp.refreshPosition(position);
            }
            fp.setSkin(skin);
            fp.setNoGravity(true);
            fp.setInvulnerable(true);
        });

        var eventNode = instance.eventNode();
        eventNode.addListener(PlayerEntityInteractEvent.class, event -> {
            if (event.getTarget().equals(this) && event.getHand() == Hand.MAIN) {
                onInteract(event.getPlayer());
            }
        });
    }

    protected abstract void onInteract(Player player);

    private static FakePlayerOption getFakePlayerOption() {
        return new FakePlayerOption().setRegistered(false).setInTabList(false);
    }
}
