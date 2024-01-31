package net.minestom.server.world.biomes;

import net.minestom.server.utils.NamespaceID;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Collection;

/**
 * Allows servers to register custom dimensions. Also used during player joining to send the list of all existing dimensions.
 * <p>
 * Contains {@link Biome#PLAINS} by default but can be removed.
 */
public interface BiomeManager {

    /**
     * Adds a new biome. This does NOT send the new list to players.
     *
     * @param biome the biome to add
     */
    void addBiome(Biome biome);

    /**
     * Removes a biome. This does NOT send the new list to players.
     *
     * @param biome the biome to remove
     */
    void removeBiome(Biome biome);

    /**
     * Returns an immutable copy of the biomes already registered.
     *
     * @return an immutable copy of the biomes already registered
     */
    Collection<Biome> unmodifiableCollection();

    /**
     * Gets a biome by its id.
     *
     * @param id the id of the biome
     * @return the {@link Biome} linked to this id
     */
    Biome getById(int id);

    Biome getByName(NamespaceID namespaceID);

    NBTCompound toNBT();
}
