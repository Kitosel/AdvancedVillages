package pl.kiosel.villages.data.village.turets.worldedit;

import org.bukkit.Material;
import org.bukkit.World;
import pl.kiosel.villages.data.village.turets.Turret;

public class TurretSetWE extends Turret {

	@Override
	public void setTurret(World world, int x, int y, int z) {
		set(world, x, y, z, Material.NOTE_BLOCK);
	}
}