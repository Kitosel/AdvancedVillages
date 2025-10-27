package pl.kiosel.villages.models.turets;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import pl.kiosel.common.Cuboid;

public abstract class Turret {

    public void set(Location location, Material material) {
        location.getBlock().setType(material);
    }

    public void set(World world, int x, int y, int z, Material material) {
        set(new Location(world, x, y, z), material);
    }

    public void fill(Location location1, Location location2, Material material) {
        Cuboid cuboid = new Cuboid(location1, location2);
        for (Block b : cuboid.getBlocks()) {
            b.setType(material);
        }
    }

    public abstract void setTurret(World world, int x, int y, int z);
}