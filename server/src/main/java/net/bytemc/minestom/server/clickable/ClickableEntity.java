package net.bytemc.minestom.server.clickable;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClickableEntity {
    private static final List<ClickableEntity> ENTITY_LIST = new ArrayList<>();

    static {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerEntityInteractEvent.class, event -> {
            for (var entity : ENTITY_LIST) {
                if(event.getTarget().getUuid().equals(entity.entity.getUuid())) {
                    entity.interactionPlayer.accept(event.getPlayer());
                    break;
                }
            }
        });
    }

    private final Entity entity;
    private final Consumer<Player> interactionPlayer;

    public ClickableEntity(EntityType entity, Consumer<Player> interactionPlayer) {
        this.entity = new Entity(entity);
        this.interactionPlayer = interactionPlayer;

        ENTITY_LIST.add(this);
    }

    public ClickableEntity modify(Consumer<Entity> entityCallback) {
        entityCallback.accept(entity);
        return this;
    }

    public void spawn(Pos pos, Instance instance) {
        entity.setInstance(instance, pos).whenComplete((unused, throwable) -> {
            entity.spawn();
            entity.refreshPosition(pos);
        });
    }

    public void remove() {
        entity.remove();
        ENTITY_LIST.remove(this);
    }
}
