package pl.kiosel.villages.data.village.turets.internal;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import pl.kiosel.villages.data.village.turets.Turret;
import pl.kiosel.villages.data.village.turets.TurretStructure;

public class TurretSetEnd extends Turret {

	private static final TurretStructure STRUCTURE = TurretStructure.builder()
			.layer(Material.IRON_BLOCK, -3, 2)
			.corners(Material.BEACON, -2, 1)

			.layer(Material.END_STONE, -1, 2)
			.cardinals(Material.END_STONE_BRICKS, -1, 1)

			.corners(Material.WHITE_STAINED_GLASS, -1, 1)
			.mirrorXZ(Material.END_STONE_BRICKS, 2, -1, 1)
			.mirrorXZ(Material.END_STONE_BRICKS, 1, -1, 2)

			.fourPillars(Material.PURPUR_PILLAR, 2, 0, 2)
			.mirrorXZ(Material.PURPUR_SLAB, 2, 0, 1)
			.mirrorXZ(Material.PURPUR_SLAB, 1, 0, 2)
			.mirrorXZ(Material.PURPUR_PILLAR, 2, 3, 1)
			.mirrorXZ(Material.PURPUR_PILLAR, 1, 3, 2)

			.cardinals(Material.PURPUR_SLAB, 3, 2)

			.corners(Material.PURPLE_STAINED_GLASS, 4, 1)
			.cardinals(Material.MAGENTA_STAINED_GLASS,4, 1)
			.block(Material.PINK_STAINED_GLASS, 0, 4, 0)
			.block(Material.AIR, 0, 5, 0)
			.corners(Material.AIR, 5, 1)
			.cardinals(Material.PURPUR_SLAB,5, 1)

			.stairs(Material.PURPUR_STAIRS, 2, 4, 1, BlockFace.EAST)
			.stairs(Material.PURPUR_STAIRS, 2, 4, -1, BlockFace.WEST)
			.stairs(Material.PURPUR_STAIRS, -2, 4, 1, BlockFace.EAST)
			.stairs(Material.PURPUR_STAIRS, -2, 4, -1, BlockFace.EAST)
			.stairs(Material.PURPUR_STAIRS, 1, 4, 2, BlockFace.NORTH)
			.stairs(Material.PURPUR_STAIRS, -1, 4, 2, BlockFace.NORTH)
			.stairs(Material.PURPUR_STAIRS, 1, 4, -2, BlockFace.SOUTH)
			.stairs(Material.PURPUR_STAIRS, -1, 4, -2, BlockFace.SOUTH)
			.stairs(Material.PURPUR_STAIRS, 2, 2, 1, BlockFace.SOUTH)
			.stairs(Material.PURPUR_STAIRS, 2, 2, -1, BlockFace.NORTH)
			.stairs(Material.PURPUR_STAIRS, -2, 2, 1, BlockFace.SOUTH)
			.stairs(Material.PURPUR_STAIRS, -2, 2, -1, BlockFace.NORTH)
			.stairs(Material.PURPUR_STAIRS, 1, 2, 2, BlockFace.EAST)
			.stairs(Material.PURPUR_STAIRS, -1, 2, 2, BlockFace.WEST)
			.stairs(Material.PURPUR_STAIRS, 1, 2, -2, BlockFace.EAST)
			.stairs(Material.PURPUR_STAIRS, -1, 2, -2, BlockFace.WEST)
			.build();

	@Override
	public void setTurret(World world, int x, int y, int z) {
		paste(STRUCTURE, world, x, y, z);
	}
}
