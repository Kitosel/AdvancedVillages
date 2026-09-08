package pl.kiosel.villages.data.village.handler.turret;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.bukkit.Location;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class VillageProtectionExtent extends AbstractDelegateExtent {

	private final Map<Long, List<ProtectedArea>> protectedAreasByChunk = new HashMap<>();

	VillageProtectionExtent(Extent delegate, AdvancedVillages plugin, String worldName, UUID actorUuid) {
		super(delegate);
		List<ProtectedArea> areas = new ArrayList<>();
		for (Village village : plugin.getVillageManager().getVillagesView()) {
			Location center = village.getLocation().orElse(null);
			if (center == null || center.getWorld() == null || village.getLevel() == null) continue;
			if (!center.getWorld().getName().equals(worldName)) continue;

			Location turretMin = plugin.getVillageUtils().getTurretMin(center);
			Location turretMax = plugin.getVillageUtils().getTurretMax(center);
			boolean member = village.getMembers().stream()
					.map(User::getUUID)
					.anyMatch(actorUuid::equals);
			areas.add(new ProtectedArea(
					center.getBlockX(),
					center.getBlockZ(),
					Math.max(0, village.getLevel().getSize()),
					turretMin.getBlockX(),
					turretMin.getBlockY(),
					turretMin.getBlockZ(),
					turretMax.getBlockX(),
					turretMax.getBlockY(),
					turretMax.getBlockZ(),
					member
			));
		}
		for (ProtectedArea area : areas) this.index(area);
	}

	@Override
	public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 position, T block) throws WorldEditException {
		List<ProtectedArea> areas = this.protectedAreasByChunk.get(chunkKey(position.getX() >> 4, position.getZ() >> 4));
		if (areas != null) {
			for (ProtectedArea area : areas) {
				if (area.blocks(position)) return false;
			}
		}
		return super.setBlock(position, block);
	}

	private void index(ProtectedArea area) {
		int minChunkX = area.minProtectedX() >> 4;
		int maxChunkX = area.maxProtectedX() >> 4;
		int minChunkZ = area.minProtectedZ() >> 4;
		int maxChunkZ = area.maxProtectedZ() >> 4;
		for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
			for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
				this.protectedAreasByChunk.computeIfAbsent(chunkKey(chunkX, chunkZ), ignored -> new ArrayList<>())
						.add(area);
			}
		}
	}

	private static long chunkKey(int chunkX, int chunkZ) {
		return ((long) chunkX << 32) | (chunkZ & 0xffffffffL);
	}

	private record ProtectedArea(
			int centerX,
			int centerZ,
			int radius,
			int turretMinX,
			int turretMinY,
			int turretMinZ,
			int turretMaxX,
			int turretMaxY,
			int turretMaxZ,
			boolean member
	) {

		private boolean blocks(BlockVector3 position) {
			if (position.getX() >= this.turretMinX && position.getX() <= this.turretMaxX
					&& position.getY() >= this.turretMinY && position.getY() <= this.turretMaxY
					&& position.getZ() >= this.turretMinZ && position.getZ() <= this.turretMaxZ) {
				return true;
			}
			return !this.member
					&& Math.abs((long) position.getX() - this.centerX) <= this.radius
					&& Math.abs((long) position.getZ() - this.centerZ) <= this.radius;
		}

		private int minProtectedX() {
			return this.member ? this.turretMinX : Math.min(this.centerX - this.radius, this.turretMinX);
		}

		private int maxProtectedX() {
			return this.member ? this.turretMaxX : Math.max(this.centerX + this.radius, this.turretMaxX);
		}

		private int minProtectedZ() {
			return this.member ? this.turretMinZ : Math.min(this.centerZ - this.radius, this.turretMinZ);
		}

		private int maxProtectedZ() {
			return this.member ? this.turretMaxZ : Math.max(this.centerZ + this.radius, this.turretMaxZ);
		}
	}
}
