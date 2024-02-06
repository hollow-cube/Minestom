package net.bytemc.minestom.server.utils.positions;

import net.bytemc.minestom.server.ByteServer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;

public class BytePositionPair {
    private final BytePosition bytePosition;
    private final Pos position;
    private Instance instance;

    private BytePositionPair(BytePosition bytePosition) {
        this.bytePosition = bytePosition;
        this.position = new Pos(this.bytePosition.getX(),
                this.bytePosition.getY(),
                this.bytePosition.getZ(),
                this.bytePosition.getYaw(),
                this.bytePosition.getPitch());

        Instance minestomInstance = ByteServer.getInstance().getInstanceHandler().getInstanceFromName(this.bytePosition.getInstanceName());

        if (minestomInstance != null) {
            this.instance = minestomInstance;
        }
    }

    public static BytePositionPair forPosition(BytePosition bytePosition) {
        return new BytePositionPair(bytePosition);
    }

    public void teleport(Entity entity) {
        if (this.instance != null) {
            if (entity.getInstance().equals(this.instance)) {
                entity.teleport(this.position);
            } else {
                entity.setInstance(this.instance, this.position);
            }
        } else {
            Instance minestomInstance = ByteServer.getInstance().getInstanceHandler().getInstanceFromName(this.bytePosition.getInstanceName());

            if (minestomInstance != null) {
                setInstance(minestomInstance);
            } else {
                MinecraftServer.LOGGER.warn("The saved instance of the position '" + this.bytePosition.getName() +
                        "' is not registered, the entity might be in a different instance then the one saved in the database: '" +
                        this.bytePosition.getInstanceName() + "'");
            }

            entity.teleport(this.position);
        }
    }

    public BytePosition getBytePosition() {
        return bytePosition;
    }

    public Pos getPosition() {
        return position;
    }

    public Instance getInstance() {
        return instance;
    }

    void setInstance(Instance instance) {
        this.instance = instance;
    }
}


