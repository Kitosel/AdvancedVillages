package pl.kiosel.villages.data.village.turets.internal;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import pl.kiosel.villages.data.village.turets.Turret;

public class TurretSetEnd extends Turret {

	@Override
	public void setTurret(World world, int x, int y, int z) {
		set(world, x, y, z, Material.NOTE_BLOCK);
		set(world, x + 1, y - 2, z + 1, Material.BEACON);
		set(world, x + 1, y - 2, z - 1, Material.BEACON);
		set(world, x - 1, y - 2, z + 1, Material.BEACON);
		set(world, x - 1, y - 2, z - 1, Material.BEACON);

		set(world, x + 1, y + 4, z + 1, Material.PURPLE_STAINED_GLASS);
		set(world, x + 1, y + 4, z, Material.MAGENTA_STAINED_GLASS);
		set(world, x + 1, y + 4, z - 1, Material.PURPLE_STAINED_GLASS);
		set(world, x - 1, y + 4, z + 1, Material.PURPLE_STAINED_GLASS);
		set(world, x - 1, y + 4, z, Material.MAGENTA_STAINED_GLASS);
		set(world, x - 1, y + 4, z - 1, Material.PURPLE_STAINED_GLASS);
		set(world, x, y + 4, z + 1, Material.MAGENTA_STAINED_GLASS);
		set(world, x, y + 4, z, Material.PINK_STAINED_GLASS);
		set(world, x, y + 4, z - 1, Material.MAGENTA_STAINED_GLASS);

		set(world, x, y + 5, z, Material.AIR);
		set(world, x + 1, y + 5, z + 1, Material.AIR);
		set(world, x + 1, y + 5, z - 1, Material.AIR);
		set(world, x - 1, y + 5, z + 1, Material.AIR);
		set(world, x - 1, y + 5, z - 1, Material.AIR);
		
		setEndFloor(world, x, y, z);
		setEndIron(world, x, y, z);
		setEndPillar(world, x, y, z);
		setEndStairs(world, x, y, z);
	}
	
	public void setEndFloor(World world, int x, int y, int z) {
		set(world, x, y - 1, z, Material.END_STONE);
		set(world, x + 1, y - 1, z, Material.END_STONE_BRICKS);
		set(world, x - 1, y - 1, z, Material.END_STONE_BRICKS);
		set(world, x, y - 1, z + 1, Material.END_STONE_BRICKS);
		set(world, x, y - 1, z - 1, Material.END_STONE_BRICKS);
		set(world, x + 2, y - 1, z, Material.END_STONE);
		set(world, x - 2, y - 1, z, Material.END_STONE);
		set(world, x, y - 1, z + 2, Material.END_STONE);
		set(world, x, y - 1, z - 2, Material.END_STONE);

		set(world, x + 1, y - 1, z + 1, Material.WHITE_STAINED_GLASS);
		set(world, x + 2, y - 1, z + 1, Material.END_STONE_BRICKS);
		set(world, x + 1, y - 1, z + 2, Material.END_STONE_BRICKS);
		set(world, x + 1, y - 1, z - 1, Material.WHITE_STAINED_GLASS);
		set(world, x + 2, y - 1, z - 1, Material.END_STONE_BRICKS);
		set(world, x + 1, y - 1, z - 2, Material.END_STONE_BRICKS);
		set(world, x - 1, y - 1, z + 1, Material.WHITE_STAINED_GLASS);
		set(world, x - 2, y - 1, z + 1, Material.END_STONE_BRICKS);
		set(world, x - 1, y - 1, z + 2, Material.END_STONE_BRICKS);
		set(world, x - 1, y - 1, z - 1, Material.WHITE_STAINED_GLASS);
		set(world, x - 2, y - 1, z - 1, Material.END_STONE_BRICKS);
		set(world, x - 1, y - 1, z - 2, Material.END_STONE_BRICKS);
		set(world, x + 2, y - 1, z + 2, Material.END_STONE);
		set(world, x + 2, y - 1, z - 2, Material.END_STONE);
		set(world, x - 2, y - 1, z + 2, Material.END_STONE);
		set(world, x - 2, y - 1, z - 2, Material.END_STONE);
	}
	
