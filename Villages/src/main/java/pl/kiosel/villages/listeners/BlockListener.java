package pl.kiosel.villages.listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import pl.kiosel.villages.Wioski;
import pl.kiosel.villages.api.events.VillageCreateEvent;
import pl.kiosel.villages.config.Config;
import pl.kiosel.villages.config.Language;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.village.Village;
import pl.kiosel.villages.village.VillageManager;
import pl.kiosel.villages.village.VillageNameGenerator;

import java.util.List;
import java.util.Objects;

public class BlockListener implements Listener {

	private final Wioski plugin;
	private final VillageNameGenerator villageNameGenerator;

	public BlockListener(Wioski plugin) {
		this.plugin = plugin;
		this.villageNameGenerator = new VillageNameGenerator();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlock(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();

		if (block.getType() != Material.NOTE_BLOCK) return;
		if (!event.getItemInHand().hasItemMeta() || !Objects.requireNonNull(event.getItemInHand().getItemMeta()).hasDisplayName()
				|| !event.getItemInHand().getItemMeta().hasLore()) return;

		if (!event.getItemInHand().getItemMeta().getDisplayName()
				.equalsIgnoreCase(plugin.getLang().getMessage(Lang.VILLAGE_BLOCK_NAME))) return;

		Language lang = plugin.getLang();

		if (!plugin.getApi().getEnabledWorlds().contains(block.getWorld().getName())) {
			player.sendMessage(lang.getMessage(Lang.DISABLED_WORLD));
			event.setCancelled(true);
			return;
		}

		if (VillageManager.getVillageByOfflineOwner(player.getName()) != null) {
			player.sendMessage(lang.getMessage(Lang.VILLAGE_IN));
			event.setCancelled(true);
			return;
		}

		int minDistance = Config.minimal_distance;
		if (plugin.getVillageManager().isVillageNearby(block.getLocation(), minDistance)) {
			player.sendMessage(lang.getMessage(Lang.VILLAGE_NEARBY)
					.replace("%distance%", String.valueOf(minDistance)));
			event.setCancelled(true);
			return;
		}

		int spawnMinDistance = Config.spawn_minimal_distance;
		if (plugin.getVillageManager().isSpawnNearby(block.getLocation(), spawnMinDistance)) {
			player.sendMessage(lang.getMessage(Lang.VILLAGE_NEARBY)
					.replace("%distance%", String.valueOf(spawnMinDistance)));
			event.setCancelled(true);
			return;
		}

		if (!plugin.getApi().hasMoney(player, Config.cost_set)) {
			player.sendMessage(lang.getMessage(Lang.NO_MONEY));
			event.setCancelled(true);
			return;
		}

		Village village = new Village(
				player.getName(),
				block.getLocation(),
				new Location(block.getWorld(), block.getX() + 0.5, block.getY() + 1, block.getZ() + 0.5),
				List.of(player.getUniqueId()),
				villageNameGenerator.getRandomName(),
				"false;false;false;false;",
				"false;false;false;false;",
				"15;1;3;0",
				"none"
		);

		VillageCreateEvent villageCreateEvent = new VillageCreateEvent(village, player);
		plugin.getServer().getPluginManager().callEvent(villageCreateEvent);
		if (villageCreateEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		}

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			plugin.getVillageManager().createVillage(village);

			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				plugin.getApi().removeMoney(player, Config.cost_set);
				Location loc = new Location(block.getWorld(), block.getX() + 0.5, block.getY() + 1.2, block.getZ() + 0.5);
				player.getWorld().spawnParticle(Particle.FLAME, loc, 50, 1, 1, 1);
				player.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 10, 1);
				player.teleport(loc);

				plugin.getUserManager().addUserToVillage(village, player, true);
				plugin.getUpgradeManager().upgrade(village);
			}, 5L);
		});
	}
}