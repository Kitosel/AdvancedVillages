package pl.kiosel.villages.data.village.turets.internal;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import pl.kiosel.villages.data.village.turets.Turret;

public class TurretSetNether extends Turret {

	@Override
	public void setTurret(World world, int x, int y, int z) {
		set(world, x, y, z, Material.NOTE_BLOCK);

		setNetherWall(world, x, y, z);
		setNetherFloor(world, x, y, z);

//		set(world, x + 2, y, z + 1, Material.NETHER_BRICK_FENCE);
//		set(world, x + 2, y, z - 1, Material.NETHER_BRICK_FENCE);
//		set(world, x - 2, y, z + 1, Material.NETHER_BRICK_FENCE);
//		set(world, x - 2, y, z - 1, Material.NETHER_BRICK_FENCE);
//		set(world, x + 1, y, z + 2, Material.NETHER_BRICK_FENCE);
//		set(world, x - 1, y, z + 2, Material.NETHER_BRICK_FENCE);
//		set(world, x + 1, y, z - 2, Material.NETHER_BRICK_FENCE);
//		set(world, x - 1, y, z - 2, Material.NETHER_BRICK_FENCE);
//		set(world, x + 2, y + 1, z + 1, Material.NETHER_BRICK_SLAB);
//		set(world, x + 2, y + 1, z - 1, Material.NETHER_BRICK_SLAB);
//		set(world, x - 2, y + 1, z + 1, Material.NETHER_BRICK_SLAB);
//		set(world, x - 2, y + 1, z - 1, Material.NETHER_BRICK_SLAB);
//		set(world, x + 1, y + 1, z + 2, Material.NETHER_BRICK_SLAB);
//		set(world, x - 1, y + 1, z + 2, Material.NETHER_BRICK_SLAB);
//		set(world, x + 1, y + 1, z - 2, Material.NETHER_BRICK_SLAB);
//		set(world, x - 1, y + 1, z - 2, Material.NETHER_BRICK_SLAB);
		set(world, x + 2, y + 4, z, Material.AIR);
		set(world, x - 2, y + 4, z, Material.AIR);
		set(world, x, y + 4, z + 2, Material.AIR);
		set(world, x, y + 4, z - 2, Material.AIR);

		setNetherPillar(world, x, y, z);
	}

	public void setNetherWall(World world, int x, int y, int z) {
		set(world, x + 2, y + 2, z + 1, Material.NETHER_BRICK_WALL);
		set(world, x + 2, y + 2, z - 1, Material.NETHER_BRICK_WALL);
		set(world, x - 2, y + 2, z + 1, Material.NETHER_BRICK_WALL);
		set(world, x - 2, y + 2, z - 1, Material.NETHER_BRICK_WALL);
		set(world, x + 1, y + 2, z + 2, Material.NETHER_BRICK_WALL);
		set(world, x - 1, y + 2, z + 2, Material.NETHER_BRICK_WALL);
		set(world, x + 1, y + 2, z - 2, Material.NETHER_BRICK_WALL);
		set(world, x - 1, y + 2, z - 2, Material.NETHER_BRICK_WALL);
		set(world, x + 2, y + 3, z, Material.SOUL_SAND);
		set(world, x - 2, y + 3, z, Material.SOUL_SAND);
		set(world, x, y + 3, z + 2, Material.SOUL_SAND);
		set(world, x, y + 3, z - 2, Material.SOUL_SAND);
		set(world, x + 2, y + 4, z + 1, Material.NETHER_BRICK_SLAB);
		set(world, x + 2, y + 4, z - 1, Material.NETHER_BRICK_SLAB);
		set(world, x - 2, y + 4, z + 1, Material.NETHER_BRICK_SLAB);
		set(world, x - 2, y + 4, z - 1, Material.NETHER_BRICK_SLAB);
		set(world, x + 1, y + 4, z + 2, Material.NETHER_BRICK_SLAB);
		set(world, x - 1, y + 4, z + 2, Material.NETHER_BRICK_SLAB);
		set(world, x + 1, y + 4, z - 2, Material.NETHER_BRICK_SLAB);
		set(world, x - 1, y + 4, z - 2, Material.NETHER_BRICK_SLAB);

		Location stairs1 = new Location(world, x + 2, y + 3, z + 1);
		Location stairs2 = new Location(world, x + 2, y + 3, z - 1);
		Location stairs3 = new Location(world, x - 2, y + 3, z + 1);
		Location stairs4 = new Location(world, x - 2, y + 3, z - 1);
		Location stairs5 = new Location(world, x + 1, y + 3, z + 2);
		Location stairs6 = new Location(world, x - 1, y + 3, z + 2);
		Location stairs7 = new Location(world, x + 1, y + 3, z - 2);
		Location stairs8 = new Location(world, x - 1, y + 3, z - 2);

		stairs1.getBlock().setType(Material.NETHER_BRICK_STAIRS);
		BlockData stairsdata1 = stairs1.getBlock().getBlockData();
		((Directional) stairsdata1).setFacing(BlockFace.SOUTH);
		stairs1.getBlock().setBlockData(stairsdata1);

		stairs2.getBlock().setType(Material.NETHER_BRICK_STAIRS);
		BlockData stairsdata2 = stairs2.getBlock().getBlockData();
		((Directional) stairsdata2).setFacing(BlockFace.NORTH);
		stairs2.getBlock().setBlockData(stairsdata2);

		stairs3.getBlock().setType(Material.NETHER_BRICK_STAIRS);
		BlockData stairsdata3 = stairs3.getBlock().getBlockData();
		((Directional) stairsdata3).setFacing(BlockFace.SOUTH);
		stairs3.getBlock().setBlockData(stairsdata3);

		stairs4.getBlock().setType(Material.NETHER_BRICK_STAIRS);
		BlockData stairsdata4 = stairs4.getBlock().getBlockData();
		((Directional) stairsdata4).setFacing(BlockFace.NORTH);
		stairs4.getBlock().setBlockData(stairsdata4);

		stairs5.getBlock().setType(Material.NETHER_BRICK_STAIRS);
		BlockData stairsdata5 = stairs5.getBlock().getBlockData();
		((Directional) stairsdata5).setFacing(BlockFace.EAST);
		stairs5.getBlock().setBlockData(stairsdata5);

		stairs6.getBlock().setType(Material.NETHER_BRICK_STAIRS);
		BlockData stairsdata6 = stairs6.getBlock().getBlockData();
		((Directional) stairsdata6).setFacing(BlockFace.WEST);
		stairs6.getBlock().setBlockData(stairsdata6);

		stairs7.getBlock().setType(Material.NETHER_BRICK_STAIRS);
		BlockData stairsdata7 = stairs7.getBlock().getBlockData();
		((Directional) stairsdata7).setFacing(BlockFace.EAST);
		stairs7.getBlock().setBlockData(stairsdata7);

		stairs8.getBlock().setType(Material.NETHER_BRICK_STAIRS);
		BlockData stairsdata8 = stairs8.getBlock().getBlockData();
		((Directional) stairsdata8).setFacing(BlockFace.WEST);
		stairs8.getBlock().setBlockData(stairsdata8);

	}