	public void setEndIron(World world, int x, int y, int z) {
		set(world, x, y - 3, z, Material.IRON_BLOCK);
		set(world, x + 1, y - 3, z, Material.IRON_BLOCK);
		set(world, x - 1, y - 3, z, Material.IRON_BLOCK);
		set(world, x, y - 3, z + 1, Material.IRON_BLOCK);
		set(world, x, y - 3, z - 1, Material.IRON_BLOCK);
		set(world, x + 2, y - 3, z, Material.IRON_BLOCK);
		set(world, x - 2, y - 3, z, Material.IRON_BLOCK);
		set(world, x, y - 3, z + 2, Material.IRON_BLOCK);
		set(world, x, y - 3, z - 2, Material.IRON_BLOCK);
		set(world, x + 1, y - 3, z + 1, Material.IRON_BLOCK);
		set(world, x + 2, y - 3, z + 1, Material.IRON_BLOCK);
		set(world, x + 1, y - 3, z + 2, Material.IRON_BLOCK);
		set(world, x + 1, y - 3, z - 1, Material.IRON_BLOCK);
		set(world, x + 2, y - 3, z - 1, Material.IRON_BLOCK);
		set(world, x + 1, y - 3, z - 2, Material.IRON_BLOCK);
		set(world, x - 1, y - 3, z + 1, Material.IRON_BLOCK);
		set(world, x - 2, y - 3, z + 1, Material.IRON_BLOCK);
		set(world, x - 1, y - 3, z + 2, Material.IRON_BLOCK);
		set(world, x - 1, y - 3, z - 1, Material.IRON_BLOCK);
		set(world, x - 2, y - 3, z - 1, Material.IRON_BLOCK);
		set(world, x - 1, y - 3, z - 2, Material.IRON_BLOCK);
		set(world, x + 2, y - 3, z + 2, Material.IRON_BLOCK);
		set(world, x + 2, y - 3, z - 2, Material.IRON_BLOCK);
		set(world, x - 2, y - 3, z + 2, Material.IRON_BLOCK);
		set(world, x - 2, y - 3, z - 2, Material.IRON_BLOCK);
	}
	
