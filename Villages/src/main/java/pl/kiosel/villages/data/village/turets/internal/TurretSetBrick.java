package pl.kiosel.villages.data.village.turets.internal;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Stairs;
import pl.kiosel.villages.data.village.turets.Turret;

public class TurretSetBrick extends Turret {

	@Override
	public void setTurret(World world, int x, int y, int z) {
		set(world, x, y, z, Material.NOTE_BLOCK);
		set(world, x + 1, y, z, Material.STONE_BRICK_SLAB);
		set(world, x - 1, y, z, Material.STONE_BRICK_SLAB);
		set(world, x, y, z + 1, Material.STONE_BRICK_SLAB);
		set(world, x, y, z - 1, Material.STONE_BRICK_SLAB);
		set(world, x + 2, y + 3, z, Material.STONE_BRICKS);
		set(world, x - 2, y + 3, z, Material.STONE_BRICKS);
		set(world, x, y + 3, z + 2, Material.STONE_BRICKS);
		set(world, x, y + 3, z - 2, Material.STONE_BRICKS);
		set(world, x + 2, y + 4, z, Material.STONE_BRICK_SLAB);
		set(world, x - 2, y + 4, z, Material.STONE_BRICK_SLAB);
		set(world, x, y + 4, z + 2, Material.STONE_BRICK_SLAB);
		set(world, x, y + 4, z - 2, Material.STONE_BRICK_SLAB);
		set(world, x + 2, y + 4, z + 1, Material.STONE_BRICK_WALL);
		set(world, x + 2, y + 4, z - 1, Material.STONE_BRICK_WALL);
		set(world, x - 2, y + 4, z + 1, Material.MOSSY_STONE_BRICK_WALL);
		set(world, x - 2, y + 4, z - 1, Material.STONE_BRICK_WALL);
		set(world, x + 1, y + 4, z + 2, Material.STONE_BRICK_WALL);
		set(world, x - 1, y + 4, z + 2, Material.MOSSY_STONE_BRICK_WALL);
		set(world, x + 1, y + 4, z - 2, Material.STONE_BRICK_WALL);
		set(world, x - 1, y + 4, z - 2, Material.MOSSY_STONE_BRICK_WALL);

		setBrickFloor(world, x, y, z);
		setBrickStairs(world, x, y, z);
		setBrickPillar(world, x, y, z);
	}
	
	public void setBrickFloor(World world, int x, int y, int z) {
		set(world, x, y - 1, z, Material.STONE_BRICKS);
		set(world, x + 1, y - 1 ,z, Material.STONE);
		set(world, x - 1, y - 1 ,z, Material.STONE);
		set(world, x, y - 1 ,z + 1, Material.STONE);
		set(world, x, y - 1 ,z - 1, Material.STONE);
		set(world, x + 1, y - 1 ,z + 1, Material.STONE);
		set(world, x + 1, y - 1 ,z - 1, Material.STONE);
		set(world, x - 1, y - 1 ,z + 1, Material.STONE);
		set(world, x - 1, y - 1 ,z - 1, Material.STONE);
		set(world, x + 2, y - 1 ,z + 2, Material.STONE);
		set(world, x + 2, y - 1 ,z - 2, Material.STONE);
		set(world, x - 2, y - 1 ,z - 2, Material.STONE);
		set(world, x - 2, y - 1 ,z + 2, Material.STONE);
		set(world, x + 2, y - 1, z, Material.STONE);
		set(world, x - 2, y - 1, z, Material.STONE);
		set(world, x, y - 1 ,z - 2, Material.STONE);
		set(world, x, y - 1 ,z + 2, Material.STONE);
		set(world, x + 2, y - 1, z + 1, Material.COBBLESTONE);
		set(world, x + 2, y - 1, z - 1, Material.COBBLESTONE);
		set(world, x - 2, y - 1 ,z + 1, Material.COBBLESTONE);
		set(world, x - 2, y - 1 ,z - 1, Material.COBBLESTONE);
		set(world, x + 1, y - 1, z + 2, Material.COBBLESTONE);
		set(world, x + 1, y - 1, z - 2, Material.COBBLESTONE);
		set(world, x - 1, y - 1 ,z + 2, Material.COBBLESTONE);
		set(world, x - 1, y - 1 ,z - 2, Material.COBBLESTONE);
	}
	
