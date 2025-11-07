package pl.kiosel.villages.data.village.turets.internal;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import pl.kiosel.villages.data.village.turets.Turret;

public class TurretReset extends Turret {

    public void setAir(Location location) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        fill(new Location(world, x + 2, y - 1, z + 2), new Location(world, x - 2, y + 5, z - 2), Material.AIR);
    }

    public void removeVillage(Location location) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        Material mat = Material.AIR;

        set(world, x, y, z, mat);
        set(world, x + 1, y, z, mat);
        set(world, x, y, z + 1, mat);
        set(world, x, y + 1, z, mat);
        set(world, x + 2, y - 1, z, mat);
        set(world, x + 2, y - 1, z + 1, mat);
        set(world, x + 1, y - 1, z + 1, mat);
        set(world, x + 1, y - 1, z + 2, mat);
        set(world, x + 2, y, z + 2, mat);
        set(world, x + 2, y + 1, z + 2, mat);
        set(world, x + 2, y + 2, z + 2, mat);
        set(world, x + 2, y + 3, z + 2, mat);
        set(world, x + 2, y + 4, z + 2, mat);
        set(world, x + 2, y + 5, z + 2, mat);
        set(world, x + 2, y + 6, z + 2, mat);
        set(world, x + 2, y + 3, z, mat);
        set(world, x + 2, y + 3, z + 1, mat);
        set(world, x + 2, y + 4, z + 1, mat);
        set(world, x + 1, y + 3, z + 2, mat);
        set(world, x + 1, y + 4, z + 2, mat);
        set(world, x + 2, y + 4, z - 1, mat);
        set(world, x, y + 3, z - 2, mat);
        set(world, x + 1, y + 3, z - 2, mat);
        set(world, x - 1, y + 4, z - 1, mat);
        set(world, x - 2, y + 4, z - 1, mat);
        set(world, x + 2, y + 4, z, mat);
        set(world, x, y + 4, z - 2, mat);
        set(world, x + 2, y, z + 1, mat);
        set(world, x + 2, y + 1, z + 1, mat);
        set(world, x + 2, y + 2, z + 1, mat);
        set(world, x + 1, y, z + 2, mat);
        set(world, x + 1, y + 1, z + 2, mat);
        set(world, x + 1, y + 2, z + 2, mat);
        set(world, x + 1, y + 4, z + 1, mat);
        set(world, x + 1, y + 5, z + 1, mat);
        set(world, x - 1, y + 4, z - 1, mat);
        set(world, x - 1, y + 5, z - 1, mat);
        set(world, x + 1, y + 5, z, mat);
        set(world, x + 1, y + 4, z - 2, mat);
        set(world, x, y + 5, z - 1, mat);
        set(world, x, y + 5, z, mat);
        set(world, x, y + 5, z + 1, mat);
        set(world, x - 1, y + 5, z, mat);
        set(world, x - 1, y + 5, z - 1, mat);
        set(world, x + 1, y + 4, z - 1, mat);
        set(world, x + 1, y + 5, z - 1, mat);
        set(world, x + 1, y - 2, z + 1, mat);
        set(world, x + 1, y - 2, z - 1, mat);
        set(world, x - 1, y - 2, z + 1, mat);
        set(world, x - 1, y - 2, z - 1, mat);
        set(world, x, y + 4, z, mat);
        set(world, x + 1, y + 4, z, mat);
        set(world, x - 1, y + 4, z, mat);
        set(world, x + 1, y + 4, z + 1, mat);
        set(world, x + 1, y + 4, z - 1, mat);
        set(world, x, y + 4, z - 1, mat);
        set(world, x, y + 4, z + 1, mat);
    }

    @Override
    public void setTurret(World world, int x, int y, int z) {

    }
}