package pl.kiosel.villages.data.village.handler;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.kiosel.core.utils.Cuboid;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageManager;

import java.util.ArrayList;
import java.util.List;

public class InteractBlockListeners implements Listener {

	private final AdvancedVillages plugin;
	private final VillageManager villageManager;
	private final int maxX, maxY, maxZ, minX, minY, minZ;

	public InteractBlockListeners(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.villageManager = plugin.getVillageManager();
		maxX = 2;
		maxY = 6;
		maxZ = 2;
		minX = -2;
		minY = -3;
		minZ = -2;
	}

	private boolean isInEnabledWorld(Location loc) {
		if (loc == null || loc.getWorld() == null) return true;
		return plugin.getBlacklistHandler().isBlacklisted(loc.getWorld());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlaceTurret(BlockPlaceEvent event) {
		Block block = event.getBlock();
		Location location = block.getLocation();

		if (isInEnabledWorld(location)) return;

		Player player = event.getPlayer();
		Village village = villageManager.getVillageAt(location);
		if (village != null) {
			Location max = village.getLocation().clone().add(maxX, maxY, maxZ);
			Location min = village.getLocation().clone().add(minX, minY, minZ);
			Cuboid cuboid = new Cuboid(min, max);

			if (!cuboid.contains(location)) return;

			event.setCancelled(true);
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.4f, 1.0f);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBreakTurret(BlockBreakEvent event) {
		Block block = event.getBlock();
		Location location = block.getLocation();

		if (isInEnabledWorld(location)) return;

		Player player = event.getPlayer();
		Village village = villageManager.getVillageAt(location);
		if (village != null) {
			Location max = village.getLocation().clone().add(maxX, maxY, maxZ);
			Location min = village.getLocation().clone().add(minX, minY, minZ);
			Cuboid cuboid = new Cuboid(min, max);

			if (!cuboid.contains(location)) return;

			event.setCancelled(true);
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
		}
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
		if (village != null) {
			Material type = event.getClickedBlock().getType();
			Location max = village.getLocation().clone().add(maxX, maxY, maxZ);
			Location min = village.getLocation().clone().add(minX, minY, minZ);
			Cuboid cuboid = new Cuboid(min, max);

			if (!cuboid.contains(location)) return;

			event.setCancelled(true);
			if (type.equals(Material.NOTE_BLOCK)) return;
			player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.04f, 1.0f);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockIgnite(BlockIgniteEvent event) {
		Location loc = event.getBlock().getLocation();
		if (isInEnabledWorld(loc)) return;

		Village village = villageManager.getVillageAt(loc);
		if (village == null) return;

		Cuboid cuboid = new Cuboid(
				village.getLocation().clone().add(minX, minY, minZ),
				village.getLocation().clone().add(maxX, maxY, maxZ)
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
				village.getLocation().clone().add(minX, minY, minZ),
				village.getLocation().clone().add(maxX, maxY, maxZ)
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
			if(!village.getVillageSettings().isTnt()) {
				if (entity instanceof Player) {
					Player player = (Player) entity;
					if (!village.isMember(player)) return;
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
				village.getLocation().clone().add(minX, minY, minZ),
				village.getLocation().clone().add(maxX, maxY, maxZ)
		);
		List<Block> protectedBlocks = new ArrayList<>();

		for (Block block : event.blockList()) {
			if (cuboid.contains(block.getLocation())) {
				protectedBlocks.add(block);
			}
		}
		event.blockList().removeAll(protectedBlocks);
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
						village.getLocation().clone().add(minX, minY, minZ),
						village.getLocation().clone().add(maxX, maxY, maxZ)
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
						village.getLocation().clone().add(minX, minY, minZ),
						village.getLocation().clone().add(maxX, maxY, maxZ)
				);

				if (cuboid.contains(movedBlock.getLocation()) || cuboid.contains(pistonLocation)) {
					event.setCancelled(true);
					return;
				}
			}
		}
	}
}