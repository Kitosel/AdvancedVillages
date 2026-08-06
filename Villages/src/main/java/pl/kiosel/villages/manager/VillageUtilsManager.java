package pl.kiosel.villages.manager;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import pl.kiosel.core.hooks.EconomyManager;
import pl.kiosel.core.locale.Message;
import pl.kiosel.core.utils.ColorUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiConfig;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.VillageManager;
import pl.kiosel.villages.settings.Settings;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.storage.DatabaseUserSerializer;
import pl.kiosel.villages.storage.DatabaseVillageSerializer;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class VillageUtilsManager {

	private final AdvancedVillages plugin;
	private final VillageManager manager;

	public VillageUtilsManager(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.manager = plugin.getVillageManager();
	}

	public void createVillage(Village village) throws SQLException {
//		plugin.getDataHelper().createVillage(village);
		this.plugin.getVillageManager().addVillage(village);
		for (User user : village.getMembers()) {
			DatabaseUserSerializer.serialize(user);
		}
		DatabaseVillageSerializer.serialize(village);
		plugin.getDebug().debug("Created village: " + village.getName());
	}

	public void deleteVillage(Village village) {
		plugin.getDataHelper().deleteVillage(village);
		plugin.getDebug().debug("Deleted village: " + village.getName());
	}

	public void destroyVillage(Village village, int hearth) {
		if (village.getLives() > 1) {
			village.updateLives(lives -> lives - hearth);
			village.setProtection(Instant.now().plus(Duration.ofHours(12)));
			plugin.getVillageRemoveManager().destroyVillage(village, true);
		} else {
			plugin.getVillageRemoveManager().removeVillage(village, true);
		}
	}

	@Nullable
	public Village getVillageAt(Location loc) {
		for (Village village : manager.getVillages())
			if (village.getVillageAt(loc) != null) return village;
		return null;
	}

	@Nullable
	public Village getVillageAt(Location loc, int customSize) {
		for (Village village : manager.getVillages())
			if (village.getVillageAt(loc, customSize) != null) return village;
		return null;
	}

	public boolean isVillageNearby(Location location, int radius) {
		for (Village village : manager.getVillages()) {
			Location vLoc = village.getLocation().get();
			if (vLoc == null || vLoc.getWorld() == null) continue;
			if (!vLoc.getWorld().equals(location.getWorld())) continue;

			if (vLoc.distance(location) <= radius) {
				return true;
			}
		}
		return false;
	}

	public boolean isSpawnNearby(Location location, int radius) {
		World world = plugin.getServer().getWorld(Settings.SPAWN_WORLD.getString());
		double x = Settings.SPAWN_X.getDouble();
		double y = Settings.SPAWN_Y.getDouble();
		double z = Settings.SPAWN_Z.getDouble();
		Location vLoc = new Location(world, x, y, z);
		if (vLoc.getWorld() == null) return false;
		if (!vLoc.getWorld().equals(location.getWorld())) return false;

		return vLoc.distance(location) <= radius;
	}

	public static List<String> replaceWithList(Village village, List<String> strings) {
		List<String> list = new ArrayList<>();
		for (String s : strings) {
			list.add(replaceWith(village, s));
		}
		return list;
	}

	public static String replaceWith(Village village, String string) {
		String notag = GuiConfig.no_tag;
		String tagset = GuiConfig.guis_village_setting_tag_set;
		String tagnotset = GuiConfig.guis_village_setting_tag_notset;
		return ColorUtils.tl(string.replace("%village_level%", village == null ? "" : village.getLevel().getLevel()+"")
				.replace("%village_cost%", village == null ? "" : village.getLevel().getCostEconomy()+"")
				.replace("%village_teleport%", village == null ? "" : village.tpToString())
				.replace("%village_name%", village == null ? "" : village.getName())
				.replace("%village_owner%", village == null ? "" : village.getOwner().getName())
				.replace("%village_bank%", village == null ? "" : village.getBank()+"")
				.replace("%village_life%", village == null ? "" : village.getLives()+"")
				.replace("%village_size%", village == null ? "" : village.getLevel().getSize()+"")
				.replace("%village_tag%", village == null ? "" : village.isTag() ? village.getTag() : notag)
				.replace("%village_pvp%", village == null ? "" : village.isPvp() ? GuiConfig.on : GuiConfig.off)
				.replace("%village_tnt%", village == null ? "" : village.isTnt() ? GuiConfig.on : GuiConfig.off)
				.replace("%istagset%", village == null ? "" : village.isTag() ? tagnotset : tagset));

	}

	public static Message replaceWith(Player player, Village village, String message) {
		String notag = GuiConfig.no_tag;
		String tagset = GuiConfig.guis_village_setting_tag_set;
		String tagnotset = GuiConfig.guis_village_setting_tag_notset;
		String noVillage = AdvancedVillages.getInstance().getScoreboardHandler().scoreboardNoVillage();

		Message formatedMessage = AdvancedVillages.getInstance().getLocale().newMessage(message)
				.processPlaceholder("player", player.getName())
				.processPlaceholder("player_name", player.getName())
				.processPlaceholder("player_money", EconomyManager.getBalance(player));

		formatedMessage
				.processPlaceholder("village_owner", village == null ? noVillage : village.getOwner().getName())
				.processPlaceholder("village_name", village == null ? noVillage : village.getName())
				.processPlaceholder("village_tag", village == null ? noVillage : village.isTag() ? village.getTag() : notag)
				.processPlaceholder("village_level", village == null ? noVillage : village.getLevel().getLevel() + "")
				.processPlaceholder("village_cost", village == null ? noVillage : village.getLevel().getCostEconomy() + "")
				.processPlaceholder("village_teleport", village == null ? noVillage : village.tpToString())
				.processPlaceholder("village_bank", village == null ? noVillage : village.getBank() + "")
				.processPlaceholder("village_life", village == null ? noVillage : village.getLives() + "")
				.processPlaceholder("village_size", village == null ? noVillage : village.getLevel().getSize() + "")
				.processPlaceholder("istagset", village == null ? noVillage : village.isTag() ? tagnotset : tagset);

		return formatedMessage;
	}
}