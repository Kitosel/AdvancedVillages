package pl.kiosel.villages.data.village.handler;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import pl.kiosel.rosacore.listener.RosaListener;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.api.events.PlayerEnterVillageEvent;
import pl.kiosel.villages.api.events.PlayerExitVillageEvent;
import pl.kiosel.villages.config.Settings;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Effects;
import pl.kiosel.villages.data.village.Village;

import java.util.HashSet;
import java.util.Set;

public class PlayerListeners extends RosaListener {

	private final AdvancedVillages plugin;
	private final Set<Player> insideVillagePlayers = new HashSet<>();

	public PlayerListeners(AdvancedVillages plugin) {
		super(plugin);
		this.plugin = plugin;
	}

	private boolean isWorldEnabled(World world) {
		return plugin.getVillageUtilsManager().isBlacklisted(world);
	}

	private void deny(Player player) {
		player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 1.0f, 1.0f);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (isWorldEnabled(player.getWorld())) return;

		User user = plugin.getUserManager().findByUuid(player.getUniqueId()).get();
		Village village = plugin.getVillageUtilsManager().getVillageAt(event.getBlock().getLocation());
		if (village == null) return;

		if (isSameType(event.getBlock().getType(), Material.NOTE_BLOCK) && village.isCentralBlock(event.getBlock())) return;

