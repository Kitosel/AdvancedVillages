package pl.kiosel.villages.data.outpost.handler;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import pl.kiosel.rosacore.material.ItemTag;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.api.events.OutpostCreateEvent;
import pl.kiosel.villages.api.events.VillageListener;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.config.Settings;
import pl.kiosel.villages.data.outpost.Outpost;
import pl.kiosel.villages.data.outpost.OutpostManager;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.VillagePermission;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.level.Level;

public class PlaceOutpostListener extends VillageListener {

	private final AdvancedVillages plugin;

	public PlaceOutpostListener(AdvancedVillages plugin) {
		super(plugin);
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlock(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();

		if (!isSameType(block.getType(), Material.LODESTONE)) return;
		boolean outpostBlock = ItemTag.has(event.getItemInHand(), "outpostBlock");
		if (!outpostBlock) return;

		User user = getUser(player);
		if (user == null) return;

		event.setCancelled(true);
		if (!plugin.isDev() || plugin.getOutpostManager() == null || plugin.getOutpostLevelManager() == null
				|| !plugin.getOutpostFile().getBoolean("enabled", true)) {
			sendLocalized(player, Lang.OUTPOST_DISABLED);
			return;
		}

		if (plugin.getVillageUtils().isBlacklisted(block.getWorld())) {
			sendLocalized(player, Lang.DISABLED_WORLD);
			return;
		}

		Village village = user.getPresentVillage();
		if (!user.hasVillage() || village == null) {
			sendLocalized(player, Lang.VILLAGE_NO);
			return;
		}
		if (!plugin.getRoleManager().hasPermission(user, VillagePermission.OUTPOST_MANAGE)) {
			sendLocalized(player, Lang.VILLAGE_NO_PERMISSION);
			return;
		}

		int requiredLevel = Math.max(1, plugin.getOutpostFile().getInt("required-outpost-level", 3));
		if (village.getLevel().getLevel() < requiredLevel) {
			sendLocalized(player, Lang.OUTPOST_LEVEL_REQUIRED, "level", requiredLevel);
			return;
		}
		OutpostManager manager = plugin.getOutpostManager();
		int maximum = Math.max(1, plugin.getOutpostFile().getInt("maximum-per-outpost", 3));
		if (manager.count(village) >= maximum) {
			sendLocalized(player, Lang.OUTPOST_LIMIT, "limit", maximum);
			return;
		}

		int minDistance = Math.max(0, plugin.getOutpostFile().getInt("minimum-distance-from-outpost", 125));
		if (plugin.getVillageUtils().isVillageNearby(block.getLocation(), minDistance)) {
			sendLocalized(player, Lang.OUTPOST_NEAR_VILLAGE, "distance", minDistance);
			return;
		}
		int outpostDistance = Math.max(0, plugin.getOutpostFile().getInt("minimum-distance-from-other-outpost", 100));
		if (manager.isNearby(block.getLocation(), outpostDistance)) {
			sendLocalized(player, Lang.OUTPOST_NEAR_OUTPOST, "distance", outpostDistance);
			return;
		}

		int spawnMinDistance = Settings.VILLAGE_SPAWN_MINIMAL_DISTANCE.getInt();
		if (plugin.getVillageUtils().isSpawnNearby(block.getLocation(), spawnMinDistance)) {
			sendLocalized(player, Lang.VILLAGE_SPAWN, "distance", spawnMinDistance);
			return;
		}

		if (!isBuildAreaClear(block.getLocation())) {
			sendLocalized(player, Lang.OUTPOST_AREA_BLOCKED);
			return;
		}

		Level level = plugin.getOutpostLevelManager().getLowestLevel();
		Outpost outpost = new Outpost(village, village.getName() + " Outpost " + (manager.count(village) + 1), block.getLocation(), level);

		OutpostCreateEvent outpostCreateEvent = new OutpostCreateEvent(outpost, player);
		plugin.getServer().getPluginManager().callEvent(outpostCreateEvent);
		if (outpostCreateEvent.isCancelled()) {
			return;
		}

		try {
			manager.add(outpost);
			event.setCancelled(false);
			plugin.getServer().getScheduler().runTask(plugin, () -> {
				try {
					manager.paste(outpost);
				} catch (RuntimeException exception) {
					plugin.getRosaLogger().log(java.util.logging.Level.SEVERE,
							"Could not paste outpost " + outpost.getUuid(), exception);
				}
			});
			sendLocalized(player, Lang.OUTPOST_CREATED, "outpost", outpost.getName());
		} catch (RuntimeException exception) {
			plugin.getRosaLogger().log(java.util.logging.Level.SEVERE, "Could not create outpost", exception);
			sendLocalized(player, Lang.OUTPOST_CREATE_FAILED);
		}
	}

	private boolean isBuildAreaClear(Location center) {
		World world = center.getWorld();
		if (world == null) return false;

		int centerY = center.getBlockY();
		if (centerY + OutpostManager.BUILD_MAX_Y >= world.getMaxHeight()
				|| centerY + OutpostManager.BUILD_MIN_Y < world.getMinHeight()) return false;

		for (int x = -OutpostManager.BUILD_RADIUS; x <= OutpostManager.BUILD_RADIUS; x++) {
			for (int y = 0; y <= OutpostManager.BUILD_MAX_Y; y++) {
				for (int z = -OutpostManager.BUILD_RADIUS; z <= OutpostManager.BUILD_RADIUS; z++) {
					if (x == 0 && y == 0 && z == 0) continue;
					if (!world.getBlockAt(center.getBlockX() + x, centerY + y, center.getBlockZ() + z)
							.getType().isAir()) return false;
				}
			}
		}
		return true;
	}
}
