package net.minestom.server.world.biomes;

import net.minestom.server.utils.NamespaceID;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTType;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BiomeManagerImpl implements BiomeManager {
    private final Map<Integer, Biome> biomes = new ConcurrentHashMap<>();

    public BiomeManagerImpl() {
        addBiome(Biome.PLAINS);
    }

    @Override
    public void addBiome(Biome biome) {
        this.biomes.put(biome.id(), biome);
    }

    @Override
    public void removeBiome(Biome biome) {
        this.biomes.remove(biome.id());
    }

    @Override
    public Collection<Biome> unmodifiableCollection() {
        return Collections.unmodifiableCollection(biomes.values());
    }

    @Override
    public Biome getById(int id) {
        return biomes.get(id);
    }

    @Override
    public Biome getByName(NamespaceID namespaceID) {
        Biome biome = null;
        for (final Biome biomeT : biomes.values()) {
            if (biomeT.name().equals(namespaceID)) {
                biome = biomeT;
                break;
            }
        }
        return biome;
    }

    @Override
    public NBTCompound toNBT() {
        return NBT.Compound(Map.of(
                "type", NBT.String("minecraft:worldgen/biome"),
                "value", NBT.List(NBTType.TAG_Compound, biomes.values().stream().map(Biome::toNbt).toList())));
    }
}
