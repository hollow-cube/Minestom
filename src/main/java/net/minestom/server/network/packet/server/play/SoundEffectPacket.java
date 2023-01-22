package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.sound.Sound.Source;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.*;

public class SoundEffectPacket implements ServerPacket {
    private final SoundEvent soundEvent; // only one of soundEvent and soundName may be present
    private final String soundName;
    private final Float range; // optional
    private final Source source;
    private final int x;
    private final int y;
    private final int z;
    private final float volume;
    private final float pitch;
    private final long seed;

    public SoundEffectPacket(@NotNull NetworkBuffer reader) {
        int soundId = reader.read(VAR_INT);
        if (soundId == 0) {
            this.soundEvent = null;
            this.soundName = reader.read(STRING);
        } else {
            this.soundEvent = SoundEvent.fromId(soundId - 1);
            this.soundName = null;
        }
        this.range = reader.readOptional(FLOAT);
        this.source = reader.readEnum(Source.class);
        this.x = reader.read(INT) * 8;
        this.y = reader.read(INT) * 8;
        this.z = reader.read(INT) * 8;
        this.volume = reader.read(FLOAT);
        this.pitch = reader.read(FLOAT);
        this.seed = reader.read(LONG);
    }

    public SoundEffectPacket(@NotNull SoundEvent soundEvent, @Nullable Float range, @NotNull Source source,
                             @NotNull Point position, float volume, float pitch, long seed) {
        this.soundEvent = soundEvent;
        this.soundName = null;
        this.range = range;
        this.source = source;
        this.x = (int) position.x();
        this.y = (int) position.y();
        this.z = (int) position.z();
        this.volume = volume;
        this.pitch = pitch;
        this.seed = seed;
    }

    public SoundEffectPacket(@NotNull String soundName, @Nullable Float range, @NotNull Source source,
                             @NotNull Point position, float volume, float pitch, long seed) {
        this.soundEvent = null;
        this.soundName = soundName;
        this.range = range;
        this.source = source;
        this.x = (int) position.x();
        this.y = (int) position.y();
        this.z = (int) position.z();
        this.volume = volume;
        this.pitch = pitch;
        this.seed = seed;
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        if (soundEvent != null) {
            writer.write(VAR_INT, soundEvent.id() + 1);
        } else {
            writer.write(STRING, soundName);
        }
        writer.writeOptional(FLOAT, range);
        writer.write(VAR_INT, AdventurePacketConvertor.getSoundSourceValue(source));
        writer.write(INT, x * 8);
        writer.write(INT, y * 8);
        writer.write(INT, z * 8);
        writer.write(FLOAT, volume);
        writer.write(FLOAT, pitch);
        writer.write(LONG, seed);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SOUND_EFFECT;
    }

    public @Nullable SoundEvent soundEvent() {
        return soundEvent;
    }

    public @Nullable String soundName() {
        return soundName;
    }

    public @Nullable Float range() {
        return range;
    }

    public @NotNull Source source() {
        return source;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public float volume() {
        return volume;
    }

    public float pitch() {
        return pitch;
    }

    public long seed() {
        return seed;
    }
}
