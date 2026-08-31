package pl.kiosel.villages.data.village.turets.internal;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import pl.kiosel.villages.data.village.turets.Turret;
import pl.kiosel.villages.data.village.turets.TurretStructure;

public class TurretSetBrick extends Turret {

	private static final TurretStructure STRUCTURE = TurretStructure.builder()
			.layer(Material.STONE, -1, 2)
			.block(Material.STONE_BRICKS, 0, -1, 0)
			.mirrorXZ(Material.COBBLESTONE, 2, -1, 1)
			.mirrorXZ(Material.COBBLESTONE, 1, -1, 2)

			.cardinals(Material.STONE_BRICK_SLAB, 0, 1)
			.corners(Material.STONE_BRICKS, 0, 2)
			.corners(Material.CHISELED_STONE_BRICKS, 1, 2)
			.corners(Material.STONE_BRICKS, 2, 2)
			.corners(Material.CRACKED_STONE_BRICKS, 3, 2)
			.corners(Material.CHISELED_STONE_BRICKS, 4, 2)
			.corners(Material.STONE_BRICK_SLAB, 5, 2)

			.cardinals(Material.STONE_BRICKS, 3, 2)
			.cardinals(Material.STONE_BRICK_SLAB, 4, 2)
			.blocks(Material.STONE_BRICK_WALL,
					2, 4, 1,
					2, 4, -1,
					-2, 4, -1,
					1, 4, 2,
					1, 4, -2)
			.blocks(Material.MOSSY_STONE_BRICK_WALL,
					-2, 4, 1,
					-1, 4, 2,
					-1, 4, -2)

			.stairs(Material.STONE_BRICK_STAIRS, 2, 3, 1, BlockFace.NORTH)
			.stairs(Material.STONE_BRICK_STAIRS, 2, 3, -1, BlockFace.SOUTH)
			.stairs(Material.STONE_BRICK_STAIRS, -2, 3, 1, BlockFace.NORTH)
			.stairs(Material.STONE_BRICK_STAIRS, -2, 3, -1, BlockFace.SOUTH)
			.stairs(Material.STONE_BRICK_STAIRS, 1, 3, 2, BlockFace.WEST)
			.stairs(Material.STONE_BRICK_STAIRS, -1, 3, 2, BlockFace.EAST)
			.stairs(Material.STONE_BRICK_STAIRS, 1, 3, -2, BlockFace.WEST)
			.stairs(Material.STONE_BRICK_STAIRS, -1, 3, -2, BlockFace.EAST)

			.stairs(Material.STONE_BRICK_STAIRS, 2, 2, 1, BlockFace.SOUTH, Bisected.Half.TOP)
			.stairs(Material.STONE_BRICK_STAIRS, 2, 2, -1, BlockFace.NORTH, Bisected.Half.TOP)
			.stairs(Material.STONE_BRICK_STAIRS, -2, 2, 1, BlockFace.SOUTH, Bisected.Half.TOP)
			.stairs(Material.STONE_BRICK_STAIRS, -2, 2, -1, BlockFace.NORTH, Bisected.Half.TOP)
			.stairs(Material.STONE_BRICK_STAIRS, 1, 2, 2, BlockFace.EAST, Bisected.Half.TOP)
			.stairs(Material.STONE_BRICK_STAIRS, -1, 2, 2, BlockFace.WEST, Bisected.Half.TOP)
			.stairs(Material.STONE_BRICK_STAIRS, 1, 2, -2, BlockFace.EAST, Bisected.Half.TOP)
			.stairs(Material.STONE_BRICK_STAIRS, -1, 2, -2, BlockFace.WEST, Bisected.Half.TOP)
			.build();

	@Override
	public void setTurret(World world, int x, int y, int z) {
		paste(STRUCTURE, world, x, y, z);
	}
}
