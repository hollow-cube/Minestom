package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.RemoveEntityFromInstanceEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.BlockEntityDataPacket;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTString;
import org.jglrxavpok.hephaistos.nbt.NBTType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Sign {
    private static final int LINES = 4;
    @NotNull
    private final Instance instance;
    @NotNull
    private final Point position;
    @NotNull
    private final Map<@NotNull Player, @Nullable NBTCompound> playerTextNbtMap =
            Collections.synchronizedMap(new HashMap<>());
    @Nullable
    private Function<@NotNull Player, @Nullable TextComponent[]> playerTextProvider;
    private final Loader loader;
    private final Block originalBlock;
    private final EventNode<InstanceEvent> playerRemoveEvent;
    private final EventNode<InstanceEvent> breakBlockEvent;
    private boolean staticText = true;

    private Sign(@NotNull Instance instance, @NotNull Point position) {
        this.instance = instance;
        this.position = position.blockCenter();
        this.originalBlock = instance.getBlock(position);
        loader = new Loader(instance, this.position);
        playerRemoveEvent = instance.eventNode().addListener(
                RemoveEntityFromInstanceEvent.class, event -> {
                    if (event.getEntity() instanceof Player player) {
                        playerTextNbtMap.remove(player);
                    }
                });
        breakBlockEvent = instance.eventNode().addListener(PlayerBlockBreakEvent.class, e -> {
            if (e.getBlockPosition().sameBlock(position)) {
                e.setCancelled(true);
                instance.scheduler().scheduleNextTick(
                        () -> updateTextForPlayer(e.getPlayer()));
            }
        });
    }

    public void updateTextForPlayer(@NotNull Player player) {
        if (staticText) {
            return;
        }
        NBTCompound nbt;
        if (playerTextProvider == null) {
            nbt = playerTextNbtMap.getOrDefault(player, NBTCompound.EMPTY);
        } else {
            nbt = getNbt(playerTextProvider.apply(player));
        }
        int entityId = instance.getBlock(position)
                .registry().blockEntityId();
        player.sendPacket(new BlockEntityDataPacket(position, entityId, nbt));
    }

    public void updateTextForAllPlayers() {
        loader.getViewers().forEach(this::updateTextForPlayer);
    }

    public void setTextForPlayer(@NotNull Player player, @Nullable TextComponent[] lines) {
        var nbt = getNbt(lines);
        playerTextNbtMap.put(player, nbt);
        playerTextProvider = null;
        staticText = false;
        updateTextForPlayer(player);
    }

    public void setTextForPlayers(@Nullable Function<@NotNull Player, @Nullable TextComponent[]>
                                          playerTextProvider) {
        this.playerTextProvider = playerTextProvider;
        staticText = false;
        playerTextNbtMap.clear();
        updateTextForAllPlayers();
    }

    public void setText(@Nullable TextComponent[] lines) {
        instance.setBlock(position,
                instance.getBlock(position).withNbt(getNbt(lines)));
        playerTextProvider = null;
        playerTextNbtMap.clear();
        staticText = true;
    }

    public void clearText() {
        setText(null);
    }

    public void remove() {
        loader.remove();
        var currentBlock = instance.getBlock(position);
        if (currentBlock.id() == originalBlock.id() &&
                currentBlock.stateId() == originalBlock.stateId()) {
            instance.setBlock(position, Block.AIR);
        }
        instance.eventNode().removeChild(playerRemoveEvent);
        instance.eventNode().removeChild(breakBlockEvent);
    }

    public static Sign createSign(@NotNull Instance instance,
                                  @NotNull Point position,
                                  @NotNull SignType signType,
                                  SignRotation rotation) {
        var block = signType.normal.withProperty("rotation",
                "" + rotation.ordinal());
        instance.setBlock(position, block);
        return new Sign(instance, position);
    }

    public static Sign createWallSign(@NotNull Instance instance,
                                      @NotNull Point position,
                                      @NotNull SignType signType,
                                      @NotNull Direction direction) {
        if (!direction.isHorizontal()) {
            throw new IllegalArgumentException("direction of sign must be horizontal");
        }
        var block = signType.wall.withProperty("facing",
                direction.getFacingProperty());
        instance.setBlock(position, block);
        return new Sign(instance, position);
    }

    @NotNull
    private static NBTCompound getNbt(@Nullable TextComponent[] lines) {
        if (lines == null) {
            lines = new TextComponent[LINES];
        }
        assert lines.length == LINES;
        var linesGson = new NBTString[LINES];
        for (int i = 0; i < LINES; i++) {
            var line = lines[i];
            if (line == null) line = Component.text("");
            linesGson[i] = new NBTString(GsonComponentSerializer.gson().serialize(line));
        }
        var frontMap = new HashMap<String, NBT>();
        frontMap.put("messages", NBT.List(NBTType.TAG_String, Arrays.asList(linesGson)));
        var map = new HashMap<String, NBT>();
        map.put("front_text", NBT.Compound(frontMap));
        return NBT.Compound(map);
    }

    public enum SignType {
        OAK(Block.OAK_SIGN, Block.OAK_WALL_SIGN),
        SPRUCE(Block.SPRUCE_SIGN, Block.SPRUCE_WALL_SIGN),
        BIRCH(Block.BIRCH_SIGN, Block.BIRCH_WALL_SIGN),
        JUNGLE(Block.JUNGLE_SIGN, Block.JUNGLE_WALL_SIGN),
        ACACIA(Block.ACACIA_SIGN, Block.ACACIA_WALL_SIGN),
        DARK_OAK(Block.DARK_OAK_SIGN, Block.DARK_OAK_WALL_SIGN),
        MANGROVE(Block.MANGROVE_SIGN, Block.MANGROVE_WALL_SIGN),
        CHERRY(Block.CHERRY_SIGN, Block.CHERRY_WALL_SIGN),
        BAMBOO(Block.BAMBOO_SIGN, Block.BAMBOO_WALL_SIGN),
        CRIMSON(Block.CRIMSON_SIGN, Block.CRIMSON_WALL_SIGN),
        WARPED(Block.WARPED_SIGN, Block.WARPED_WALL_SIGN),
        HANGING_OAK(Block.OAK_HANGING_SIGN, Block.OAK_WALL_HANGING_SIGN),
        HANGING_SPRUCE(Block.SPRUCE_HANGING_SIGN, Block.SPRUCE_WALL_HANGING_SIGN),
        HANGING_BIRCH(Block.BIRCH_HANGING_SIGN, Block.BIRCH_WALL_HANGING_SIGN),
        HANGING_JUNGLE(Block.JUNGLE_HANGING_SIGN, Block.JUNGLE_WALL_HANGING_SIGN),
        HANGING_ACACIA(Block.ACACIA_HANGING_SIGN, Block.ACACIA_WALL_HANGING_SIGN),
        HANGING_DARK_OAK(Block.DARK_OAK_HANGING_SIGN, Block.DARK_OAK_WALL_HANGING_SIGN),
        HANGING_MANGROVE(Block.MANGROVE_HANGING_SIGN, Block.MANGROVE_WALL_HANGING_SIGN),
        HANGING_CHERRY(Block.CHERRY_HANGING_SIGN, Block.CHERRY_WALL_HANGING_SIGN),
        HANGING_BAMBOO(Block.BAMBOO_HANGING_SIGN, Block.BAMBOO_WALL_HANGING_SIGN),
        HANGING_CRIMSON(Block.CRIMSON_HANGING_SIGN, Block.CRIMSON_WALL_HANGING_SIGN),
        HANGING_WARPED(Block.WARPED_HANGING_SIGN, Block.WARPED_WALL_HANGING_SIGN);

        private final Block normal;
        private final Block wall;

        SignType(Block normal, Block wall) {
            this.normal = normal;
            this.wall = wall;
        }
    }

    public enum SignRotation {
        // The direction the sign will be facing
        SOUTH,
        S_SW,
        SW,
        W_SW,
        WEST,
        W_NW,
        NW,
        N_NW,
        NORTH,
        N_NE,
        NE,
        E_NE,
        EAST,
        E_SE,
        SE,
        S_SE;
        private static final double ANGLE_BETWEEN = 360. / values().length;

        public SignRotation opposite() {
            return values()[(ordinal() + values().length / 2) % values().length];
        }

        public static SignRotation fromPoint(Point direction) {
            double angle = Math.toDegrees(Math.atan2(direction.z(), direction.x()));
            angle += 270 + ANGLE_BETWEEN / 2;
            return values()[((int) (angle / ANGLE_BETWEEN)) % values().length];
        }

        public static SignRotation fromDirection(Direction direction) {
            return switch (direction) {
                case NORTH -> NORTH;
                case SOUTH -> SOUTH;
                case WEST -> WEST;
                case EAST -> EAST;
                default -> throw new RuntimeException();
            };
        }
    }

    private class Loader extends EntityCreature {
        public Loader(Instance instance, Point position) {
            super(EntityType.ARMOR_STAND);
            var meta = (ArmorStandMeta) getEntityMeta();
            meta.setMarker(true);
            meta.setInvisible(true);
            meta.setHasNoGravity(true);
            setInstance(instance, position);
        }

        @Override
        public void updateNewViewer(@NotNull Player player) {
            scheduler().scheduleNextTick(() -> updateTextForPlayer(player));
        }
    }
}