	public void setEndStairs(World world, int x, int y, int z) {
		Location stairs1 = new Location(world, x + 2, y + 4, z + 1);
		Location stairs2 = new Location(world, x + 2, y + 4, z - 1);
		Location stairs3 = new Location(world, x - 2, y + 4, z + 1);
		Location stairs4 = new Location(world, x - 2, y + 4, z - 1);
		Location stairs5 = new Location(world, x + 1, y + 4, z + 2);
		Location stairs6 = new Location(world, x - 1, y + 4, z + 2);
		Location stairs7 = new Location(world, x + 1, y + 4, z - 2);
		Location stairs8 = new Location(world, x - 1, y + 4, z - 2);
		Location stairs9 = new Location(world, x + 2, y + 2, z + 1);
		Location stairs10 = new Location(world, x + 2, y + 2, z - 1);
		Location stairs11 = new Location(world, x - 2, y + 2, z + 1);
		Location stairs12 = new Location(world, x - 2, y + 2, z - 1);
		Location stairs13 = new Location(world, x + 1, y + 2, z + 2);
		Location stairs14 = new Location(world, x - 1, y + 2, z + 2);
		Location stairs15 = new Location(world, x + 1, y + 2, z - 2);
		Location stairs16 = new Location(world, x - 1, y + 2, z - 2);
		
		stairs1.getBlock().setType(Material.PURPUR_STAIRS);
		BlockData stairsdata1 = stairs1.getBlock().getBlockData();
		((Directional) stairsdata1).setFacing(BlockFace.WEST);
		stairs1.getBlock().setBlockData(stairsdata1);
		
		stairs2.getBlock().setType(Material.PURPUR_STAIRS);
		BlockData stairsdata2 = stairs2.getBlock().getBlockData();
		((Directional) stairsdata2).setFacing(BlockFace.WEST);
		stairs2.getBlock().setBlockData(stairsdata2);
		
		stairs3.getBlock().setType(Material.PURPUR_STAIRS);
		BlockData stairsdata3 = stairs3.getBlock().getBlockData();
		((Directional) stairsdata3).setFacing(BlockFace.EAST);
		stairs3.getBlock().setBlockData(stairsdata3);
		
		stairs4.getBlock().setType(Material.PURPUR_STAIRS);
		BlockData stairsdata4 = stairs4.getBlock().getBlockData();
		((Directional) stairsdata4).setFacing(BlockFace.EAST);
		stairs4.getBlock().setBlockData(stairsdata4);
		
		stairs5.getBlock().setType(Material.PURPUR_STAIRS);
		BlockData stairsdata5 = stairs5.getBlock().getBlockData();
		((Directional) stairsdata5).setFacing(BlockFace.NORTH);
		stairs5.getBlock().setBlockData(stairsdata5);
		
		stairs6.getBlock().setType(Material.PURPUR_STAIRS);
		BlockData stairsdata6 = stairs6.getBlock().getBlockData();
		((Directional) stairsdata6).setFacing(BlockFace.NORTH);
		stairs6.getBlock().setBlockData(stairsdata6);
		
		stairs7.getBlock().setType(Material.PURPUR_STAIRS);
		BlockData stairsdata7 = stairs7.getBlock().getBlockData();
		((Directional) stairsdata7).setFacing(BlockFace.SOUTH);
		stairs7.getBlock().setBlockData(stairsdata7);
		
		stairs8.getBlock().setType(Material.PURPUR_STAIRS);
		BlockData stairsdata8 = stairs8.getBlock().getBlockData();
		((Directional) stairsdata8).setFacing(BlockFace.SOUTH);
		stairs8.getBlock().setBlockData(stairsdata8);

		stairs9.getBlock().setType(Material.PURPUR_STAIRS);
		BlockData stairsdata9 = stairs9.getBlock().getBlockData();
		((Directional) stairsdata9).setFacing(BlockFace.SOUTH);
		stairs9.getBlock().setBlockData(stairsdata9);
		
		stairs10.getBlock().setType(Material.PURPUR_STAIRS);
		BlockData stairsdata10 = stairs10.getBlock().getBlockData();
		((Directional) stairsdata10).setFacing(BlockFace.NORTH);
		stairs10.getBlock().setBlockData(stairsdata10);
		
		stairs11.getBlock().setType(Material.PURPUR_STAIRS);
		BlockData stairsdata11 = stairs11.getBlock().getBlockData();
		((Directional) stairsdata11).setFacing(BlockFace.SOUTH);
		stairs11.getBlock().setBlockData(stairsdata11);
		
		stairs12.getBlock().setType(Material.PURPUR_STAIRS);
		BlockData stairsdata12 = stairs12.getBlock().getBlockData();
		((Directional) stairsdata12).setFacing(BlockFace.NORTH);
		stairs12.getBlock().setBlockData(stairsdata12);
		
		stairs13.getBlock().setType(Material.PURPUR_STAIRS);
		BlockData stairsdata13 = stairs13.getBlock().getBlockData();
		((Directional) stairsdata13).setFacing(BlockFace.EAST);
		stairs13.getBlock().setBlockData(stairsdata13);
		
		stairs14.getBlock().setType(Material.PURPUR_STAIRS);
		BlockData stairsdata14 = stairs14.getBlock().getBlockData();
		((Directional) stairsdata14).setFacing(BlockFace.WEST);
		stairs14.getBlock().setBlockData(stairsdata14);
		
		stairs15.getBlock().setType(Material.PURPUR_STAIRS);
		BlockData stairsdata15 = stairs15.getBlock().getBlockData();
		((Directional) stairsdata15).setFacing(BlockFace.EAST);
		stairs15.getBlock().setBlockData(stairsdata15);
		
		stairs16.getBlock().setType(Material.PURPUR_STAIRS);
		BlockData stairsdata16 = stairs16.getBlock().getBlockData();
		((Directional) stairsdata16).setFacing(BlockFace.WEST);
		stairs16.getBlock().setBlockData(stairsdata16);
		
	}
	
