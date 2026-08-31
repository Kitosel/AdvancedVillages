package pl.kiosel.villages.data.village.handler;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.rosacore.listener.RosaListener;
import pl.kiosel.rosacore.location.Cuboid;
import pl.kiosel.rosacore.utils.PlayerUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.diplomacy.DiplomacyAttackResult;
import pl.kiosel.villages.addons.logs.VillageLogType;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.config.Settings;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.manager.VillageUtilsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InteractBlockListeners extends RosaListener {

	private final AdvancedVillages plugin;
	private final VillageUtilsManager villageManager;
	private final int maxX, maxY, maxZ, minX, minY, minZ;

	public InteractBlockListeners(AdvancedVillages plugin) {
		super(plugin);
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
			Location vloc = village.getLocation().orElseThrow();
			Location max = vloc.clone().add(maxX, maxY, maxZ);
			Location min = vloc.clone().add(minX, minY, minZ);
			Cuboid cuboid = new Cuboid(min, max);

			if (!cuboid.contains(location)) return;

			User user = plugin.getUserManager().findByUuid(player.getUniqueId()).get();
			ItemStack hand = player.getInventory().getItemInMainHand();

			if (village.isMember(user)) {
				event.setCancelled(true);
				if (Item.hasTag(hand, "villageHearth")) {
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

					VillageUtilsManager.replaceWith(player, village, Lang.VILLAGE_HEARTH_ADD).sendPrefixed(player);
					player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 0.8f, 2.0f);
				}
				return;
			}

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

		Location vloc = village.getLocation().orElseThrow();
		Location max = vloc.clone().add(maxX, maxY, maxZ);
		Location min = vloc.clone().add(minX, minY, minZ);
		Cuboid cuboid = new Cuboid(min, max);

		if (!cuboid.contains(location)) return;

		User user = plugin.getUserManager().findByUuid(player.getUniqueId()).get();
		ItemStack hand = player.getInventory().getItemInMainHand();

		if (!isSameType(block.getType(), Material.NOTE_BLOCK)) {
			event.setCancelled(true);
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
			return;
		}

		if (!village.isMember(user)) {
			event.setCancelled(true);
			if (Item.hasTag(hand, "villageDestroyer")) {
				event.setCancelled(true);
				if (!user.getVillage().isPresent()) {
					return;
				}
				Village attackerVillage = user.getPresentVillage();
				DiplomacyAttackResult diplomacyResult = plugin.getDiplomacyManager()
						.canAttack(attackerVillage, village);
				if (diplomacyResult != DiplomacyAttackResult.ALLOWED) {
					if (diplomacyResult == DiplomacyAttackResult.ALLIED) {
						plugin.getVillageMessages().sendPrefixed(player, Lang.DIPLOMACY_ALLIANCE_CANNOT_ATTACK);
					} else if (diplomacyResult == DiplomacyAttackResult.ATTACKER_HAS_NO_VILLAGE) {
						plugin.getVillageMessages().sendPrefixed(player, Lang.DIPLOMACY_WAR_ATTACKER_NO_VILLAGE);
					} else if (diplomacyResult == DiplomacyAttackResult.WAR_PREPARING) {
						plugin.getVillageMessages().sendPrefixed(player, Lang.DIPLOMACY_WAR_PREPARING,
								"time", plugin.getVillageMessages().formatDuration(
										plugin.getDiplomacyManager().getPreparationRemaining(attackerVillage, village)));
					} else {
						plugin.getVillageMessages().sendPrefixed(player, Lang.DIPLOMACY_WAR_REQUIRED);
					}
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 0.0f);
					return;
				}
				if (!Settings.VILLAGE_ATTACK_WHEN_OFFLINE.getBoolean()
						&& village.getOnlineMembers().isEmpty()) {
					plugin.getVillageMessages().get(Lang.VILLAGE_PROTECTED_OFFLINE).sendPrefixed(player);
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 0.0f);
					return;
				}
				if (!village.canBeAttacked()) {
					plugin.getVillageMessages().get(Lang.VILLAGE_PROTECTED).sendPrefixed(player);
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 0.0f);
					return;
				}
				if (attackerVillage != null) {
					plugin.getDiplomacyManager().recordVillageLifeLost(attackerVillage, village);
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
		Location vloc = village.getLocation().orElseThrow().clone();
		Cuboid cuboid = new Cuboid(vloc.add(minX, minY, minZ), vloc.add(maxX, maxY, maxZ));

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

		Location location = village.getLocation().orElseThrow();
		Cuboid cuboid = new Cuboid(
				location.clone().add(minX, minY, minZ),
				location.clone().add(maxX, maxY, maxZ)
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

		Location location = village.getLocation().orElseThrow();
		Cuboid cuboid = new Cuboid(
				location.clone().add(minX, minY, minZ),
				location.clone().add(maxX, maxY, maxZ)
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
					User user = plugin.getUserManager().findByUuid(player.getUniqueId()).orElseThrow();
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

		Location location = village.getLocation().orElseThrow();
		Cuboid cuboid = new Cuboid(
				location.clone().add(minX, minY, minZ),
				location.clone().add(maxX, maxY, maxZ)
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
				Location location = village.getLocation().orElseThrow();
				Cuboid cuboid = new Cuboid(
						location.clone().add(minX, minY, minZ),
						location.clone().add(maxX, maxY, maxZ)
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
				Location location = village.getLocation().orElseThrow();
				Cuboid cuboid = new Cuboid(
						location.clone().add(minX, minY, minZ),
						location.clone().add(maxX, maxY, maxZ)
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
			Location location = village.getLocation().orElseThrow();
			Cuboid cuboid = new Cuboid(
					location.clone().add(minX, minY, minZ),
					location.clone().add(maxX, maxY, maxZ)
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
			Location location = village.getLocation().orElseThrow();
			Cuboid cuboid = new Cuboid(
					location.clone().add(minX, minY, minZ),
					location.clone().add(maxX, maxY, maxZ)
			);

			if (cuboid.contains(event.getBlock().getLocation()) || cuboid.contains(pistonLocation)) {
				event.setCancelled(true);
			}
		}
	}
}
