package pl.kiosel.villages.listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import pl.kiosel.core.locale.Locale;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.api.events.VillageCreateEvent;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.settings.Settings;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageManager;
import pl.kiosel.villages.data.village.VillageBuilder;

import java.util.List;
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

		if (VillageManager.getVillageByOfflineOwner(player.getName()) != null) {
			locale.getMessage(Lang.VILLAGE_IN.getPath()).sendPrefixedMessage(player);
			event.setCancelled(true);
			return;
		}

		int minDistance = Settings.VILLAGE_MINIMAL_DISTANCE.getInt();
		if (plugin.getVillageManager().isVillageNearby(block.getLocation(), minDistance)) {
			locale.getMessage(Lang.VILLAGE_NEARBY.getPath())
					.processPlaceholder("distance", minDistance).sendPrefixedMessage(player);
			event.setCancelled(true);
			return;
		}

		int spawnMinDistance = Settings.VILLAGE_SPAWN_MINIMAL_DISTANCE.getInt();
		if (plugin.getVillageManager().isSpawnNearby(block.getLocation(), spawnMinDistance)) {
			locale.getMessage(Lang.VILLAGE_SPAWN.getPath())
					.processPlaceholder("distance", spawnMinDistance).sendPrefixedMessage(player);
			event.setCancelled(true);
			return;
		}
		event.setCancelled(true);
		if (!plugin.getUpgradeManager().canUpgrade(player, plugin.getLevelManager().getLevel(1))) return;

		Village village = new VillageBuilder(block.getLocation())
				.setOwner(player.getName())
				.setOwnerUUID(player.getUniqueId())
				.setTeleport(new Location(block.getWorld(), block.getX() + 0.5, block.getY() + 1, block.getZ() + 0.5))
				.setLevel(plugin.getLevelManager().getLevel(1))
				.setMembers(List.of(player.getUniqueId()))
				.setRandomVillageName()
				.setEffectsDefault()
				.setLife(3)
				.setBank(0)
				.noTag()
				.build();

		VillageCreateEvent villageCreateEvent = new VillageCreateEvent(village, player);
		plugin.getServer().getPluginManager().callEvent(villageCreateEvent);
		if (villageCreateEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		}

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			plugin.getVillageManager().createVillage(village);

			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				plugin.getApi().removeMoney(player, plugin.getLevelManager().getLevel(1).getCostEconomy());
				Location loc = new Location(block.getWorld(), block.getX() + 0.5, block.getY() + 1.2, block.getZ() + 0.5);
				player.getWorld().spawnParticle(Particle.FLAME, loc, 50, 1, 1, 1);
				player.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 10, 1);
				player.teleport(loc);

				plugin.getDatabaseUserManager().addUserToVillage(village, player, true);
				plugin.getUpgradeManager().upgrade(village);
			}, 5L);
		});
	}
}