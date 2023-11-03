package net.bytemc.minestom.server.schematics;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.*;

public class CuboId {

    private final int xMin;
    private final int xMax;
    private final int yMin;
    private final int yMax;
    private final int zMin;
    private final int zMax;
    private final double xMinCentered;
    private final double xMaxCentered;
    private final double yMinCentered;
    private final double yMaxCentered;
    private final double zMinCentered;
    private final double zMaxCentered;
    private final Instance instance;

    public CuboId(Point point1, Point point2, Instance instance) {
        this.xMin = Math.min(point1.blockX(), point2.blockX());
        this.xMax = Math.max(point1.blockX(), point2.blockX());
        this.yMin = Math.min(point1.blockY(), point2.blockY());
        this.yMax = Math.max(point1.blockY(), point2.blockY());
        this.zMin = Math.min(point1.blockZ(), point2.blockZ());
        this.zMax = Math.max(point1.blockZ(), point2.blockZ());
        this.instance = instance;
        this.xMinCentered = this.xMin + 0.5;
        this.xMaxCentered = this.xMax + 0.5;
        this.yMinCentered = this.yMin + 0.5;
        this.yMaxCentered = this.yMax + 0.5;
        this.zMinCentered = this.zMin + 0.5;
        this.zMaxCentered = this.zMax + 0.5;
    }

    public Map<Block, Point> blockList() {
        Map<Block, Point> bL = new HashMap<>(this.getTotalBlockSize());

        for (int x = this.xMin; x <= this.xMax; ++x) {
            for (int y = this.yMin; y <= this.yMax; ++y) {
                for (int z = this.zMin; z <= this.zMax; ++z) {
                    Block b = this.instance.getBlock(x, y, z);
                    bL.put(b, new Vec(x, y, z));
                }
            }
        }
        return bL;
    }

    public Point getCenter() {
        return new Vec((this.xMax - this.xMin) / 2 + this.xMin, (this.yMax - this.yMin) / 2 + this.yMin, (this.zMax - this.zMin) / 2 + this.zMin);
    }

    public int getHeight() {
        return this.yMax - this.yMin + 1;
    }

    public Point getPoint1() {
        return new Vec(this.xMin, this.yMin, this.zMin);
    }

    public Point getPoint2() {
        return new Vec(this.xMax, this.yMax, this.zMax);
    }

    public int getTotalBlockSize() {
        return this.getHeight() * this.getXWidth() * this.getZWidth();
    }

    public int getXWidth() {
        return this.xMax - this.xMin + 1;
    }

    public int getZWidth() {
        return this.zMax - this.zMin + 1;
    }
}

