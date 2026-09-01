package pl.kiosel.villages.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import pl.kiosel.rosacore.listener.RosaListener;
import pl.kiosel.rosacore.utils.TimeUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.logs.VillageLogType;
import pl.kiosel.villages.api.events.VillageCreateEvent;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.config.Settings;
import pl.kiosel.villages.config.VillageMessages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Permission;
import pl.kiosel.villages.data.village.Region;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageBuilder;
import pl.kiosel.villages.data.village.level.Level;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.manager.VillageUtils;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class BlockListener extends RosaListener {

	private final AdvancedVillages plugin;

	public BlockListener(AdvancedVillages plugin) {
		super(plugin);
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlock(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();

		if (!isSameType(block.getType(), Material.NOTE_BLOCK)) return;
		boolean villageBlockTag = Item.hasTag(event.getItemInHand(), "villageBlock");
		boolean legacyVillageBlock = event.getItemInHand().hasItemMeta()
				&& Objects.requireNonNull(event.getItemInHand().getItemMeta()).hasDisplayName()
				&& event.getItemInHand().getItemMeta().hasLore()
				&& event.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(
						plugin.getVillageMessages().get(Lang.VILLAGE_BLOCK_NAME).toString());
		if (!villageBlockTag && !legacyVillageBlock) return;

		User user = this.plugin.getUserManager().findByPlayer(player).orElse(null);
		if (user == null) return;

		VillageMessages messages = plugin.getVillageMessages();

		event.setCancelled(true);

		if (plugin.getVillageUtils().isBlacklisted(block.getWorld())) {
			messages.sendPrefixed(player, Lang.DISABLED_WORLD);
			return;
		}

		if (user.hasVillage()) {
			messages.sendPrefixed(player, Lang.VILLAGE_IN);
			return;
		}

		int minDistance = Settings.VILLAGE_MINIMAL_DISTANCE.getInt();
		if (plugin.getVillageUtils().isVillageNearby(block.getLocation(), minDistance + 10)) {
			messages.sendPrefixed(player, Lang.VILLAGE_NEARBY, "distance", minDistance);
			return;
		}

		int spawnMinDistance = Settings.VILLAGE_SPAWN_MINIMAL_DISTANCE.getInt();
		if (plugin.getVillageUtils().isSpawnNearby(block.getLocation(), spawnMinDistance)) {
			messages.sendPrefixed(player, Lang.VILLAGE_SPAWN, "distance", spawnMinDistance);
			return;
		}

		Level level = plugin.getLevelManager().getLowestLevel();

		if (!plugin.getUpgradeManager().canUpgrade(player, level)) return;

		int lives = Settings.VILLAGE_DEFAULT_LIVES.getInt();
		if (lives > Settings.VILLAGE_MAX_LIVES.getInt()) {
			plugin.getRosaLogger().info("The default health points are greater than the maximum health points configured in the config file ");
		}

		Village village = new VillageBuilder(null, block.getLocation())
				.setOwner(user)
				.setTeleport(new Location(block.getWorld(), block.getX() + 0.5, block.getY() + 1, block.getZ() + 0.5))
				.setLevel(level)
				.setLives(lives)
				.setBank(0)
				.setEffectsDefault()
				.setRandomVillageName()
				.noTag()
				.build();

		VillageCreateEvent villageCreateEvent = new VillageCreateEvent(village, player);
		plugin.getServer().getPluginManager().callEvent(villageCreateEvent);
		if (villageCreateEvent.isCancelled()) {
			return;
		}
		event.setCancelled(false);

		Region region = new Region(village, village.getLocation().orElseGet(block::getLocation), level.getSize());
		village.setRegion(region);

		Duration duration = TimeUtils.getDuration("hour", 24);

		village.setPvP(true);
		village.setProtection(Instant.now().plus(duration));

		try {
			plugin.getVillageUtils().createVillage(village);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		user.setPermissions(Permission.OWNER);
		plugin.getLogManager().record(village, VillageLogType.VILLAGE_CREATED, player);

		Bukkit.getScheduler().runTask(plugin, () -> {
			plugin.getVillageAnimationManager().playCreation(village);
			plugin.getUpgradeManager().upgrade(village);
			village.teleportHome(player);
		});
		VillageUtils.replaceWithM(village, plugin.getVillageMessages().text(Lang.CREATED))
				.with("time", plugin.getVillageMessages().formatDuration(duration))
				.sendMessage(player);
	}
}