		if (!village.isMember(user)) {
			event.setCancelled(true);
			deny(player);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if (isWorldEnabled(player.getWorld())) return;

		User user = plugin.getUserManager().findByUuid(player.getUniqueId()).get();
		Village village = plugin.getVillageUtilsManager().getVillageAt(event.getBlock().getLocation());
		if (village != null && !village.isMember(user)) {
			event.setCancelled(true);
			deny(player);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (isWorldEnabled(player.getWorld())) return;
		if (event.getClickedBlock() == null) return;

		Block block = event.getClickedBlock();
		Village village = plugin.getVillageUtilsManager().getVillageAt(block.getLocation());
		if (village == null) return;

		if (isSameType(block.getType(), Material.NOTE_BLOCK) && village.isCentralBlock(event.getClickedBlock())) return;

		User user = plugin.getUserManager().findByUuid(player.getUniqueId()).orElseThrow();
		if (!village.isMember(user)) {
			boolean shouldCancel = isShouldCancel(event.getAction(), block);
			if (shouldCancel) {
				event.setCancelled(true);
				deny(player);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteractFrame(PlayerInteractEntityEvent event) {
		if (!(event.getRightClicked() instanceof ItemFrame frame) || !(event.getRightClicked() instanceof GlowItemFrame frame2)) return;

		Player player = event.getPlayer();
		if (!isWorldEnabled(player.getWorld())) return;

		Village village = plugin.getVillageUtilsManager().getVillageAt(frame.getLocation());
		if (village == null)
			village = plugin.getVillageUtilsManager().getVillageAt(frame2.getLocation());
		if (village == null) return;

		User user = plugin.getUserManager().findByUuid(player.getUniqueId()).get();
		if (!village.isMember(user)) {
			event.setCancelled(true);
			deny(player);
		}
	}

	private static boolean isShouldCancel(Action action, Block block) {
		Material type = block.getType();
		boolean shouldCancel = false;

		switch (action) {
			case PHYSICAL -> {
				if (type == Material.FARMLAND) shouldCancel = true;
			}
			case RIGHT_CLICK_BLOCK -> {
				if (type.name().contains("CHEST") || type.name().contains("FURNACE") ||
						type.name().contains("SHULKER_BOX") || type.name().contains("DOOR") ||
						type.name().contains("BUTTON") ||
						type == Material.LEVER || type == Material.NOTE_BLOCK) {
					shouldCancel = true;
				}
			}
			case LEFT_CLICK_BLOCK -> {
				if (type == Material.NOTE_BLOCK) {
					shouldCancel = true;
				}
			}
		}
		return shouldCancel;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player victim && event.getDamager() instanceof Player damager) {
			if (isWorldEnabled(damager.getWorld())) return;

			Village village = plugin.getVillageUtilsManager().getVillageAt(victim.getLocation());
			if (village == null) return;
			if(village.isSameVillage(damager, victim) && !village.isPvp()) {
				event.setCancelled(true);
				damager.playSound(damager.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
			}
		}
		if (event.getEntity() instanceof ItemFrame frame && event.getDamager() instanceof Player player) {
			if (isWorldEnabled(frame.getWorld())) return;
			User user = plugin.getUserManager().findByUuid(player.getUniqueId()).get();

			Village village = plugin.getVillageUtilsManager().getVillageAt(player.getLocation());
			if (village == null) return;
			if (!village.isMember(user)) {
				event.setCancelled(true);
			}
		}
		if (event.getEntity() instanceof GlowItemFrame frame && event.getDamager() instanceof Player player) {
			if (isWorldEnabled(frame.getWorld())) return;
			User user = plugin.getUserManager().findByUuid(player.getUniqueId()).get();

			Village village = plugin.getVillageUtilsManager().getVillageAt(player.getLocation());
			if (village == null) return;
			if (!village.isMember(user)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
		Player player = event.getPlayer();
		if (isWorldEnabled(player.getWorld())) return;
		User user = plugin.getUserManager().findByUuid(player.getUniqueId()).get();

		EntityType type = event.getRightClicked().getType();
		if (type == EntityType.ITEM_FRAME || type == EntityType.GLOW_ITEM_FRAME || type == EntityType.ARMOR_STAND) {
			Village village = plugin.getVillageUtilsManager().getVillageAt(event.getRightClicked().getLocation());
			if (village != null && !village.isMember(user)) {
				event.setCancelled(true);
				deny(player);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (isWorldEnabled(player.getWorld())) return;
		User user = plugin.getUserManager().findByUuid(player.getUniqueId()).get();

		Location destination = event.getTo();
		if (destination == null
				|| (event.getFrom().getBlockX() == destination.getBlockX()
				&& event.getFrom().getBlockZ() == destination.getBlockZ())) return;

		Village village = plugin.getVillageUtilsManager().getVillageAt(destination);

		if (village != null && village.isMember(user)) {
			if (village.isRegenerationActive()) {
				player.addPotionEffect(
						new PotionEffect(Effects.REGENERATION.getPotion().getPotionEffectType().orElseThrow(), 40,
								toBukkitAmplifier(Settings.EFFECTS_REGENERATION_AMPLIFIER.getInt())));
			}
			if (village.isSpeedActive()) {
				player.addPotionEffect(
						new PotionEffect(Effects.SPEED.getPotion().getPotionEffectType().orElseThrow(), 40,
								toBukkitAmplifier(Settings.EFFECTS_SPEED_AMPLIFIER.getInt())));
			}
			if (village.isJumpActive()) {
				player.addPotionEffect(
						new PotionEffect(Effects.JUMP_BOOST.getPotion().getPotionEffectType().orElseThrow(), 40,
								toBukkitAmplifier(Settings.EFFECTS_JUMP_BOOST_AMPLIFIER.getInt())));
			}
			if (village.isHasteActive()) {
				player.addPotionEffect(
						new PotionEffect(Effects.HASTE.getPotion().getPotionEffectType().orElseThrow(), 40,
								toBukkitAmplifier(Settings.EFFECTS_HASTE_AMPLIFIER.getInt())));
			}
		}

		if (insideVillagePlayers.contains(player)) {
			if (village == null) {
				insideVillagePlayers.remove(player);
				plugin.getServer().getPluginManager().callEvent(new PlayerExitVillageEvent(player));
			}
		} else if (village != null) {
			insideVillagePlayers.add(player);
			plugin.getServer().getPluginManager().callEvent(new PlayerEnterVillageEvent(village, player, insideVillagePlayers));
		}
	}

	private static int toBukkitAmplifier(int configuredLevel) {
		return Math.max(0, configuredLevel - 1);
	}

	@EventHandler
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		Player player = event.getPlayer();
		User user = plugin.getUserManager().findByUuid(player.getUniqueId()).get();
		if (isWorldEnabled(player.getWorld())) return;

		Village village = plugin.getVillageUtilsManager().getVillageAt(event.getBlock().getLocation());
		if (village != null && !village.isMember(user)) {
			event.setCancelled(true);
			deny(player);
		}
	}

	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		Player player = event.getPlayer();
		User user = plugin.getUserManager().findByUuid(player.getUniqueId()).get();
		if (isWorldEnabled(player.getWorld())) return;

		Village village = plugin.getVillageUtilsManager().getVillageAt(event.getBlock().getLocation());
		if (village != null && !village.isMember(user)) {
			event.setCancelled(true);
			deny(player);
		}
	}
}
