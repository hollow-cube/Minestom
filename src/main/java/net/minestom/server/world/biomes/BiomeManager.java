package net.minestom.server.world.biomes;

import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.validate.Check;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTType;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Allows servers to register custom dimensions. Also used during player joining to send the list of all existing dimensions.
 * <p>
 */
public final class BiomeManager {
    private final Map<Integer, Biome> biomes = new ConcurrentHashMap<>();

    public BiomeManager() {
        // Need to register plains for the client to work properly
        // Plains is always ID 0
        addBiome(BiomeImpl.get("minecraft:plains"));
    }

    public void loadVanillaBiomes() {
        for (BiomeImpl biome : BiomeImpl.values()) {
            if (biome.id() != 0) addBiome(biome);
        }
    }

    /**
     * Adds a new biome. This does NOT send the new list to players.
     *
     * @param biome the biome to add
     */
    public void addBiome(Biome biome) {
        Check.stateCondition(getByName(biome.namespace()) != null, "The biome " + biome.namespace() + " has already been registered");
        this.biomes.put(biome.id(), biome);
    }

    /**
     * Removes a biome. This does NOT send the new list to players.
     *
     * @param biome the biome to remove
     */
    public void removeBiome(Biome biome) {
        this.biomes.remove(biome.id());
    }

    /**
     * Returns an immutable copy of the biomes already registered.
     *
     * @return an immutable copy of the biomes already registered
     */
    public Collection<Biome> unmodifiableCollection() {
        return Collections.unmodifiableCollection(biomes.values());
    }

    /**
     * Gets a biome by its id.
     *
     * @param id the id of the biome
     * @return the {@link Biome} linked to this id
     */
    public Biome getById(int id) {
        return biomes.get(id);
    }

    public Biome getByName(NamespaceID namespaceID) {
        Biome biome = null;
        for (final Biome biomeT : biomes.values()) {
            if (biomeT.namespace().equals(namespaceID)) {
                biome = biomeT;
                break;
            }
        }
        return biome;
    }

    public NBTCompound toNBT() {
        return NBT.Compound(Map.of(
                "type", NBT.String("minecraft:worldgen/biome"),
                "value", NBT.List(NBTType.TAG_Compound, biomes.values().stream().map(Biome::toNbt).toList())));
    }
}
