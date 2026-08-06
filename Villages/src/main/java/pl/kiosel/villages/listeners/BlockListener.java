package pl.kiosel.villages.listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import pl.kiosel.core.hooks.EconomyManager;
import pl.kiosel.core.locale.Locale;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.Permission;
import pl.kiosel.villages.events.VillageCreateEvent;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.level.Level;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.settings.Settings;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageBuilder;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class BlockListener implements Listener {

	private final AdvancedVillages plugin;

	public BlockListener(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlock(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		User user = this.plugin.getUserManager().findByPlayer(player).get();

		if (block.getType() != Material.NOTE_BLOCK) return;
		if (!event.getItemInHand().hasItemMeta() || !Objects.requireNonNull(event.getItemInHand().getItemMeta()).hasDisplayName()
				|| !event.getItemInHand().getItemMeta().hasLore()) return;

		if (!event.getItemInHand().getItemMeta().getDisplayName()
				.equalsIgnoreCase(plugin.getLocale().getMessage(Lang.VILLAGE_BLOCK_NAME.getPath()).toString())) return;

		Locale locale = plugin.getLocale();

		if (plugin.getBlacklistHandler().isBlacklisted(block.getWorld())) {
			locale.getMessage(Lang.DISABLED_WORLD.getPath()).sendPrefixedMessage(player);
			event.setCancelled(true);
			return;
		}

		if (user.hasVillage()) {
			locale.getMessage(Lang.VILLAGE_IN.getPath()).sendPrefixedMessage(player);
			event.setCancelled(true);
			return;
		}

		int minDistance = Settings.VILLAGE_MINIMAL_DISTANCE.getInt();
		if (plugin.getVillageUtilsManager().isVillageNearby(block.getLocation(), minDistance)) {
			locale.getMessage(Lang.VILLAGE_NEARBY.getPath())
					.processPlaceholder("distance", minDistance).sendPrefixedMessage(player);
			event.setCancelled(true);
			return;
		}

		int spawnMinDistance = Settings.VILLAGE_SPAWN_MINIMAL_DISTANCE.getInt();
		if (plugin.getVillageUtilsManager().isSpawnNearby(block.getLocation(), spawnMinDistance)) {
			locale.getMessage(Lang.VILLAGE_SPAWN.getPath())
					.processPlaceholder("distance", spawnMinDistance).sendPrefixedMessage(player);
			event.setCancelled(true);
			return;
		}
		event.setCancelled(true);
		Level level = plugin.getLevelManager().getLowestLevel();
		if (!plugin.getUpgradeManager().canUpgrade(player, level)) return;

		Village village = new VillageBuilder(null, block.getLocation())
				.setOwner(user)
				.setTeleport(new Location(block.getWorld(), block.getX() + 0.5, block.getY() + 1, block.getZ() + 0.5))
				.setLevel(level)
				.setLives(3)
				.setBank(0)
				.setEffectsDefault()
				.setRandomVillageName()
				.noTag()
				.build();

		VillageCreateEvent villageCreateEvent = new VillageCreateEvent(village, player);
		plugin.getServer().getPluginManager().callEvent(villageCreateEvent);
		if (villageCreateEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		}

		village.setPvP(true);
		village.setProtection(Instant.now().plus(Duration.ofHours(24)));

		try {
			plugin.getVillageUtilsManager().createVillage(village);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		user.setVillage(village);
		user.setPermissions(Permission.OWNER);

		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			EconomyManager.withdrawBalance(player, plugin.getLevelManager().getLowestLevel().getCostEconomy());
			Location loc = new Location(block.getWorld(), block.getX() + 0.5, block.getY() + 1.2, block.getZ() + 0.5);
			player.getWorld().spawnParticle(Particle.FLAME, loc, 50, 1, 1, 1);
			player.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 10, 1);
			player.teleport(loc);
			plugin.getUpgradeManager().upgrade(village);
		}, 2L);
	}
}