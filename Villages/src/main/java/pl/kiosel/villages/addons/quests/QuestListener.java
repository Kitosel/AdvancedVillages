package pl.kiosel.villages.addons.quests;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import pl.kiosel.rosacore.listener.RosaListener;
import pl.kiosel.villages.AdvancedVillages;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class QuestListener extends RosaListener {

	private final VillageQuestManager questManager;
	private final AdvancedVillages plugin;
	private final NamespacedKey playerDropKey;
	private final QuestPlacedBlockTracker placedBlocks;
	private final Set<BlockPosition> ignoredBlockDrops = new HashSet<>();

	public QuestListener(AdvancedVillages plugin) {
		super(plugin);
		this.plugin = plugin;
		this.questManager = plugin.getQuestManager();
		this.playerDropKey = new NamespacedKey(plugin, "quest-player-drop");
		this.placedBlocks = new QuestPlacedBlockTracker(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent event) {
		Player killer = event.getEntity().getKiller();
		if (killer != null) {
			this.questManager.record(killer, QuestType.KILL_ENTITY, event.getEntityType().name(), 1);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if (this.placedBlocks.untrack(event.getBlock())) {
			BlockPosition position = BlockPosition.of(event.getBlock());
			this.ignoredBlockDrops.add(position);
			Bukkit.getScheduler().runTask(this.plugin, () -> this.ignoredBlockDrops.remove(position));
			return;
		}
		this.questManager.record(event.getPlayer(), QuestType.BREAK_BLOCK,
				event.getBlock().getType().name(), 1);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockDrops(BlockDropItemEvent event) {
		if (!this.ignoredBlockDrops.remove(BlockPosition.of(event.getBlock()))) {
			return;
		}
		for (Item item : event.getItems()) {
			item.getPersistentDataContainer().set(
					this.playerDropKey, PersistentDataType.BYTE, (byte) 1);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (this.questManager.shouldTrackPlacedBlock(event.getBlockPlaced().getType())) {
			this.placedBlocks.track(event.getBlockPlaced());
		}
		this.questManager.record(event.getPlayer(), QuestType.PLACE_BLOCK,
				event.getBlockPlaced().getType().name(), 1);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemPickup(EntityPickupItemEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		if (event.getItem().getPersistentDataContainer().has(this.playerDropKey, PersistentDataType.BYTE)) {
			return;
		}
		int collectedAmount = event.getItem().getItemStack().getAmount() - event.getRemaining();
		this.questManager.record((Player) event.getEntity(), QuestType.COLLECT_ITEM,
				event.getItem().getItemStack().getType().name(), collectedAmount);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemDrop(PlayerDropItemEvent event) {
		event.getItemDrop().getPersistentDataContainer().set(
				this.playerDropKey, PersistentDataType.BYTE, (byte) 1);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCraft(CraftItemEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getWhoClicked();
		ItemStack result = event.getRecipe().getResult();
		int amount = this.getCraftedAmount(event, player, result);
		this.questManager.record(player, QuestType.CRAFT_ITEM, result.getType().name(), amount);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onFish(PlayerFishEvent event) {
		if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH
				|| !(event.getCaught() instanceof Item)) {
			return;
		}
		ItemStack caught = ((Item) event.getCaught()).getItemStack();
		this.questManager.record(event.getPlayer(), QuestType.FISH_ITEM,
				caught.getType().name(), caught.getAmount());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSmelt(FurnaceExtractEvent event) {
		this.questManager.record(event.getPlayer(), QuestType.SMELT_ITEM,
				event.getItemType().name(), event.getItemAmount());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEnchant(EnchantItemEvent event) {
		this.questManager.record(event.getEnchanter(), QuestType.ENCHANT_ITEM,
				event.getItem().getType().name(), 1);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onConsume(PlayerItemConsumeEvent event) {
		this.questManager.record(event.getPlayer(), QuestType.CONSUME_ITEM,
				event.getItem().getType().name(), 1);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBreed(EntityBreedEvent event) {
		if (event.getBreeder() instanceof Player) {
			this.questManager.record((Player) event.getBreeder(), QuestType.BREED_ENTITY,
					event.getEntityType().name(), 1);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onTame(EntityTameEvent event) {
		if (event.getOwner() instanceof Player) {
			this.questManager.record((Player) event.getOwner(), QuestType.TAME_ENTITY,
					event.getEntityType().name(), 1);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onExperience(PlayerExpChangeEvent event) {
		if (!this.questManager.isGrantingQuestExperience(event.getPlayer())) {
			this.questManager.record(event.getPlayer(), QuestType.GAIN_EXPERIENCE,
					null, event.getAmount());
		}
	}

	private int getCraftedAmount(CraftItemEvent event, Player player, ItemStack result) {
		if (result.getType() == Material.AIR || result.getAmount() <= 0) {
			return 0;
		}
		if (!event.isShiftClick()) {
			return result.getAmount();
		}

		int possibleCrafts = Integer.MAX_VALUE;
		for (ItemStack ingredient : event.getInventory().getMatrix()) {
			if (ingredient != null && ingredient.getType() != Material.AIR) {
				possibleCrafts = Math.min(possibleCrafts, ingredient.getAmount());
			}
		}
		if (possibleCrafts == Integer.MAX_VALUE) {
			return 0;
		}

		int freeSpace = 0;
		for (ItemStack stored : player.getInventory().getStorageContents()) {
			if (stored == null || stored.getType() == Material.AIR) {
				freeSpace += result.getMaxStackSize();
			} else if (stored.isSimilar(result)) {
				freeSpace += Math.max(0, stored.getMaxStackSize() - stored.getAmount());
			}
		}
		int craftsBySpace = freeSpace / result.getAmount();
		return Math.min(possibleCrafts, craftsBySpace) * result.getAmount();
	}

	private static final class BlockPosition {
		private final UUID worldId;
		private final int x;
		private final int y;
		private final int z;

		private BlockPosition(UUID worldId, int x, int y, int z) {
			this.worldId = worldId;
			this.x = x;
			this.y = y;
			this.z = z;
		}

		private static BlockPosition of(Block block) {
			return new BlockPosition(block.getWorld().getUID(),
					block.getX(), block.getY(), block.getZ());
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}
			if (!(object instanceof BlockPosition)) {
				return false;
			}
			BlockPosition other = (BlockPosition) object;
			return this.x == other.x && this.y == other.y && this.z == other.z
					&& this.worldId.equals(other.worldId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.worldId, this.x, this.y, this.z);
		}
	}
}
