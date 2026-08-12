package pl.kiosel.villages.data.village.handler;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.core.utils.Cuboid;
import pl.kiosel.dependencies.com.cryptomorin.xseries.XMaterial;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.manager.VillageUtilsManager;
import pl.kiosel.villages.settings.Settings;

import java.util.Objects;

public class InteractBlockListeners implements Listener {

	private final AdvancedVillages plugin;
	private final VillageUtilsManager villageManager;
	private final int maxX, maxY, maxZ, minX, minY, minZ;

	public InteractBlockListeners(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.villageManager = plugin.getVillageUtilsManager();
		maxX = 2;
		maxY = 6;
		maxZ = 2;
		minX = -2;
		minY = -3;
		minZ = -2;
	}

	private boolean isInEnabledWorld(Location loc) {
		if (loc == null || loc.getWorld() == null) return true;
		return plugin.getVillageUtilsManager().isBlacklisted(loc.getWorld());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlaceTurret(BlockPlaceEvent event) {
		Block block = event.getBlock();
		Location location = block.getLocation();

		if (isInEnabledWorld(location)) return;

		Player player = event.getPlayer();
		Village village = villageManager.getVillageAt(location);
		if (village != null) {
			Location max = village.getLocation().get().clone().add(maxX, maxY, maxZ);
			Location min = village.getLocation().get().clone().add(minX, minY, minZ);
			Cuboid cuboid = new Cuboid(min, max);

			if (!cuboid.contains(location)) return;

			event.setCancelled(true);
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.4f, 1.0f);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBreakTurret(BlockBreakEvent event) {
		Block block = event.getBlock();
		Location location = block.getLocation();

		if (isInEnabledWorld(location)) return;

		Player player = event.getPlayer();
		Village village = villageManager.getVillageAt(location);
		if (village == null) return;

		Location max = village.getLocation().get().clone().add(maxX, maxY, maxZ);
		Location min = village.getLocation().get().clone().add(minX, minY, minZ);
		Cuboid cuboid = new Cuboid(min, max);

		if (!cuboid.contains(location)) return;

		User user = plugin.getUserManager().findByUuid(player.getUniqueId()).get();
		ItemStack hand = player.getInventory().getItemInMainHand();

		if (block.getType() != XMaterial.NOTE_BLOCK.get()) {
			event.setCancelled(true);
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
			return;
		}

		if (!village.isMember(user)) {
			event.setCancelled(true);
			if (Item.hasTag(hand, "noBreak")) {
				event.setCancelled(true);
				if (Settings.VILLAGE_ATTACK_WHEN_OFFLINE.getBoolean()) {
					plugin.getMessages().get(Lang.VILLAGE_PROTECTED_OFFLINE).sendPrefixedMessage(player);
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 0.0f);
					return;
				}
				if (!village.canBeAttacked()) {
					plugin.getMessages().get(Lang.VILLAGE_PROTECTED).sendPrefixedMessage(player);
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 0.0f);
					return;
				}
				Objects.requireNonNull(location.getWorld()).dropItem(location.add(0.5, 1, 0.5), plugin.getApi().createHearthPart(), item -> {
					item.setGlowing(true);
					item.setUnlimitedLifetime(true);
					item.setVelocity(location.getDirection().multiply(0).setY(0.5));
				});
				plugin.getDebug().debug("Player " + player.getName() + " destroy central block of village " + village.getName());
				plugin.getVillageUtilsManager().attackOnVillage(village, 1, player);
				player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
			}
			return;
		}

		event.setCancelled(true);
		player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInteractTurret(PlayerInteractEvent event) {
		Action action = event.getAction();
		if (!(action == Action.PHYSICAL || action == Action.LEFT_CLICK_BLOCK))
			return;

		if (event.getClickedBlock() == null) return;

		Location location = event.getClickedBlock().getLocation();
		if (isInEnabledWorld(location)) return;

		Player player = event.getPlayer();
		Village village = villageManager.getVillageAt(location);
		if (village == null) return;

		Material type = event.getClickedBlock().getType();
		Location max = village.getLocation().get().clone().add(maxX, maxY, maxZ);
		Location min = village.getLocation().get().clone().add(minX, minY, minZ);
		Cuboid cuboid = new Cuboid(min, max);

		if (!cuboid.contains(location)) return;

		if (type.equals(Material.NOTE_BLOCK) && village.isCentralBlock(event.getClickedBlock())) return;
		event.setCancelled(true);
		player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.04f, 1.0f);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockIgnite(BlockIgniteEvent event) {
		Location loc = event.getBlock().getLocation();
		if (isInEnabledWorld(loc)) return;

		Village village = villageManager.getVillageAt(loc);
		if (village == null) return;

		Cuboid cuboid = new Cuboid(
				village.getLocation().get().clone().add(minX, minY, minZ),
				village.getLocation().get().clone().add(maxX, maxY, maxZ)
		);

		if (cuboid.contains(loc)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBurn(BlockBurnEvent event) {
		Location loc = event.getBlock().getLocation();
		if (isInEnabledWorld(loc)) return;

		Village village = villageManager.getVillageAt(loc);
		if (village == null) return;

		Cuboid cuboid = new Cuboid(
				village.getLocation().get().clone().add(minX, minY, minZ),
				village.getLocation().get().clone().add(maxX, maxY, maxZ)
		);

		if (cuboid.contains(loc)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFire(TNTPrimeEvent event) {
		Location location = event.getBlock().getLocation();
		if (isInEnabledWorld(location)) return;

		Entity entity = event.getPrimingEntity();
		Village village = villageManager.getVillageAt(location);
		if (village != null) {
			if(!village.isTnt()) {
				if (entity instanceof Player) {
					Player player = (Player) entity;
					User user = plugin.getUserManager().findByUuid(player.getUniqueId()).get();
					if (!village.isMember(user)) return;
					event.setCancelled(true);
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 0.5f, 1.0f);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		Location explosionLocation = event.getLocation();
		if (isInEnabledWorld(explosionLocation)) return;

		Village village = villageManager.getVillageAt(explosionLocation);
		if (village == null) return;

		Cuboid cuboid = new Cuboid(
				village.getLocation().get().clone().add(minX, minY, minZ),
				village.getLocation().get().clone().add(maxX, maxY, maxZ)
		);
		event.blockList().removeIf(block -> cuboid.contains(block.getLocation()));
		if (cuboid.contains(explosionLocation)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPistonExtend(BlockPistonExtendEvent event) {
		Location pistonLocation = event.getBlock().getLocation();
		if (isInEnabledWorld(pistonLocation)) return;

		for (Block movedBlock : event.getBlocks()) {
			Village village = villageManager.getVillageAt(movedBlock.getLocation());
			if (village != null) {
				Cuboid cuboid = new Cuboid(
						village.getLocation().get().clone().add(minX, minY, minZ),
						village.getLocation().get().clone().add(maxX, maxY, maxZ)
				);

				if (cuboid.contains(movedBlock.getLocation())) {
					event.setCancelled(true);
					return;
				}

				if (cuboid.contains(pistonLocation)) {
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPistonRetract(BlockPistonRetractEvent event) {
		Location pistonLocation = event.getBlock().getLocation();
		if (isInEnabledWorld(pistonLocation)) return;

		for (Block movedBlock : event.getBlocks()) {
			Village village = villageManager.getVillageAt(movedBlock.getLocation());
			if (village != null) {
				Cuboid cuboid = new Cuboid(
						village.getLocation().get().clone().add(minX, minY, minZ),
						village.getLocation().get().clone().add(maxX, maxY, maxZ)
				);

				if (cuboid.contains(movedBlock.getLocation()) || cuboid.contains(pistonLocation)) {
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBucket(PlayerBucketEmptyEvent event) {
		Location pistonLocation = event.getBlock().getLocation();
		if (isInEnabledWorld(pistonLocation)) return;

		Village village = villageManager.getVillageAt(event.getBlock().getLocation());
		if (village != null) {
			Cuboid cuboid = new Cuboid(
					village.getLocation().get().clone().add(minX, minY, minZ),
					village.getLocation().get().clone().add(maxX, maxY, maxZ)
			);

			if (cuboid.contains(event.getBlock().getLocation()) || cuboid.contains(pistonLocation)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBucket(PlayerBucketFillEvent event) {
		Location pistonLocation = event.getBlock().getLocation();
		if (isInEnabledWorld(pistonLocation)) return;

		Village village = villageManager.getVillageAt(event.getBlock().getLocation());
		if (village != null) {
			Cuboid cuboid = new Cuboid(
					village.getLocation().get().clone().add(minX, minY, minZ),
					village.getLocation().get().clone().add(maxX, maxY, maxZ)
			);

			if (cuboid.contains(event.getBlock().getLocation()) || cuboid.contains(pistonLocation)) {
				event.setCancelled(true);
			}
		}
	}
}
