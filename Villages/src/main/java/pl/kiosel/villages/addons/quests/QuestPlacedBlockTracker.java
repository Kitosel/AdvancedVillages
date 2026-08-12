package pl.kiosel.villages.addons.quests;

import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import pl.kiosel.villages.AdvancedVillages;

import java.util.Arrays;

/** Persists player-placed quest resources in the chunk that contains them. */
final class QuestPlacedBlockTracker {

	private final NamespacedKey placedBlocksKey;

	QuestPlacedBlockTracker(AdvancedVillages plugin) {
		this.placedBlocksKey = new NamespacedKey(plugin, "quest-placed-blocks");
	}

	void track(Block block) {
		int packed = this.pack(block);
		if (packed < 0) {
			return;
		}

		PersistentDataContainer data = block.getChunk().getPersistentDataContainer();
		int[] positions = this.getPositions(data);
		int index = Arrays.binarySearch(positions, packed);
		if (index >= 0) {
			return;
		}

		int insertionPoint = -index - 1;
		int[] updated = new int[positions.length + 1];
		System.arraycopy(positions, 0, updated, 0, insertionPoint);
		updated[insertionPoint] = packed;
		System.arraycopy(positions, insertionPoint, updated, insertionPoint + 1,
				positions.length - insertionPoint);
		data.set(this.placedBlocksKey, PersistentDataType.INTEGER_ARRAY, updated);
	}

	boolean untrack(Block block) {
		int packed = this.pack(block);
		if (packed < 0) {
			return false;
		}

		PersistentDataContainer data = block.getChunk().getPersistentDataContainer();
		int[] positions = this.getPositions(data);
		int index = Arrays.binarySearch(positions, packed);
		if (index < 0) {
			return false;
		}

		if (positions.length == 1) {
			data.remove(this.placedBlocksKey);
			return true;
		}

		int[] updated = new int[positions.length - 1];
		System.arraycopy(positions, 0, updated, 0, index);
		System.arraycopy(positions, index + 1, updated, index,
				positions.length - index - 1);
		data.set(this.placedBlocksKey, PersistentDataType.INTEGER_ARRAY, updated);
		return true;
	}

	private int[] getPositions(PersistentDataContainer data) {
		int[] positions = data.get(this.placedBlocksKey, PersistentDataType.INTEGER_ARRAY);
		return positions == null ? new int[0] : positions;
	}

	private int pack(Block block) {
		int relativeY = block.getY() - block.getWorld().getMinHeight();
		int worldHeight = block.getWorld().getMaxHeight() - block.getWorld().getMinHeight();
		if (relativeY < 0 || relativeY >= worldHeight) {
			return -1;
		}
		return (relativeY << 8) | ((block.getX() & 15) << 4) | (block.getZ() & 15);
	}
}
