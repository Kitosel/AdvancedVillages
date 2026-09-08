package pl.kiosel.villages.data.village.turets.internal;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import pl.kiosel.villages.data.village.turets.Turret;
import pl.kiosel.villages.data.village.turets.TurretStructure;

public class TurretSetNether extends Turret {

	private static final TurretStructure BODY = TurretStructure.builder()
			.layer(Material.NETHERRACK, -1, 2)

			.corners(Material.NETHER_BRICKS, 0, 2)
			.corners(Material.RED_NETHER_BRICKS, 1, 2)
			.corners(Material.NETHER_BRICKS, 2, 2)
			.corners(Material.NETHER_BRICKS, 3, 2)
			.corners(Material.NETHER_BRICK_SLAB, 5, 2)
			.block(Material.CRACKED_NETHER_BRICKS, 2, 0, 2)
			.block(Material.CRACKED_NETHER_BRICKS, 2, 3, -2)
			.block(Material.CRACKED_NETHER_BRICKS, -2, 2, -2)

			.cardinals(Material.SOUL_SAND, 3, 2)
			.mirrorXZ(Material.NETHER_BRICK_SLAB, 2, 4, 1)
			.mirrorXZ(Material.NETHER_BRICK_SLAB, 1, 4, 2)
			.cardinals(Material.AIR, 4, 2)

			.stairs(Material.NETHER_BRICK_STAIRS, 2, 3, 1, BlockFace.SOUTH)
			.stairs(Material.NETHER_BRICK_STAIRS, 2, 3, -1, BlockFace.NORTH)
			.stairs(Material.NETHER_BRICK_STAIRS, -2, 3, 1, BlockFace.SOUTH)
			.stairs(Material.NETHER_BRICK_STAIRS, -2, 3, -1, BlockFace.NORTH)
			.stairs(Material.NETHER_BRICK_STAIRS, 1, 3, 2, BlockFace.EAST)
			.stairs(Material.NETHER_BRICK_STAIRS, -1, 3, 2, BlockFace.WEST)
			.stairs(Material.NETHER_BRICK_STAIRS, 1, 3, -2, BlockFace.EAST)
			.stairs(Material.NETHER_BRICK_STAIRS, -1, 3, -2, BlockFace.WEST)
			.build();

	private static final TurretStructure CONNECTED = TurretStructure.builder()
			.corners(Material.NETHER_BRICK_WALL, 4, 2)
			.mirrorXZ(Material.NETHER_BRICK_WALL, 2, 2, 1)
			.mirrorXZ(Material.NETHER_BRICK_WALL, 1, 2, 2)
			.build();

	@Override
	public void setTurret(World world, int x, int y, int z) {
		paste(CONNECTED, world, x, y, z);
		paste(BODY, world, x, y, z);
	}
}
