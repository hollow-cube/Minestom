package net.bytemc.minestom.server.utils.positions;

public class BytePosition {
    private final String groupName;
    private final String name;
    private final String instanceName;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public BytePosition(String groupName, String name, String instanceName, double x, double y, double z, float yaw, float pitch) {
        this.groupName = groupName;
        this.name = name;
        this.instanceName = instanceName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getName() {
        return name;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}
