package pl.kiosel.villages.data.outpost.turets;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import pl.kiosel.villages.data.outpost.OutpostManager;
import pl.kiosel.villages.data.village.turets.Turret;

import java.util.Objects;

public final class TurretReset extends Turret {

    public void setAir(Location location) {
        World world = Objects.requireNonNull(location.getWorld(), "outpost world");
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
		for (int offsetX = -OutpostManager.BUILD_RADIUS; offsetX <= OutpostManager.BUILD_RADIUS; offsetX++) {
			for (int offsetY = OutpostManager.BUILD_MIN_Y; offsetY <= OutpostManager.BUILD_MAX_Y; offsetY++) {
				for (int offsetZ = -OutpostManager.BUILD_RADIUS; offsetZ <= OutpostManager.BUILD_RADIUS; offsetZ++) {
					world.getBlockAt(x + offsetX, y + offsetY, z + offsetZ).setType(Material.AIR, false);
				}
			}
		}
    }

    @Override
    public void setTurret(World world, int x, int y, int z) {

    }
}
