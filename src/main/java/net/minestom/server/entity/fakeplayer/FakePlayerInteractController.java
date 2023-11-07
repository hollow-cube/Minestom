package net.minestom.server.entity.fakeplayer;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerEntityInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class FakePlayerInteractController {
    private final List<Consumer<Player>> consumers;

    public FakePlayerInteractController(FakePlayer fakePlayer) {
        this.consumers = new ArrayList<>();

        MinecraftServer.getGlobalEventHandler().addListener(PlayerEntityInteractEvent.class, event -> {
            if(event.getTarget().getUuid().equals(fakePlayer.getUuid())) {
                click(event.getPlayer());
            }
        });
    }

    public void subscribe(Consumer<Player> onClick) {
        this.consumers.add(onClick);
    }

    private void click(Player player) {
        this.consumers.forEach(it -> it.accept(player));
    }
}