	public void setEndPillar(World world, int x, int y, int z) {
		set(world, x + 2, y, z + 2, Material.PURPUR_PILLAR);
		set(world, x + 2, y + 1, z + 2, Material.PURPUR_PILLAR);
		set(world, x + 2, y + 2, z + 2, Material.PURPUR_PILLAR);
		set(world, x + 2, y, z - 2, Material.PURPUR_PILLAR);
		set(world, x + 2, y + 1, z - 2, Material.PURPUR_PILLAR);
		set(world, x + 2, y + 2, z - 2, Material.PURPUR_PILLAR);
		set(world, x - 2, y, z + 2, Material.PURPUR_PILLAR);
		set(world, x - 2, y + 1, z + 2, Material.PURPUR_PILLAR);
		set(world, x - 2, y + 2, z + 2, Material.PURPUR_PILLAR);
		set(world, x - 2, y, z - 2, Material.PURPUR_PILLAR);
		set(world, x - 2, y + 1, z - 2, Material.PURPUR_PILLAR);
		set(world, x - 2, y + 2, z - 2, Material.PURPUR_PILLAR);
		set(world, x + 2, y + 3, z + 1, Material.PURPUR_PILLAR);
		set(world, x + 2, y + 3, z - 1, Material.PURPUR_PILLAR);
		set(world, x - 2, y + 3, z + 1, Material.PURPUR_PILLAR);
		set(world, x - 2, y + 3, z - 1, Material.PURPUR_PILLAR);
		set(world, x + 1, y + 3, z + 2, Material.PURPUR_PILLAR);
		set(world, x - 1, y + 3, z + 2, Material.PURPUR_PILLAR);
		set(world, x + 1, y + 3, z - 2, Material.PURPUR_PILLAR);
		set(world, x - 1, y + 3, z - 2, Material.PURPUR_PILLAR);

		set(world, x + 2, y + 3, z, Material.PURPUR_SLAB);
		set(world, x - 2, y + 3, z, Material.PURPUR_SLAB);
		set(world, x, y + 3, z + 2, Material.PURPUR_SLAB);
		set(world, x, y + 3, z - 2, Material.PURPUR_SLAB);
		set(world, x + 2, y, z + 1, Material.PURPUR_SLAB);
		set(world, x + 2, y, z - 1, Material.PURPUR_SLAB);
		set(world, x - 2, y, z + 1, Material.PURPUR_SLAB);
		set(world, x - 2, y, z - 1, Material.PURPUR_SLAB);
		set(world, x + 1, y, z + 2, Material.PURPUR_SLAB);
		set(world, x - 1, y, z + 2, Material.PURPUR_SLAB);
		set(world, x + 1, y, z - 2, Material.PURPUR_SLAB);
		set(world, x - 1, y, z - 2, Material.PURPUR_SLAB);
		set(world, x + 1, y + 5, z, Material.PURPUR_SLAB);
		set(world, x - 1, y + 5, z, Material.PURPUR_SLAB);
		set(world, x, y + 5, z + 1, Material.PURPUR_SLAB);
		set(world, x, y + 5, z - 1, Material.PURPUR_SLAB);
	}
}