package pl.kiosel.villages.addons.buildeditor;

import org.bukkit.Location;
import org.bukkit.World;

final class BuildEditorBounds {

    private final World world;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;

    BuildEditorBounds(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.world = world;
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
    }

    World getWorld() {
        return world;
    }

    int getMinX() {
        return minX;
    }

    int getMinY() {
        return minY;
    }

    int getMinZ() {
        return minZ;
    }

    int getMaxX() {
        return maxX;
    }

    int getMaxY() {
        return maxY;
    }

    int getMaxZ() {
        return maxZ;
    }

    boolean contains(Location location) {
        if (location == null || location.getWorld() == null || !world.equals(location.getWorld())) {
            return false;
        }
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    boolean intersects(BuildEditorBounds other) {
        return world.equals(other.world)
                && minX <= other.maxX && maxX >= other.minX
                && minY <= other.maxY && maxY >= other.minY
                && minZ <= other.maxZ && maxZ >= other.minZ;
    }

    BuildEditorBounds expand(int amount) {
        int safeAmount = Math.max(0, amount);
        return new BuildEditorBounds(
                world,
                minX - safeAmount,
                Math.max(world.getMinHeight(), minY - safeAmount),
                minZ - safeAmount,
                maxX + safeAmount,
                Math.min(world.getMaxHeight() - 1, maxY + safeAmount),
                maxZ + safeAmount
        );
    }
}
