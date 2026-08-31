package pl.kiosel.villages.data.village.turets;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Stairs;

import java.util.*;

public final class TurretStructure {

	private final List<Placement> placements;
	@Getter private final Bounds bounds;

	private TurretStructure(List<Placement> placements, Bounds bounds) {
		this.placements = placements;
		this.bounds = bounds;
	}

	public static Builder builder() {
		return new Builder();
	}

	public int paste(Location center) {
		Objects.requireNonNull(center, "center");
		World world = Objects.requireNonNull(center.getWorld(), "center world");
		return paste(world, center.getBlockX(), center.getBlockY(), center.getBlockZ());
	}

	public int paste(World world, int x, int y, int z) {
		return paste(world, x, y, z, true);
	}

	public int paste(World world, int x, int y, int z, boolean applyPhysics) {
		Objects.requireNonNull(world, "world");
		for (Placement placement : this.placements) {
			Block block = world.getBlockAt(x + placement.offset.x, y + placement.offset.y, z + placement.offset.z);
			if (placement.blockData == null) {
				block.setType(placement.material, applyPhysics);
			} else {
				block.setBlockData(placement.blockData.clone(), applyPhysics);
			}
		}
		return this.placements.size();
	}

	public int size() {
		return this.placements.size();
	}

	public static final class Builder {

		private final Map<Offset, Placement> placements = new LinkedHashMap<>();

		public Builder block(Material material, int x, int y, int z) {
			return put(material, null, x, y, z);
		}

		public Builder blocks(Material material, int... coordinates) {
			Objects.requireNonNull(coordinates, "coordinates");
			if (coordinates.length % 3 != 0) {
				throw new IllegalArgumentException("Block coordinates must be supplied as x, y, z triplets");
			}
			for (int index = 0; index < coordinates.length; index += 3) {
				block(material, coordinates[index], coordinates[index + 1], coordinates[index + 2]);
			}
			return this;
		}

		public Builder cardinals(Material material, int y, int radius) {
			return mirrorXZ(material, radius, y, 0).mirrorXZ(material, 0, y, radius);
		}

		public Builder fill(Material material, int x1, int y1, int z1, int x2, int y2, int z2) {
			int minimumX = Math.min(x1, x2);
			int maximumX = Math.max(x1, x2);
			int minimumY = Math.min(y1, y2);
			int maximumY = Math.max(y1, y2);
			int minimumZ = Math.min(z1, z2);
			int maximumZ = Math.max(z1, z2);
			for (int x = minimumX; x <= maximumX; x++) {
				for (int y = minimumY; y <= maximumY; y++) {
					for (int z = minimumZ; z <= maximumZ; z++) block(material, x, y, z);
				}
			}
			return this;
		}

		public Builder layer(Material material, int y, int radius) {
			return fill(material, -radius, y, -radius, radius, y, radius);
		}

		public Builder outline(Material material, int y, int radius) {
			return outline(material, y, -radius, radius, -radius, radius);
		}

		public Builder outline(Material material, int y, int minimumX, int maximumX,
		                       int minimumZ, int maximumZ) {
			for (int x = Math.min(minimumX, maximumX); x <= Math.max(minimumX, maximumX); x++) {
				block(material, x, y, minimumZ);
				block(material, x, y, maximumZ);
			}
			for (int z = Math.min(minimumZ, maximumZ) + 1; z < Math.max(minimumZ, maximumZ); z++) {
				block(material, minimumX, y, z);
				block(material, maximumX, y, z);
			}
			return this;
		}

		public Builder cross(Material material, int y, int radius) {
			for (int offset = -Math.abs(radius); offset <= Math.abs(radius); offset++) {
				block(material, offset, y, 0);
				block(material, 0, y, offset);
			}
			return this;
		}

		public Builder corners(Material material, int y, int radius) {
			return corners(material, y, radius, radius);
		}

		public Builder corners(Material material, int y, int xRadius, int zRadius) {
			return mirrorXZ(material, Math.abs(xRadius), y, Math.abs(zRadius));
		}

		public Builder mirrorXZ(Material material, int x, int y, int z) {
			int[] xValues = x == 0 ? new int[]{0} : new int[]{x, -x};
			int[] zValues = z == 0 ? new int[]{0} : new int[]{z, -z};
			for (int mirroredX : xValues) {
				for (int mirroredZ : zValues) block(material, mirroredX, y, mirroredZ);
			}
			return this;
		}

		public Builder pillar(Material material, int x, int z, int fromY, int toY) {
			return fill(material, x, fromY, z, x, toY, z);
		}

		public Builder fourPillars(Material material, int radius, int fromY, int toY) {
			int absoluteRadius = Math.abs(radius);
			pillar(material, absoluteRadius, absoluteRadius, fromY, toY);
			pillar(material, absoluteRadius, -absoluteRadius, fromY, toY);
			pillar(material, -absoluteRadius, absoluteRadius, fromY, toY);
			return pillar(material, -absoluteRadius, -absoluteRadius, fromY, toY);
		}

