package pl.kiosel.villages.models.turets.internal;

import org.bukkit.Material;
import org.bukkit.World;
import pl.kiosel.villages.models.turets.Turret;

public class TurretSetPrismarine extends Turret {

	@Override
	public void setTurret(World world, int x, int y, int z) {
		set(world, x, y, z, Material.NOTE_BLOCK);

		setPrismarineWall(world, x, y, z);
		setPrismarineFloor(world, x, y, z);

		set(world, x, y + 5, z, Material.SEA_LANTERN);
		set(world, x + 1, y + 5, z, Material.WHITE_STAINED_GLASS);
		set(world, x - 1, y + 5, z, Material.WHITE_STAINED_GLASS);
		set(world, x, y + 5, z + 1, Material.WHITE_STAINED_GLASS);
		set(world, x, y + 5, z - 1, Material.WHITE_STAINED_GLASS);

		setPrismarinePillar(world, x, y, z);
	}
	
	public void setPrismarineFloor(World world, int x, int y, int z) {
		set(world, x, y - 1, z, Material.DARK_PRISMARINE);
		set(world, x + 1, y - 1, z, Material.PRISMARINE);
		set(world, x - 1, y - 1, z, Material.PRISMARINE);
		set(world, x, y - 1, z + 1, Material.PRISMARINE);
		set(world, x, y - 1, z - 1, Material.PRISMARINE);
		set(world, x + 1, y - 1, z + 1, Material.DARK_PRISMARINE);
		set(world, x - 1, y - 1, z + 1, Material.DARK_PRISMARINE);
		set(world, x - 1, y - 1, z - 1, Material.DARK_PRISMARINE);
		set(world, x + 1, y - 1, z - 1, Material.DARK_PRISMARINE);
		set(world, x + 2, y - 1, z + 2, Material.DARK_PRISMARINE);
		set(world, x + 2, y - 1, z - 2, Material.DARK_PRISMARINE);
		set(world, x - 2, y - 1, z - 2, Material.DARK_PRISMARINE);
		set(world, x - 2, y - 1, z + 2, Material.DARK_PRISMARINE);
		set(world, x + 2, y - 1, z + 1, Material.PRISMARINE);
		set(world, x + 2, y - 1, z - 1, Material.PRISMARINE);
		set(world, x + 2, y - 1, z, Material.PRISMARINE);
		set(world, x - 2, y - 1, z + 1, Material.PRISMARINE);
		set(world, x - 2, y - 1, z - 1, Material.PRISMARINE);
		set(world, x - 2, y - 1, z, Material.PRISMARINE);
		set(world, x + 1, y - 1, z + 2, Material.PRISMARINE);
		set(world, x - 1, y - 1, z + 2, Material.PRISMARINE);
		set(world, x, y - 1, z + 2, Material.PRISMARINE);
		set(world, x + 1, y - 1, z - 2, Material.PRISMARINE);
		set(world, x - 1, y - 1, z - 2, Material.PRISMARINE);
		set(world, x, y - 1, z - 2, Material.PRISMARINE);
	}
	
	public void setPrismarineWall(World world, int x, int y, int z) {
		set(world, x + 2, y, z + 1, Material.PRISMARINE_WALL);
		set(world, x + 2, y, z - 1, Material.PRISMARINE_WALL);
		set(world, x - 2, y, z + 1, Material.PRISMARINE_WALL);
		set(world, x - 2, y, z - 1, Material.PRISMARINE_WALL);
		set(world, x + 1, y, z + 2, Material.PRISMARINE_WALL);
		set(world, x - 1, y, z + 2, Material.PRISMARINE_WALL);
		set(world, x + 1, y, z - 2, Material.PRISMARINE_WALL);
		set(world, x - 1, y, z - 2, Material.PRISMARINE_WALL);
		set(world, x + 2, y + 2, z + 1, Material.QUARTZ_PILLAR);
		set(world, x + 2, y + 2, z - 1, Material.QUARTZ_PILLAR);
		set(world, x - 2, y + 2, z + 1, Material.QUARTZ_PILLAR);
		set(world, x - 2, y + 2, z - 1, Material.QUARTZ_PILLAR);
		set(world, x + 1, y + 2, z + 2, Material.QUARTZ_PILLAR);
		set(world, x - 1, y + 2, z + 2, Material.QUARTZ_PILLAR);
		set(world, x + 1, y + 2, z - 2, Material.QUARTZ_PILLAR);
		set(world, x - 1, y + 2, z - 2, Material.QUARTZ_PILLAR);
		set(world, x + 2, y + 3, z, Material.WHITE_STAINED_GLASS_PANE);
		set(world, x - 2, y + 3, z, Material.WHITE_STAINED_GLASS_PANE);
		set(world, x, y + 3, z + 2, Material.WHITE_STAINED_GLASS_PANE);
		set(world, x, y + 3, z - 2, Material.WHITE_STAINED_GLASS_PANE);
	}
	
