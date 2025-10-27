package pl.kiosel.villages.models.turets.internal;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import pl.kiosel.villages.Wioski;
import pl.kiosel.villages.models.turets.Turret;

public class TurretSetOak extends Turret {

	@Override
	public void setTurret(World world, int x, int y, int z) {
		set(world, x, y, z, Material.NOTE_BLOCK);
		setOakTurretFence(world, x, y, z);

		Bukkit.getScheduler().runTaskLater(Wioski.getInstance(),() -> {
			set(world, x + 2, y - 1, z + 2, Material.SPRUCE_PLANKS);
			set(world, x + 2, y - 1, z - 2, Material.SPRUCE_PLANKS);
			set(world, x - 2, y - 1, z + 2, Material.SPRUCE_PLANKS);
			set(world, x - 2, y - 1, z - 2, Material.SPRUCE_PLANKS);
			set(world, x - 2, y - 1, z, Material.SPRUCE_LOG);
			set(world, x + 2, y - 1, z, Material.SPRUCE_LOG);
			set(world, x, y - 1, z - 2, Material.SPRUCE_LOG);
			set(world, x, y - 1, z + 2, Material.SPRUCE_LOG);
			set(world, x - 2, y - 1, z - 1, Material.SPRUCE_LOG);
			set(world, x + 2, y - 1, z + 1, Material.SPRUCE_LOG);
			set(world,  x - 1, y - 1, z - 2, Material.SPRUCE_LOG);
			set(world, x + 1, y - 1, z + 2, Material.SPRUCE_LOG);
			set(world, x - 2, y - 1, z + 1, Material.SPRUCE_LOG);
			set(world, x + 2, y - 1, z - 1, Material.SPRUCE_LOG);
			set(world, x + 1, y - 1, z - 2, Material.SPRUCE_LOG);
			set(world, x - 1, y - 1, z + 2, Material.SPRUCE_LOG);
			set(world, x + 2, y + 5, z + 2, Material.SPRUCE_SLAB);
			set(world, x + 2, y + 5, z - 2, Material.SPRUCE_SLAB);
			set(world, x - 2, y + 5, z + 2, Material.SPRUCE_SLAB);
			set(world, x - 2, y + 5, z - 2, Material.SPRUCE_SLAB);

			setOakTuretFlor(world, x, y, z);
			setOakTuretSlup(world, x, y, z);
		}, 5L);
	}
	
	private void setOakTurretFence(World world, int x, int y, int z) {
		set(world, x + 2, y + 3, z, Material.SPRUCE_FENCE);
		set(world, x - 2, y + 3, z, Material.SPRUCE_FENCE);
		set(world, x, y + 3, z + 2, Material.SPRUCE_FENCE);
		set(world, x, y + 3, z - 2, Material.SPRUCE_FENCE);
		set(world, x + 2, y + 4, z + 1, Material.SPRUCE_FENCE);
		set(world, x - 2, y + 4, z - 1, Material.SPRUCE_FENCE);
		set(world, x + 1, y + 4, z + 2, Material.SPRUCE_FENCE);
		set(world, x - 1, y + 4, z - 2, Material.SPRUCE_FENCE);
		set(world, x - 1, y + 4, z + 2, Material.SPRUCE_FENCE);
		set(world, x + 1, y + 4, z - 2, Material.SPRUCE_FENCE);
		set(world, x + 2, y + 4, z - 1, Material.SPRUCE_FENCE);
		set(world, x - 2, y + 4, z + 1, Material.SPRUCE_FENCE);
	}
	
	public void setOakTuretFlor(World world, int x, int y, int z) {
		set(world, x + 1, y - 1 , z + 1, Material.SPRUCE_PLANKS);
		set(world, x + 1, y - 1 , z - 1, Material.SPRUCE_PLANKS);
		set(world, x - 1, y - 1 , z - 1, Material.SPRUCE_PLANKS);
		set(world, x - 1, y - 1 , z + 1, Material.SPRUCE_PLANKS);
		set(world, x + 1, y - 1 , z, Material.SPRUCE_PLANKS);
		set(world, x - 1, y - 1 , z, Material.SPRUCE_PLANKS);
		set(world, x, y - 1 , z + 1, Material.SPRUCE_PLANKS);
		set(world, x, y - 1 , z - 1, Material.SPRUCE_PLANKS);
		set(world, x, y - 1, z, Material.SPRUCE_PLANKS);
	}
	
	public void setOakTuretSlup(World world, int x, int y, int z) {
		set(world, x + 2, y, z + 2, Material.SPRUCE_LOG);
		set(world, x + 2, y, z - 2, Material.SPRUCE_LOG);
		set(world, x - 2, y, z + 2, Material.SPRUCE_LOG);
		set(world, x - 2, y, z - 2, Material.SPRUCE_LOG);
		set(world, x + 2, y + 1, z + 2, Material.SPRUCE_LOG);
		set(world, x + 2, y + 1, z - 2, Material.SPRUCE_LOG);
		set(world, x - 2, y + 1, z + 2, Material.SPRUCE_LOG);
		set(world, x - 2, y + 1, z - 2, Material.SPRUCE_LOG);
		set(world, x + 2, y + 2, z + 2, Material.SPRUCE_LOG);
		set(world, x + 2, y + 2, z - 2, Material.SPRUCE_LOG);
		set(world, x - 2, y + 2, z + 2, Material.SPRUCE_LOG);
		set(world, x - 2, y + 2, z - 2, Material.SPRUCE_LOG);
		set(world, x + 2, y + 3, z + 2, Material.SPRUCE_LOG);
		set(world, x + 2, y + 3, z - 2, Material.SPRUCE_LOG);
		set(world, x - 2, y + 3, z + 2, Material.SPRUCE_LOG);
		set(world, x - 2, y + 3, z - 2, Material.SPRUCE_LOG);
		set(world, x + 2, y + 4, z + 2, Material.SPRUCE_LOG);
		set(world, x + 2, y + 4, z - 2, Material.SPRUCE_LOG);
		set(world, x - 2, y + 4, z + 2, Material.SPRUCE_LOG);
		set(world, x - 2, y + 4, z - 2, Material.SPRUCE_LOG);
		set(world, x + 2, y + 3, z + 1, Material.SPRUCE_WOOD);
		set(world, x + 2, y + 3, z - 1, Material.SPRUCE_WOOD);
		set(world, x - 2, y + 3, z + 1, Material.SPRUCE_WOOD);
		set(world, x - 2, y + 3, z - 1, Material.SPRUCE_WOOD);
		set(world, x + 1, y + 3, z + 2, Material.SPRUCE_WOOD);
		set(world, x + 1, y + 3, z - 2, Material.SPRUCE_WOOD);
		set(world, x - 1, y + 3, z + 2, Material.SPRUCE_WOOD);
		set(world, x - 1, y + 3, z - 2, Material.SPRUCE_WOOD);
	}
}