		public Builder directional(Material material, int x, int y, int z, BlockFace facing) {
			Objects.requireNonNull(facing, "facing");
			BlockData data = Objects.requireNonNull(material, "material").createBlockData();
			if (!(data instanceof Directional)) {
				throw new IllegalArgumentException(material + " does not support a facing direction");
			}
			((Directional) data).setFacing(facing);
			return put(material, data, x, y, z);
		}

		public Builder stairs(Material material, int x, int y, int z, BlockFace facing) {
			return stairs(material, x, y, z, facing, Bisected.Half.BOTTOM);
		}

		public Builder stairs(Material material, int x, int y, int z,
		                      BlockFace facing, Bisected.Half half) {
			Objects.requireNonNull(facing, "facing");
			Objects.requireNonNull(half, "half");
			BlockData data = Objects.requireNonNull(material, "material").createBlockData();
			if (!(data instanceof Stairs)) {
				throw new IllegalArgumentException(material + " is not stairs");
			}
			Stairs stairs = (Stairs) data;
			stairs.setFacing(facing);
			stairs.setHalf(half);
			return put(material, stairs, x, y, z);
		}

		public TurretStructure build() {
			List<Placement> result = new ArrayList<>(this.placements.size());
			for (Placement placement : this.placements.values()) result.add(placement.copy());
			return new TurretStructure(List.copyOf(result), Bounds.of(result));
		}

		private Builder put(Material material, BlockData data, int x, int y, int z) {
			Material requiredMaterial = Objects.requireNonNull(material, "material");
			Offset offset = new Offset(x, y, z);
			this.placements.put(offset, new Placement(offset, requiredMaterial,
					data == null ? null : data.clone()));
			return this;
		}
	}

	public static final class Bounds {

		private final int minimumX;
		private final int minimumY;
		private final int minimumZ;
		private final int maximumX;
		private final int maximumY;
		private final int maximumZ;

		private Bounds(int minimumX, int minimumY, int minimumZ,
		               int maximumX, int maximumY, int maximumZ) {
			this.minimumX = minimumX;
			this.minimumY = minimumY;
			this.minimumZ = minimumZ;
			this.maximumX = maximumX;
			this.maximumY = maximumY;
			this.maximumZ = maximumZ;
		}

		private static Bounds of(List<Placement> placements) {
			if (placements.isEmpty()) return new Bounds(0, 0, 0, 0, 0, 0);
			int minimumX = Integer.MAX_VALUE;
			int minimumY = Integer.MAX_VALUE;
			int minimumZ = Integer.MAX_VALUE;
			int maximumX = Integer.MIN_VALUE;
			int maximumY = Integer.MIN_VALUE;
			int maximumZ = Integer.MIN_VALUE;
			for (Placement placement : placements) {
				minimumX = Math.min(minimumX, placement.offset.x);
				minimumY = Math.min(minimumY, placement.offset.y);
				minimumZ = Math.min(minimumZ, placement.offset.z);
				maximumX = Math.max(maximumX, placement.offset.x);
				maximumY = Math.max(maximumY, placement.offset.y);
				maximumZ = Math.max(maximumZ, placement.offset.z);
			}
			return new Bounds(minimumX, minimumY, minimumZ, maximumX, maximumY, maximumZ);
		}

		public int getMinimumX() { return this.minimumX; }
		public int getMinimumY() { return this.minimumY; }
		public int getMinimumZ() { return this.minimumZ; }
		public int getMaximumX() { return this.maximumX; }
		public int getMaximumY() { return this.maximumY; }
		public int getMaximumZ() { return this.maximumZ; }
		public int getWidth() { return this.maximumX - this.minimumX + 1; }
		public int getHeight() { return this.maximumY - this.minimumY + 1; }
		public int getDepth() { return this.maximumZ - this.minimumZ + 1; }
	}

	private static final class Placement {

		private final Offset offset;
		private final Material material;
		private final BlockData blockData;

		private Placement(Offset offset, Material material, BlockData blockData) {
			this.offset = offset;
			this.material = material;
			this.blockData = blockData;
		}

		private Placement copy() {
			return new Placement(this.offset, this.material, this.blockData == null ? null : this.blockData.clone());
		}
	}

	private static final class Offset {

		private final int x;
		private final int y;
		private final int z;

		private Offset(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) return true;
			if (!(other instanceof Offset)) return false;
			Offset offset = (Offset) other;
			return this.x == offset.x && this.y == offset.y && this.z == offset.z;
		}

		@Override
		public int hashCode() {
			int result = Integer.hashCode(this.x);
			result = 31 * result + Integer.hashCode(this.y);
			return 31 * result + Integer.hashCode(this.z);
		}
	}
}
