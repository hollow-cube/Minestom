package net.bytemc.minestom.server.hologram;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class Hologram {
    private final List<Entity> entities;

    private final Point pos;
    private final Instance instance;
    private final String[] lines;
    private final Predicate<Player> viewerRule;
    private final List<Consumer<Player>> onInteractList;

    public Hologram(Point pos, Instance instance, String... lines) {
        this(pos, instance, null, lines);
    }

    public Hologram(Point pos, Instance instance, Predicate<Player> viewerRule, String... lines) {
        this.entities = new ArrayList<>();

        this.pos = pos;
        this.instance = instance;
        this.viewerRule = viewerRule;
        this.lines = lines;
        this.onInteractList = new ArrayList<>();

        MinecraftServer.getGlobalEventHandler().addListener(PlayerEntityInteractEvent.class, event -> {
            if(entities.stream().anyMatch(it -> it.getUuid().equals(event.getEntity().getUuid()))) {
                onInteractList.forEach(it -> it.accept(event.getPlayer()));
            }
        });
    }

    public void subscribe(Consumer<Player> onInteract) {
        this.onInteractList.add(onInteract);
    }

    public void update(int id, String text) {
        var entity = entities.get(id);
        if(entity == null) {
            throw new RuntimeException("Hologram line with id " + id + " does not exist!");
        }
        entity.setCustomName(Component.text(text));
    }

    public void spawn() {
        for (int i = 0; i < lines.length; i++) {
            var text = lines[i];
            var entity = new EntityCreature(EntityType.ARMOR_STAND);
            var meta = (ArmorStandMeta) entity.getEntityMeta();

            entity.setNoGravity(true);
            entity.setInvisible(true);
            meta.setHasNoBasePlate(true);
            meta.setSmall(true);

            entity.setCustomNameVisible(true);
            entity.setCustomName(Component.text(text));

            if(viewerRule != null) {
                entity.updateViewableRule(viewerRule);
            }
            entity.setInstance(instance, pos.sub(0, (i / 3.0), 0)).whenComplete((unused, throwable) -> {
                entity.spawn();
            });
            this.entities.add(entity);
        }
    }

    public void destroy() {
        for (Entity entity : entities) {
            entity.remove();
        }
        entities.clear();
    }
}