	public void setBrickPillar(World world, int x, int y, int z) {
		set(world, x + 2, y, z + 2, Material.STONE_BRICKS);
		set(world, x + 2, y, z - 2, Material.STONE_BRICKS);
		set(world, x - 2, y, z + 2, Material.STONE_BRICKS);
		set(world, x - 2, y, z - 2, Material.STONE_BRICKS);
		set(world, x + 2, y + 1, z + 2, Material.CHISELED_STONE_BRICKS);
		set(world, x + 2, y + 1, z - 2, Material.CHISELED_STONE_BRICKS);
		set(world, x - 2, y + 1, z + 2, Material.CHISELED_STONE_BRICKS);
		set(world, x - 2, y + 1, z - 2, Material.CHISELED_STONE_BRICKS);
		set(world, x + 2, y + 2, z + 2, Material.STONE_BRICKS);
		set(world, x + 2, y + 2, z - 2, Material.STONE_BRICKS);
		set(world, x - 2, y + 2, z + 2, Material.STONE_BRICKS);
		set(world, x - 2, y + 2, z - 2, Material.STONE_BRICKS);
		set(world, x + 2, y + 3, z + 2, Material.CRACKED_STONE_BRICKS);
		set(world, x + 2, y + 3, z - 2, Material.CRACKED_STONE_BRICKS);
		set(world, x - 2, y + 3, z + 2, Material.CRACKED_STONE_BRICKS);
		set(world, x - 2, y + 3, z - 2, Material.CRACKED_STONE_BRICKS);
		set(world, x + 2, y + 4, z + 2, Material.CHISELED_STONE_BRICKS);
		set(world, x + 2, y + 4, z - 2, Material.CHISELED_STONE_BRICKS);
		set(world, x - 2, y + 4, z + 2, Material.CHISELED_STONE_BRICKS);
		set(world, x - 2, y + 4, z - 2, Material.CHISELED_STONE_BRICKS);
		set(world, x + 2, y + 5, z + 2, Material.STONE_BRICK_SLAB);
		set(world, x + 2, y + 5, z - 2, Material.STONE_BRICK_SLAB);
		set(world, x - 2, y + 5, z + 2, Material.STONE_BRICK_SLAB);
		set(world, x - 2, y + 5, z - 2, Material.STONE_BRICK_SLAB);
	}
	
