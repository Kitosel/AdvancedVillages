package pl.kiosel.villages.listeners.village;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import pl.kiosel.villages.Wioski;
import pl.kiosel.villages.api.events.PlayerEnterVillageEvent;
import pl.kiosel.villages.api.events.PlayerExitVillageEvent;
import pl.kiosel.villages.config.Config;
import pl.kiosel.villages.enums.Effects;
import pl.kiosel.villages.village.Village;

import java.util.HashSet;
import java.util.Set;

public class PlayerListeners implements Listener {

	private final Wioski plugin;
	private final Set<String> insideVillagePlayers = new HashSet<>();

	public PlayerListeners(Wioski plugin) {
		this.plugin = plugin;
	}

	private boolean isWorldEnabled(String worldName) {
		return !plugin.getApi().getEnabledWorlds().contains(worldName);
	}

	private void deny(Player player) {
		player.sendMessage("§cNie możesz tego zrobić, to nie twoja wioska!");
		player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 1.0f, 1.0f);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (isWorldEnabled(player.getWorld().getName())) return;

		Village village = plugin.getVillageManager().getVillageAt(event.getBlock().getLocation());
		if (village != null && !village.isMember(player)) {
			event.setCancelled(true);
			deny(player);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if (isWorldEnabled(player.getWorld().getName())) return;

		Village village = plugin.getVillageManager().getVillageAt(event.getBlock().getLocation());
		if (village != null && !village.isMember(player)) {
			event.setCancelled(true);
			deny(player);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (isWorldEnabled(player.getWorld().getName())) return;
		if (event.getClickedBlock() == null) return;

		Block block = event.getClickedBlock();
		Village village = plugin.getVillageManager().getVillageAt(block.getLocation());
		if (village == null) return;

		if (!village.isMember(player)) {
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
		if (!isWorldEnabled(player.getWorld().getName())) return;

		Village village = plugin.getVillageManager().getVillageAt(frame.getLocation());
		if (village == null)
			village = plugin.getVillageManager().getVillageAt(frame2.getLocation());
		if (village == null) return;

		if (!village.isMember(player)) {
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
			if (isWorldEnabled(damager.getWorld().getName())) return;

			Village village = plugin.getVillageManager().getVillageAt(victim.getLocation());
			if (village == null) return;
			if(village.isSameVillage(damager, victim) && !village.getVillageSettings().isPvp()) {
				event.setCancelled(true);
				damager.playSound(damager.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
			}
		}
		if (event.getEntity() instanceof ItemFrame frame && event.getDamager() instanceof Player player) {
			if (isWorldEnabled(frame.getWorld().getName())) return;

			Village village = plugin.getVillageManager().getVillageAt(player.getLocation());
			if (village == null) return;
			if (!village.isMember(player)) {
				event.setCancelled(true);
			}
		}
		if (event.getEntity() instanceof GlowItemFrame frame && event.getDamager() instanceof Player player) {
			if (isWorldEnabled(frame.getWorld().getName())) return;

			Village village = plugin.getVillageManager().getVillageAt(player.getLocation());
			if (village == null) return;
			if (!village.isMember(player)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
		Player player = event.getPlayer();
		if (isWorldEnabled(player.getWorld().getName())) return;

		EntityType type = event.getRightClicked().getType();
		if (type == EntityType.ITEM_FRAME || type == EntityType.GLOW_ITEM_FRAME || type == EntityType.ARMOR_STAND) {
			Village village = plugin.getVillageManager().getVillageAt(event.getRightClicked().getLocation());
			if (village != null && !village.isMember(player)) {
				event.setCancelled(true);
				deny(player);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (isWorldEnabled(player.getWorld().getName())) return;

		assert event.getTo() != null;
		if (event.getFrom().getBlockX() == event.getTo().getBlockX()
				&& event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

		Village village = plugin.getVillageManager().getVillageAt(player.getLocation());

		if (village != null && village.isMember(player)) {
			if (village.isRegeneration())
				player.addPotionEffect(new PotionEffect(Effects.REGENERATION.getPotionEffectType(), 40, Config.effect_regeneration_amplifier));
			if (village.isSpeed())
				player.addPotionEffect(new PotionEffect(Effects.SPEED.getPotionEffectType(), 40, Config.effect_speed_amplifier));
			if (village.isJump())
				player.addPotionEffect(new PotionEffect(Effects.JUMP_BOOST.getPotionEffectType(), 40, Config.effect_jump_amplifier));
			if (village.isHaste())
				player.addPotionEffect(new PotionEffect(Effects.HASTE.getPotionEffectType(), 40, Config.effect_haste_amplifier));
		}

		if (insideVillagePlayers.contains(player.getName())) {
			if (village == null) {
				insideVillagePlayers.remove(player.getName());
				plugin.getServer().getPluginManager().callEvent(new PlayerExitVillageEvent(player));
			}
		} else if (village != null) {
			insideVillagePlayers.add(player.getName());
			plugin.getServer().getPluginManager().callEvent(new PlayerEnterVillageEvent(village, player));
		}
	}

	@EventHandler
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		Player player = event.getPlayer();
		if (isWorldEnabled(player.getWorld().getName())) return;

		Village village = plugin.getVillageManager().getVillageAt(event.getBlock().getLocation());
		if (village != null && !village.isMember(player)) {
			event.setCancelled(true);
			deny(player);
		}
	}

	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		Player player = event.getPlayer();
		if (isWorldEnabled(player.getWorld().getName())) return;

		Village village = plugin.getVillageManager().getVillageAt(event.getBlock().getLocation());
		if (village != null && !village.isMember(player)) {
			event.setCancelled(true);
			deny(player);
		}
	}
}