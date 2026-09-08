package pl.kiosel.villages.data.village.handler.turret;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.rosacore.location.Cuboid;
import pl.kiosel.rosacore.material.ItemTag;
import pl.kiosel.rosacore.utils.PlayerUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.features.logs.VillageLogType;
import pl.kiosel.villages.api.events.VillageListener;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.config.Settings;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageUtils;

import java.util.ArrayList;
import java.util.List;

public class TurretListeners extends VillageListener {

	private final AdvancedVillages plugin;
	private final VillageUtils villageUtils;

	public TurretListeners(AdvancedVillages plugin) {
		super(plugin);
		this.plugin = plugin;
		this.villageUtils = plugin.getVillageUtils();

	}

	private boolean isInEnabledWorld(Location loc) {
		if (loc == null || loc.getWorld() == null) return true;
		return villageUtils.isBlacklisted(loc.getWorld());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlaceTurret(BlockPlaceEvent event) {
		Block block = event.getBlock();
		Location location = block.getLocation();

		if (isInEnabledWorld(location)) return;

		Player player = event.getPlayer();
		Village village = villageUtils.getVillageAt(location);
		if (village != null) {
			Location vloc = village.getLocation().orElseThrow();
			Location max = villageUtils.getTurretMax(vloc);
			Location min = villageUtils.getTurretMin(vloc);
			Cuboid cuboid = new Cuboid(min, max);

			if (!cuboid.contains(location)) return;

			User user = getUser(player);
			ItemStack hand = player.getInventory().getItemInMainHand();

			if (village.isMember(user)) {
				event.setCancelled(true);
				if (ItemTag.has(hand, "villageHearth")) {
					event.setCancelled(true);
					if (Settings.VILLAGE_MAX_LIVES.getInt() <= village.getLives()) {
						plugin.getVillageMessages().get(Lang.VILLAGE_MAX_LIVES).sendPrefixed(player);
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 0.0f);
						return;
					}
					plugin.getLogManager().record(village, VillageLogType.VILLAGE_HEARTH_ADD, player);
					plugin.getDebug().debug("Player " + player.getName() + " added live to a village " + village.getName());
					village.setLives(village.getLives() + 1);

					List<ItemStack> item = new ArrayList<>();
					item.add(plugin.getApi().createHearth());
					PlayerUtils.removeItem(player, item);

					VillageUtils.replaceWith(player, village, Lang.VILLAGE_HEARTH_ADD).sendPrefixed(player);
					player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 0.8f, 2.0f);
				}
				return;
			}

			event.setCancelled(true);
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.4f, 1.0f);
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
		Village village = villageUtils.getVillageAt(location);
		if (village == null) return;

		Material type = event.getClickedBlock().getType();
		Location vloc = village.getLocation().orElseThrow().clone();
		Location max = villageUtils.getTurretMax(vloc);
		Location min = villageUtils.getTurretMin(vloc);
		Cuboid cuboid = new Cuboid(max, min);

		if (!cuboid.contains(location)) return;

		if (type.equals(Material.NOTE_BLOCK) && village.isCentralBlock(event.getClickedBlock())) return;
		event.setCancelled(true);
		player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.04f, 1.0f);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockIgnite(BlockIgniteEvent event) {
		Location loc = event.getBlock().getLocation();
		if (isInEnabledWorld(loc)) return;

		Village village = villageUtils.getVillageAt(loc);
		if (village == null) return;

		Location vloc = village.getLocation().orElseThrow();
		Cuboid cuboid = new Cuboid(
				villageUtils.getTurretMax(vloc),
				villageUtils.getTurretMin(vloc)
		);

		if (cuboid.contains(loc)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBurn(BlockBurnEvent event) {
		Location loc = event.getBlock().getLocation();
		if (isInEnabledWorld(loc)) return;

		Village village = villageUtils.getVillageAt(loc);
		if (village == null) return;

		Location vloc = village.getLocation().orElseThrow();
		Cuboid cuboid = new Cuboid(
				villageUtils.getTurretMax(vloc),
				villageUtils.getTurretMin(vloc)
		);

		if (cuboid.contains(loc)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		Location explosionLocation = event.getLocation();
		if (isInEnabledWorld(explosionLocation)) return;

		Village village = villageUtils.getVillageAt(explosionLocation);
		if (village == null) return;

		Location vloc = village.getLocation().orElseThrow();
		Cuboid cuboid = new Cuboid(
				villageUtils.getTurretMax(vloc),
				villageUtils.getTurretMin(vloc)
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
			Village village = villageUtils.getVillageAt(movedBlock.getLocation());
			if (village != null) {
				Location vloc = village.getLocation().orElseThrow();
				Cuboid cuboid = new Cuboid(
						villageUtils.getTurretMax(vloc),
						villageUtils.getTurretMin(vloc)
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
			Village village = villageUtils.getVillageAt(movedBlock.getLocation());
			if (village != null) {
				Location vloc = village.getLocation().orElseThrow();
				Cuboid cuboid = new Cuboid(
						villageUtils.getTurretMax(vloc),
						villageUtils.getTurretMin(vloc)
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

		Village village = villageUtils.getVillageAt(event.getBlock().getLocation());
		if (village != null) {
			Location vloc = village.getLocation().orElseThrow();
			Cuboid cuboid = new Cuboid(
					villageUtils.getTurretMax(vloc),
					villageUtils.getTurretMin(vloc)
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

		Village village = villageUtils.getVillageAt(event.getBlock().getLocation());
		if (village != null) {
			Location vloc = village.getLocation().orElseThrow();
			Cuboid cuboid = new Cuboid(
					villageUtils.getTurretMax(vloc),
					villageUtils.getTurretMin(vloc)
			);

			if (cuboid.contains(event.getBlock().getLocation()) || cuboid.contains(pistonLocation)) {
				event.setCancelled(true);
			}
		}
	}
}
