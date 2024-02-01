package net.minestom.server.instance;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.pointer.Pointers;
import net.minestom.server.ServerSettings;
import net.minestom.server.ServerSettingsProvider;
import net.minestom.server.Tickable;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ExperienceOrb;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.pathfinding.PFInstanceSpace;
import net.minestom.server.event.*;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.network.packet.server.play.BlockActionPacket;
import net.minestom.server.network.packet.server.play.TimeUpdatePacket;
import net.minestom.server.snapshot.InstanceSnapshot;
import net.minestom.server.snapshot.SnapshotUpdater;
import net.minestom.server.snapshot.Snapshotable;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.Taggable;
import net.minestom.server.thread.ThreadDispatcherImpl;
import net.minestom.server.timer.Schedulable;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.chunk.ChunkCache;
import net.minestom.server.utils.chunk.ChunkSupplier;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.time.Cooldown;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Instances are what are called "worlds" in Minecraft, you can add an entity in it using {@link Entity#setInstance(Instance)}.
 * <p>
 * An instance has entities and chunks, each instance contains its own entity list but the
 * chunk implementation has to be defined, see {@link InstanceContainer}.
 * <p>
 * WARNING: when making your own implementation registering the instance manually is required
 * with {@link InstanceManagerImpl#registerInstance(Instance)}, and
 * you need to be sure to signal the {@link ThreadDispatcherImpl} of every partition/element changes.
 */
public abstract class Instance implements Block.Getter, Block.Setter, Tickable, Schedulable, Snapshotable, EventHandler<InstanceEvent>, Taggable, PacketGroupingAudience {

    private final ServerSettingsProvider serverSettingsProvider;
    private final EventNode<Event> globalEventHandler;

    private boolean registered;

    private final DimensionType dimensionType;
    private final NamespaceID dimensionName;

    private final WorldBorder worldBorder;

    // Tick since the creation of the instance
    private long worldAge;

    // The time of the instance
    private long time;

    private int timeRate = 1;

    private Duration timeUpdate = Duration.of(1, TimeUnit.SECOND);
    private long lastTimeUpdate;

    // Field for tick events
    private long lastTickAge = System.currentTimeMillis();

    private final EntityTracker entityTracker;

    private final ChunkCache blockRetriever = new ChunkCache(this, null, null);

    // the uuid of this instance
    protected UUID uniqueId;

    // instance custom data
    protected TagHandler tagHandler = TagHandler.newHandler();
    private final Scheduler scheduler = Scheduler.newScheduler();
    private final EventNode<InstanceEvent> eventNode;

    // the explosion supplier
    private ExplosionSupplier explosionSupplier;

    // Pathfinder
    private final PFInstanceSpace instanceSpace = new PFInstanceSpace(this);

    // Adventure
    private final Pointers pointers;

    /**
     * Creates a new instance.
     *
     * @param uniqueId      the {@link UUID} of the instance
     * @param dimensionType the {@link DimensionType} of the instance
     */
    public Instance(
            @NotNull GlobalEventHandler globalEventHandler,
            @NotNull ServerSettingsProvider serverSettingsProvider,
            @NotNull UUID uniqueId,
            @NotNull DimensionType dimensionType
    ) {
        this(globalEventHandler, serverSettingsProvider, uniqueId, dimensionType, dimensionType.getName());
    }

    /**
     * Creates a new instance.
     *
     * @param uniqueId      the {@link UUID} of the instance
     * @param dimensionType the {@link DimensionType} of the instance
     */
    public Instance(
            @NotNull GlobalEventHandler globalEventHandler,
            @NotNull ServerSettingsProvider serverSettingsProvider,

            @NotNull UUID uniqueId,
            @NotNull DimensionType dimensionType,
            @NotNull NamespaceID dimensionName
    ) {
        this.globalEventHandler = globalEventHandler;
        Check.argCondition(!dimensionType.isRegistered(),
                "The dimension " + dimensionType.getName() + " is not registered! Please use DimensionTypeManager#addDimension");
        this.serverSettingsProvider = serverSettingsProvider;
        this.uniqueId = uniqueId;
        this.dimensionType = dimensionType;
        this.dimensionName = dimensionName;

        this.worldBorder = new WorldBorder(this);

        this.pointers = Pointers.builder()
                .withDynamic(Identity.UUID, this::getUniqueId)
                .build();

        this.eventNode = globalEventHandler.map(this, EventFilter.INSTANCE);
        entityTracker = new EntityTrackerImpl(serverSettingsProvider);
    }

    /**
     * Schedules a task to be run during the next instance tick.
     *
     * @param callback the task to execute during the next instance tick
     */
    public void scheduleNextTick(@NotNull Consumer<Instance> callback) {
        this.scheduler.scheduleNextTick(() -> callback.accept(this));
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        setBlock(x, y, z, block, true);
    }

    public void setBlock(@NotNull Point blockPosition, @NotNull Block block, boolean doBlockUpdates) {
        setBlock(blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ(), block, doBlockUpdates);
    }

    public abstract void setBlock(int x, int y, int z, @NotNull Block block, boolean doBlockUpdates);

    @ApiStatus.Internal
    public boolean placeBlock(@NotNull BlockHandler.Placement placement) {
        return placeBlock(placement, true);
    }

    @ApiStatus.Internal
    public abstract boolean placeBlock(@NotNull BlockHandler.Placement placement, boolean doBlockUpdates);

    /**
     * Does call {@link PlayerBlockBreakEvent}
     * and send particle packets
     *
     * @param player        the {@link Player} who break the block
     * @param blockPosition the position of the broken block
     * @return true if the block has been broken, false if it has been cancelled
     */
    @ApiStatus.Internal
    public boolean breakBlock(@NotNull Player player, @NotNull Point blockPosition, @NotNull BlockFace blockFace) {
        return breakBlock(player, blockPosition, blockFace, true);
    }

    /**
     * Does call {@link PlayerBlockBreakEvent}
     * and send particle packets
     *
     * @param player         the {@link Player} who break the block
     * @param blockPosition  the position of the broken block
     * @param doBlockUpdates true to do block updates, false otherwise
     * @return true if the block has been broken, false if it has been cancelled
     */
    @ApiStatus.Internal
    public abstract boolean breakBlock(@NotNull Player player, @NotNull Point blockPosition, @NotNull BlockFace blockFace, boolean doBlockUpdates);

    /**
     * Forces the generation of a {@link Chunk}, even if no file and {@link ChunkGenerator} are defined.
     *
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return a {@link CompletableFuture} completed once the chunk has been loaded
     */
    public abstract @NotNull CompletableFuture<@NotNull Chunk> loadChunk(int chunkX, int chunkZ);

    /**
     * Loads the chunk at the given {@link Point} with a callback.
     *
     * @param point the chunk position
     */
    public @NotNull CompletableFuture<@NotNull Chunk> loadChunk(@NotNull Point point) {
        return loadChunk(point.chunkX(), point.chunkZ());
    }

    /**
     * Loads the chunk if the chunk is already loaded or if
     * {@link #hasEnabledAutoChunkLoad()} returns true.
     *
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return a {@link CompletableFuture} completed once the chunk has been processed, can be null if not loaded
     */
    public abstract @NotNull CompletableFuture<@Nullable Chunk> loadOptionalChunk(int chunkX, int chunkZ);

    /**
     * Loads a {@link Chunk} (if {@link #hasEnabledAutoChunkLoad()} returns true)
     * at the given {@link Point} with a callback.
     *
     * @param point the chunk position
     * @return a {@link CompletableFuture} completed once the chunk has been processed, null if not loaded
     */
    public @NotNull CompletableFuture<@Nullable Chunk> loadOptionalChunk(@NotNull Point point) {
        return loadOptionalChunk(point.chunkX(), point.chunkZ());
    }

    /**
     * Schedules the removal of a {@link Chunk}, this method does not promise when it will be done.
     * <p>
     * WARNING: during unloading, all entities other than {@link Player} will be removed.
     *
     * @param chunk the chunk to unload
     */
    public abstract void unloadChunk(@NotNull Chunk chunk);

    /**
     * Unloads the chunk at the given position.
     *
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     */
    public void unloadChunk(int chunkX, int chunkZ) {
        final Chunk chunk = getChunk(chunkX, chunkZ);
        Check.notNull(chunk, "The chunk at {0}:{1} is already unloaded", chunkX, chunkZ);
        unloadChunk(chunk);
    }

    /**
     * Gets the loaded {@link Chunk} at a position.
     * <p>
     * WARNING: this should only return already-loaded chunk, use {@link #loadChunk(int, int)} or similar to load one instead.
     *
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return the chunk at the specified position, null if not loaded
     */
    public abstract @Nullable Chunk getChunk(int chunkX, int chunkZ);

    /**
     * @param chunkX the chunk X
     * @param chunkZ this chunk Z
     * @return true if the chunk is loaded
     */
    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        return getChunk(chunkX, chunkZ) != null;
    }

    /**
     * @param point coordinate of a block or other
     * @return true if the chunk is loaded
     */
    public boolean isChunkLoaded(Point point) {
        return isChunkLoaded(point.chunkX(), point.chunkZ());
    }

    /**
     * Saves the current instance tags.
     * <p>
     * Warning: only the global instance data will be saved, not chunks.
     * You would need to call {@link #saveChunksToStorage()} too.
     *
     * @return the future called once the instance data has been saved
     */
    @ApiStatus.Experimental
    public abstract @NotNull CompletableFuture<Void> saveInstance();

    /**
     * Saves a {@link Chunk} to permanent storage.
     *
     * @param chunk the {@link Chunk} to save
     * @return future called when the chunk is done saving
     */
    public abstract @NotNull CompletableFuture<Void> saveChunkToStorage(@NotNull Chunk chunk);

    /**
     * Saves multiple chunks to permanent storage.
     *
     * @return future called when the chunks are done saving
     */
    public abstract @NotNull CompletableFuture<Void> saveChunksToStorage();

    /**
     * Changes the instance {@link ChunkGenerator}.
     *
     * @param chunkGenerator the new {@link ChunkGenerator} of the instance
     * @deprecated Use {@link #setGenerator(Generator)}
     */
    @Deprecated
    public void setChunkGenerator(@Nullable ChunkGenerator chunkGenerator) {
        setGenerator(chunkGenerator != null ? new ChunkGeneratorCompatibilityLayer(chunkGenerator) : null);
    }

    public abstract void setChunkSupplier(@NotNull ChunkSupplier chunkSupplier);

    /**
     * Gets the chunk supplier of the instance.
     *
     * @return the chunk supplier of the instance
     */
    public abstract ChunkSupplier getChunkSupplier();

    /**
     * Gets the generator associated with the instance
     *
     * @return the generator if any
     */
    public abstract @Nullable Generator generator();

    /**
     * Changes the generator of the instance
     *
     * @param generator the new generator, or null to disable generation
     */
    public abstract void setGenerator(@Nullable Generator generator);

    /**
     * Gets all the instance's loaded chunks.
     *
     * @return an unmodifiable containing all the instance chunks
     */
    public abstract @NotNull Collection<@NotNull Chunk> getChunks();

    /**
     * When set to true, chunks will load automatically when requested.
     * Otherwise using {@link #loadChunk(int, int)} will be required to even spawn a player
     *
     * @param enable enable the auto chunk load
     */
    public abstract void enableAutoChunkLoad(boolean enable);

    /**
     * Gets if the instance should auto load chunks.
     *
     * @return true if auto chunk load is enabled, false otherwise
     */
    public abstract boolean hasEnabledAutoChunkLoad();

    /**
     * Determines whether a position in the void.
     *
     * @param point the point in the world
     * @return true if the point is inside the void
     */
    public abstract boolean isInVoid(@NotNull Point point);

    /**
     * Changes the current time in the instance, from 0 to 24000.
     * <p>
     * If the time is negative, the vanilla client will not move the sun.
     * <p>
     * 0 = sunrise
     * 6000 = noon
     * 12000 = sunset
     * 18000 = midnight
     * <p>
     * This method is unaffected by {@link #getTimeRate()}
     * <p>
     * It does send the new time to all players in the instance, unaffected by {@link #getTimeUpdate()}
     *
     * @param time the new time of the instance
     */
    public void setTime(long time) {
        this.time = time;
        PacketUtils.sendGroupedPacket(serverSettingsProvider, getPlayers(), createTimePacket());
    }

    /**
     * Changes the time rate of the instance
     * <p>
     * 1 is the default value and can be set to 0 to be completely disabled (constant time)
     *
     * @param timeRate the new time rate of the instance
     * @throws IllegalStateException if {@code timeRate} is lower than 0
     */
    public void setTimeRate(int timeRate) {
        Check.stateCondition(timeRate < 0, "The time rate cannot be lower than 0");
        this.timeRate = timeRate;
    }

    /**
     * Creates a {@link TimeUpdatePacket} with the current age and time of this instance
     *
     * @return the {@link TimeUpdatePacket} with this instance data
     */
    @ApiStatus.Internal
    public @NotNull TimeUpdatePacket createTimePacket() {
        long time = this.time;
        if (timeRate == 0) {
            //Negative values stop the sun and moon from moving
            //0 as a long cannot be negative
            time = time == 0 ? -24000L : -Math.abs(time);
        }
        return new TimeUpdatePacket(worldAge, time);
    }

    /**
     * Gets the entities in the instance;
     *
     * @return an unmodifiable {@link Set} containing all the entities in the instance
     */
    public @NotNull Set<@NotNull Entity> getEntities() {
        return entityTracker.entities();
    }

    /**
     * Gets the players in the instance;
     *
     * @return an unmodifiable {@link Set} containing all the players in the instance
     */
    @Override
    public @NotNull Set<@NotNull Player> getPlayers() {
        return entityTracker.entities(EntityTracker.Target.PLAYERS);
    }

    /**
     * Gets the creatures in the instance;
     *
     * @return an unmodifiable {@link Set} containing all the creatures in the instance
     */
    @Deprecated
    public @NotNull Set<@NotNull EntityCreature> getCreatures() {
        return entityTracker.entities().stream()
                .filter(EntityCreature.class::isInstance)
                .map(entity -> (EntityCreature) entity)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Gets the experience orbs in the instance.
     *
     * @return an unmodifiable {@link Set} containing all the experience orbs in the instance
     */
    @Deprecated
    public @NotNull Set<@NotNull ExperienceOrb> getExperienceOrbs() {
        return entityTracker.entities().stream()
                .filter(ExperienceOrb.class::isInstance)
                .map(entity -> (ExperienceOrb) entity)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Gets the entities located in the chunk.
     *
     * @param chunk the chunk to get the entities from
     * @return an unmodifiable {@link Set} containing all the entities in a chunk,
     * if {@code chunk} is unloaded, return an empty {@link HashSet}
     */
    public @NotNull Set<@NotNull Entity> getChunkEntities(Chunk chunk) {
        var chunkEntities = entityTracker.chunkEntities(chunk.toPosition(), EntityTracker.Target.ENTITIES);
        return ObjectArraySet.ofUnchecked(chunkEntities.toArray(Entity[]::new));
    }

    /**
     * Gets nearby entities to the given position.
     *
     * @param point position to look at
     * @param range max range from the given point to collect entities at
     * @return entities that are not further than the specified distance from the transmitted position.
     */
    public @NotNull Collection<Entity> getNearbyEntities(@NotNull Point point, double range) {
        List<Entity> result = new ArrayList<>();
        this.entityTracker.nearbyEntities(point, range, EntityTracker.Target.ENTITIES, result::add);
        return result;
    }

    @Override
    public @Nullable Block getBlock(int x, int y, int z, @NotNull Condition condition) {
        final Block block = blockRetriever.getBlock(x, y, z, condition);
        if (block == null) throw new NullPointerException("Unloaded chunk at " + x + "," + y + "," + z);
        return block;
    }

    /**
     * Sends a {@link BlockActionPacket} for all the viewers of the specific position.
     *
     * @param blockPosition the block position
     * @param actionId      the action id, depends on the block
     * @param actionParam   the action parameter, depends on the block
     * @see <a href="https://wiki.vg/Protocol#Block_Action">BlockActionPacket</a> for the action id &amp; param
     */
    public void sendBlockAction(@NotNull Point blockPosition, byte actionId, byte actionParam) {
        final Block block = getBlock(blockPosition);
        final Chunk chunk = getChunkAt(blockPosition);
        Check.notNull(chunk, "The chunk at {0} is not loaded!", blockPosition);
        chunk.sendPacketToViewers(serverSettingsProvider, new BlockActionPacket(blockPosition, actionId, actionParam, block));
    }

    /**
     * Gets the {@link Chunk} at the given block position, null if not loaded.
     *
     * @param x the X position
     * @param z the Z position
     * @return the chunk at the given position, null if not loaded
     */
    public @Nullable Chunk getChunkAt(double x, double z) {
        return getChunk(ChunkUtils.getChunkCoordinate(x), ChunkUtils.getChunkCoordinate(z));
    }

    /**
     * Gets the {@link Chunk} at the given {@link Point}, null if not loaded.
     *
     * @param point the position
     * @return the chunk at the given position, null if not loaded
     */
    public @Nullable Chunk getChunkAt(@NotNull Point point) {
        return getChunk(point.chunkX(), point.chunkZ());
    }

    /**
     * Performs a single tick in the instance, including scheduled tasks from {@link #scheduleNextTick(Consumer)}.
     * <p>
     * Warning: this does not update chunks and entities.
     *
     * @param time the tick time in milliseconds
     */
    @Override
    public void tick(long time) {
        // Scheduled tasks
        this.scheduler.processTick();
        // Time
        {
            this.worldAge++;
            this.time += timeRate;
            // time needs to be sent to players
            if (timeUpdate != null && !Cooldown.hasCooldown(time, lastTimeUpdate, timeUpdate)) {
                PacketUtils.sendGroupedPacket(serverSettingsProvider, getPlayers(), createTimePacket());
                this.lastTimeUpdate = time;
            }

        }
        // Tick event
        {
            // Process tick events
            globalEventHandler.call(new InstanceTickEvent(this, time, lastTickAge));
            // Set last tick age
            this.lastTickAge = time;
        }
        this.worldBorder.update();
    }

    @Override
    public @NotNull InstanceSnapshot updateSnapshot(@NotNull SnapshotUpdater updater) {
        throw new RuntimeException("Not implemented"); // FIXME
//        final Map<Long, AtomicReference<ChunkSnapshot>> chunksMap = updater.referencesMapLong(getChunks(), ChunkUtils::getChunkIndex);
//        final int[] entities = ArrayUtils.mapToIntArray(entityTracker.entities(), Entity::getEntityId);
//        return new SnapshotImpl.Instance(updater.reference(serverProcess),
//                getDimensionType(), getWorldAge(), getTime(), chunksMap, entities,
//                tagHandler.readableCopy());
    }

    /**
     * Creates an explosion at the given position with the given strength.
     * The algorithm used to compute damages is provided by {@link #getExplosionSupplier()}.
     *
     * @param centerX  the center X
     * @param centerY  the center Y
     * @param centerZ  the center Z
     * @param strength the strength of the explosion
     * @throws IllegalStateException If no {@link ExplosionSupplier} was supplied
     */
    public void explode(float centerX, float centerY, float centerZ, float strength) {
        explode(centerX, centerY, centerZ, strength, null);
    }

    /**
     * Creates an explosion at the given position with the given strength.
     * The algorithm used to compute damages is provided by {@link #getExplosionSupplier()}.
     *
     * @param centerX        center X of the explosion
     * @param centerY        center Y of the explosion
     * @param centerZ        center Z of the explosion
     * @param strength       the strength of the explosion
     * @param additionalData data to pass to the explosion supplier
     * @throws IllegalStateException If no {@link ExplosionSupplier} was supplied
     */
    public void explode(float centerX, float centerY, float centerZ, float strength, @Nullable NBTCompound additionalData) {
        final ExplosionSupplier explosionSupplier = getExplosionSupplier();
        Check.stateCondition(explosionSupplier == null, "Tried to create an explosion with no explosion supplier");
        final Explosion explosion = explosionSupplier.createExplosion(centerX, centerY, centerZ, strength, additionalData);
        explosion.apply(this);
    }

    /**
     * Gets the registered {@link ExplosionSupplier}, or null if none was provided.
     *
     * @return the instance explosion supplier, null if none was provided
     */
    public @Nullable ExplosionSupplier getExplosionSupplier() {
        return explosionSupplier;
    }

    /**
     * Registers the {@link ExplosionSupplier} to use in this instance.
     *
     * @param supplier the explosion supplier
     */
    public void setExplosionSupplier(@Nullable ExplosionSupplier supplier) {
        this.explosionSupplier = supplier;
    }

    @Override
    public ServerSettings getServerSettings() {
        return serverSettingsProvider.getServerSettings();
    }

    public ServerSettingsProvider getServerSettingsProvider() {
        return this.serverSettingsProvider;
    }

    public boolean isRegistered() {
        return this.registered;
    }

    public DimensionType getDimensionType() {
        return this.dimensionType;
    }

    public NamespaceID getDimensionName() {
        return this.dimensionName;
    }

    public WorldBorder getWorldBorder() {
        return this.worldBorder;
    }

    public long getWorldAge() {
        return this.worldAge;
    }

    public long getTime() {
        return this.time;
    }

    public int getTimeRate() {
        return this.timeRate;
    }

    public Duration getTimeUpdate() {
        return this.timeUpdate;
    }

    public EntityTracker getEntityTracker() {
        return this.entityTracker;
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public TagHandler tagHandler() {
        return this.tagHandler;
    }

    public Scheduler getScheduler() {
        return this.scheduler;
    }

    public EventNode<InstanceEvent> getEventNode() {
        return this.eventNode;
    }

    public PFInstanceSpace getInstanceSpace() {
        return this.instanceSpace;
    }

    public Pointers pointers() {
        return this.pointers;
    }

    protected void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public void setTimeUpdate(Duration timeUpdate) {
        this.timeUpdate = timeUpdate;
    }
}
