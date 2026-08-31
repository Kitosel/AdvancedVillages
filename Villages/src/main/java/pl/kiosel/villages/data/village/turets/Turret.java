package pl.kiosel.villages.data.village.turets;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Stairs;

import java.util.Objects;

public abstract class Turret {

    public void set(Location location, Material material) {
		Objects.requireNonNull(location, "location").getBlock().setType(Objects.requireNonNull(material, "material"));
    }

    public void set(Location location) {
        set(location, Material.AIR);
    }

    public void set(World world, int x, int y, int z, Material material) {
		Objects.requireNonNull(world, "world").getBlockAt(x, y, z).setType(Objects.requireNonNull(material, "material"));
    }

    public void set(World world, int x, int y, int z) {
		set(world, x, y, z, Material.AIR);
    }

    public void fill(Location location1, Location location2, Material material) {
		Objects.requireNonNull(location1, "location1");
		Objects.requireNonNull(location2, "location2");
		World world = Objects.requireNonNull(location1.getWorld(), "location1 world");
		if (!world.equals(location2.getWorld())) throw new IllegalArgumentException("Locations must be in the same world");
		int minimumX = Math.min(location1.getBlockX(), location2.getBlockX());
		int maximumX = Math.max(location1.getBlockX(), location2.getBlockX());
		int minimumY = Math.min(location1.getBlockY(), location2.getBlockY());
		int maximumY = Math.max(location1.getBlockY(), location2.getBlockY());
		int minimumZ = Math.min(location1.getBlockZ(), location2.getBlockZ());
		int maximumZ = Math.max(location1.getBlockZ(), location2.getBlockZ());
		for (int x = minimumX; x <= maximumX; x++) {
			for (int y = minimumY; y <= maximumY; y++) {
				for (int z = minimumZ; z <= maximumZ; z++) world.getBlockAt(x, y, z).setType(material);
			}
		}
    }

    public void setStairsBlock(Location location, Material material, BlockFace blockFace, Bisected.Half half) {
		Block block = Objects.requireNonNull(location, "location").getBlock();
		block.setType(Objects.requireNonNull(material, "material"));
		if (!(block.getBlockData() instanceof Stairs)) throw new IllegalArgumentException(material + " is not stairs");
		Stairs stairs1 = (Stairs) block.getBlockData();
        if (half != null)
            stairs1.setHalf(half);
        if (blockFace != null)
            stairs1.setFacing(blockFace);
		block.setBlockData(stairs1);
    }

    public void setDirectionalBlock(Location location, Material material, BlockFace blockFace) {
		Block block = Objects.requireNonNull(location, "location").getBlock();
		block.setType(Objects.requireNonNull(material, "material"));
		BlockData blockData = block.getBlockData();
		if (!(blockData instanceof Directional)) throw new IllegalArgumentException(material + " is not directional");
		((Directional) blockData).setFacing(Objects.requireNonNull(blockFace, "blockFace"));
		block.setBlockData(blockData);
    }

	protected final void paste(TurretStructure structure, World world, int x, int y, int z) {
        Objects.requireNonNull(structure, "structure").paste(world, x, y, z);
    }

    public abstract void setTurret(World world, int x, int y, int z);
}