	public void setNetherFloor(World world, int x, int y, int z) {
		set(world, x, y - 1, z, Material.NETHERRACK);
		set(world, x + 1, y - 1, z, Material.NETHERRACK);
		set(world, x - 1, y - 1, z, Material.NETHERRACK);
		set(world, x, y - 1, z + 1, Material.NETHERRACK);
		set(world, x, y - 1, z - 1, Material.NETHERRACK);
		set(world, x + 1, y - 1, z + 1, Material.NETHERRACK);
		set(world, x + 1, y - 1, z - 1, Material.NETHERRACK);
		set(world, x - 1, y - 1, z + 1, Material.NETHERRACK);
		set(world, x - 1, y - 1, z - 1, Material.NETHERRACK);
		set(world, x + 2, y - 1, z, Material.NETHERRACK);
		set(world, x - 2, y - 1, z, Material.NETHERRACK);
		set(world, x + 2, y - 1, z + 2, Material.NETHERRACK);
		set(world, x + 2, y - 1, z - 2, Material.NETHERRACK);
		set(world, x - 2, y - 1, z + 2, Material.NETHERRACK);
		set(world, x - 2, y - 1, z - 2, Material.NETHERRACK);
		set(world, x + 2, y - 1, z + 1, Material.NETHERRACK);
		set(world, x + 2, y - 1, z - 1, Material.NETHERRACK);
		set(world, x - 2, y - 1, z + 1, Material.NETHERRACK);
		set(world, x - 2, y - 1, z - 1, Material.NETHERRACK);
		set(world, x, y - 1, z + 2, Material.NETHERRACK);
		set(world, x + 1, y - 1, z + 2, Material.NETHERRACK);
		set(world, x - 1, y - 1, z + 2, Material.NETHERRACK);
		set(world, x, y - 1, z - 2, Material.NETHERRACK);
		set(world, x + 1, y - 1, z - 2, Material.NETHERRACK);
		set(world, x - 1, y - 1, z - 2, Material.NETHERRACK);
	}

	public void setNetherPillar(World world, int x, int y, int z) {
		set(world, x + 2, y, z + 2, Material.CRACKED_NETHER_BRICKS);
		set(world, x + 2, y + 1, z + 2, Material.RED_NETHER_BRICKS);
		set(world, x + 2, y + 2, z + 2, Material.NETHER_BRICKS);
		set(world, x + 2, y + 3, z + 2, Material.NETHER_BRICKS);
		set(world, x + 2, y + 4, z + 2, Material.NETHER_BRICK_WALL);
		set(world, x + 2, y + 5, z + 2, Material.NETHER_BRICK_SLAB);
		set(world, x + 2, y, z - 2, Material.NETHER_BRICKS);
		set(world, x + 2, y + 1, z - 2, Material.RED_NETHER_BRICKS);
		set(world, x + 2, y + 2, z - 2, Material.NETHER_BRICKS);
		set(world, x + 2, y + 3, z - 2, Material.CRACKED_NETHER_BRICKS);
		set(world, x + 2, y + 4, z - 2, Material.NETHER_BRICK_WALL);
		set(world, x + 2, y + 5, z - 2, Material.NETHER_BRICK_SLAB);
		set(world, x - 2, y, z - 2, Material.NETHER_BRICKS);
		set(world, x - 2, y + 1, z - 2, Material.RED_NETHER_BRICKS);
		set(world, x - 2, y + 2, z - 2, Material.CRACKED_NETHER_BRICKS);
		set(world, x - 2, y + 3, z - 2, Material.NETHER_BRICKS);
		set(world, x - 2, y + 4, z - 2, Material.NETHER_BRICK_WALL);
		set(world, x - 2, y + 5, z - 2, Material.NETHER_BRICK_SLAB);
		set(world, x - 2, y, z + 2, Material.NETHER_BRICKS);
		set(world, x - 2, y + 1, z + 2, Material.RED_NETHER_BRICKS);
		set(world, x - 2, y + 2, z + 2, Material.NETHER_BRICKS);
		set(world, x - 2, y + 3, z + 2, Material.NETHER_BRICKS);
		set(world, x - 2, y + 4, z + 2, Material.NETHER_BRICK_WALL);
		set(world, x - 2, y + 5, z + 2, Material.NETHER_BRICK_SLAB);
	}
}