	public void setBrickStairs(World world, int x, int y, int z) {
		Location sta1 = new Location(world, x + 2, y + 3, z + 1);
		
		sta1.getBlock().setType(Material.STONE_BRICK_STAIRS);
		BlockData block1 = sta1.getBlock().getBlockData();
		((Directional) block1).setFacing(BlockFace.NORTH);
		sta1.getBlock().setBlockData(block1);
		
		Location sta2 = new Location(world, x + 2, y + 3, z - 1);
		
		sta2.getBlock().setType(Material.STONE_BRICK_STAIRS);
		BlockData block2 = sta2.getBlock().getBlockData();
		((Directional) block2).setFacing(BlockFace.SOUTH);
		sta2.getBlock().setBlockData(block2);
		
		Location sta3 = new Location(world, x - 2, y + 3, z + 1);
		
		sta3.getBlock().setType(Material.STONE_BRICK_STAIRS);
		BlockData block3 = sta3.getBlock().getBlockData();
		((Directional) block3).setFacing(BlockFace.NORTH);
		sta3.getBlock().setBlockData(block3);
		
		Location sta4 = new Location(world, x - 2, y + 3, z - 1);
		
		sta4.getBlock().setType(Material.STONE_BRICK_STAIRS);
		BlockData block4 = sta4.getBlock().getBlockData();
		((Directional) block4).setFacing(BlockFace.SOUTH);
		sta4.getBlock().setBlockData(block4);
		
		Location sta5 = new Location(world, x + 1, y + 3, z + 2);
		
		sta5.getBlock().setType(Material.STONE_BRICK_STAIRS);
		BlockData block5 = sta5.getBlock().getBlockData();
		((Directional) block5).setFacing(BlockFace.WEST);
		sta5.getBlock().setBlockData(block5);
		
		Location sta6 = new Location(world, x - 1, y + 3, z + 2);
		
		sta6.getBlock().setType(Material.STONE_BRICK_STAIRS);
		BlockData block6 = sta6.getBlock().getBlockData();
		
		((Directional) block6).setFacing(BlockFace.EAST);
		sta6.getBlock().setBlockData(block6);
		
		Location sta7 = new Location(world, x + 1, y + 3, z - 2);
		
		sta7.getBlock().setType(Material.STONE_BRICK_STAIRS);
		BlockData block7 = sta7.getBlock().getBlockData();
		((Directional) block7).setFacing(BlockFace.WEST);
		sta7.getBlock().setBlockData(block7);
		
		Location sta8 = new Location(world, x - 1, y + 3, z - 2);
		
		sta8.getBlock().setType(Material.STONE_BRICK_STAIRS);
		BlockData block8 = sta8.getBlock().getBlockData();
		((Directional) block8).setFacing(BlockFace.EAST);
		sta8.getBlock().setBlockData(block8);
		
		//x+
		Location sta9 = new Location(world, x + 2, y + 2, z + 1);
		sta9.getBlock().setType(Material.STONE_BRICK_STAIRS);
		
		Stairs stairs1 = (Stairs) sta9.getBlock().getBlockData();
		stairs1.setHalf(Half.TOP);
		stairs1.setFacing(BlockFace.SOUTH);
		
		sta9.getBlock().setBlockData(stairs1);
		
		Location sta10 = new Location(world, x + 2, y + 2, z - 1);
		sta10.getBlock().setType(Material.STONE_BRICK_STAIRS);
		
		Stairs stairs2 = (Stairs) sta10.getBlock().getBlockData();
		stairs2.setHalf(Half.TOP);
		
		sta10.getBlock().setBlockData(stairs2);
		
		//x-
		Location sta11 = new Location(world, x - 2, y + 2, z + 1);
		sta11.getBlock().setType(Material.STONE_BRICK_STAIRS);
		
		Stairs stairs3 = (Stairs) sta11.getBlock().getBlockData();
		stairs3.setHalf(Half.TOP);
		stairs3.setFacing(BlockFace.SOUTH);
		
		sta11.getBlock().setBlockData(stairs3);
		
		Location sta12 = new Location(world, x - 2, y + 2, z - 1);
		sta12.getBlock().setType(Material.STONE_BRICK_STAIRS);
		
		Stairs stairs4 = (Stairs) sta12.getBlock().getBlockData();
		stairs4.setHalf(Half.TOP);
		
		sta12.getBlock().setBlockData(stairs4);
		
		//z+
		Location sta13 = new Location(world, x + 1, y + 2, z + 2);
		sta13.getBlock().setType(Material.STONE_BRICK_STAIRS);
		
		Stairs stairs5 = (Stairs) sta13.getBlock().getBlockData();
		stairs5.setHalf(Half.TOP);
		stairs5.setFacing(BlockFace.EAST);
		
		sta13.getBlock().setBlockData(stairs5);
		
		Location sta14 = new Location(world, x - 1, y + 2, z + 2);
		sta14.getBlock().setType(Material.STONE_BRICK_STAIRS);
		
		Stairs stairs6 = (Stairs) sta14.getBlock().getBlockData();
		stairs6.setHalf(Half.TOP);
		stairs6.setFacing(BlockFace.WEST);
		
		sta14.getBlock().setBlockData(stairs6);
		
		//z-
		Location sta15 = new Location(world, x + 1, y + 2, z - 2);
		sta15.getBlock().setType(Material.STONE_BRICK_STAIRS);
		
		Stairs stairs7 = (Stairs) sta15.getBlock().getBlockData();
		stairs7.setHalf(Half.TOP);
		stairs7.setFacing(BlockFace.EAST);
		
		sta15.getBlock().setBlockData(stairs7);
		
		Location sta16 = new Location(world, x - 1, y + 2, z - 2);
		sta16.getBlock().setType(Material.STONE_BRICK_STAIRS);
		
		Stairs stairs8 = (Stairs) sta16.getBlock().getBlockData();
		stairs8.setHalf(Half.TOP);
		stairs8.setFacing(BlockFace.WEST);
		
		sta16.getBlock().setBlockData(stairs8);	
	}	
}