	public void setPrismarinePillar(World world, int x, int y, int z) {
		set(world, x + 2, y, z + 2, Material.SEA_LANTERN);
		set(world, x + 2, y, z - 2, Material.SEA_LANTERN);
		set(world, x - 2, y, z + 2, Material.SEA_LANTERN);
		set(world, x - 2, y, z - 2, Material.SEA_LANTERN);
		set(world, x + 2, y + 1, z + 2, Material.DARK_PRISMARINE);
		set(world, x + 2, y + 1, z - 2, Material.DARK_PRISMARINE);
		set(world, x - 2, y + 1, z + 2, Material.DARK_PRISMARINE);
		set(world, x - 2, y + 1, z - 2, Material.DARK_PRISMARINE);
		set(world, x + 2, y + 2, z + 2, Material.SMOOTH_QUARTZ_SLAB);
		set(world, x + 2, y + 2, z - 2, Material.SMOOTH_QUARTZ_SLAB);
		set(world, x - 2, y + 2, z + 2, Material.SMOOTH_QUARTZ_SLAB);
		set(world, x - 2, y + 2, z - 2, Material.SMOOTH_QUARTZ_SLAB);
		set(world, x + 2, y + 3, z + 1, Material.PRISMARINE_BRICKS);
		set(world, x + 2, y + 3, z - 1, Material.PRISMARINE_BRICKS);
		set(world, x - 2, y + 3, z + 1, Material.PRISMARINE_BRICKS);
		set(world, x - 2, y + 3, z - 1, Material.PRISMARINE_BRICKS);
		set(world, x + 1, y + 3, z + 2, Material.PRISMARINE_BRICKS);
		set(world, x - 1, y + 3, z + 2, Material.PRISMARINE_BRICKS);
		set(world, x + 1, y + 3, z - 2, Material.PRISMARINE_BRICKS);
		set(world, x - 1, y + 3, z - 2, Material.PRISMARINE_BRICKS);
		set(world, x + 1, y + 4, z + 1, Material.PRISMARINE);
		set(world, x + 1, y + 4, z - 1, Material.PRISMARINE);
		set(world, x - 1, y + 4, z + 1, Material.PRISMARINE);
		set(world, x - 1, y + 4, z - 1, Material.PRISMARINE);
		set(world, x + 1, y + 4, z + 1, Material.PRISMARINE);
		set(world, x - 1, y + 4, z + 1, Material.PRISMARINE);
		set(world, x + 1, y + 4, z - 1, Material.PRISMARINE);
		set(world, x - 1, y + 4, z - 1, Material.PRISMARINE);
		set(world, x + 2, y + 4, z + 1, Material.SMOOTH_QUARTZ_SLAB);
		set(world, x + 2, y + 4, z - 1, Material.SMOOTH_QUARTZ_SLAB);
		set(world, x - 2, y + 4, z + 1, Material.SMOOTH_QUARTZ_SLAB);
		set(world, x - 2, y + 4, z - 1, Material.SMOOTH_QUARTZ_SLAB);
		set(world, x + 1, y + 4, z + 2, Material.SMOOTH_QUARTZ_SLAB);
		set(world, x - 1, y + 4, z + 2, Material.SMOOTH_QUARTZ_SLAB);
		set(world, x + 1, y + 4, z - 2, Material.SMOOTH_QUARTZ_SLAB);
		set(world, x - 1, y + 4, z - 2, Material.SMOOTH_QUARTZ_SLAB);
		set(world, x + 1, y + 5, z + 1, Material.SMOOTH_QUARTZ_SLAB);
		set(world, x + 1, y + 5, z - 1, Material.SMOOTH_QUARTZ_SLAB);
		set(world, x - 1, y + 5, z + 1, Material.SMOOTH_QUARTZ_SLAB);
		set(world, x - 1, y + 5, z - 1, Material.SMOOTH_QUARTZ_SLAB);
	}	
}