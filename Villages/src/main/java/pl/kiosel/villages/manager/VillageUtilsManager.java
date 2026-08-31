package pl.kiosel.villages.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.dependencies.adventure.adventure.title.Title;
import pl.kiosel.rosacore.utils.TimeUtils;
import pl.kiosel.rosacore.utils.format.RawString;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.logs.VillageLogType;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.config.Settings;
import pl.kiosel.villages.config.VillageMessage;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageManager;
import pl.kiosel.villages.storage.DatabaseUserSerializer;
import pl.kiosel.villages.storage.DatabaseVillageSerializer;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class VillageUtilsManager {

	private static final String[] VILLAGE_PLACEHOLDERS = {
			"village_level",
			"village_next_level",
			"village_cost",
			"village_teleport",
			"village_name",
			"village_owner",
			"village_bank",
			"village_life",
			"village_lives",
			"village_life_as_symbol",
			"village_size",
			"village_tag",
			"village_pvp",
			"village_tnt",
			"village_animations",
			"village_allies",
			"village_wars",
			"istagset"
	};

	private final AdvancedVillages plugin;
	private final VillageManager manager;
	private static final AdvancedVillages instance = AdvancedVillages.getInstance();

	public VillageUtilsManager(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.manager = plugin.getVillageManager();
	}

	public void createVillage(Village village) throws SQLException {
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

	public void attackOnVillage(Village village, int hearth, Player attacker) {
		plugin.getLogManager().record(village, VillageLogType.VILLAGE_ATTACK, attacker,
				"lives", Math.max(0, village.getLives() - hearth));
		VillageMessage title;
		VillageMessage subtitle;
		VillageMessage message;
		Title.Times times = Title.Times.times(Duration.ofMillis(20), Duration.ofSeconds(4), Duration.ofMillis(20));
		if (village.getLives() > 1) {
			Duration duration = TimeUtils.duration(Settings.VILLAGE_DESTROY_PROTECTION.getString(), Duration.ofHours(12), true);
			duration = plugin.getDevelopmentManager().applyAttackProtection(village, duration);
			if (duration == null) {
				duration = Duration.ofHours(12);
				plugin.getRosaLogger().severe("Duration of village protect is wrong in config.yml using default 12 hours");
			}
			village.updateLives(lives -> lives - hearth);
			village.setProtection(Instant.now().plus(duration));

			String formattedDuration = plugin.getVillageMessages().formatDuration(duration);
			for (User user : village.getOnlineMembers()) {
				message = replaceWithM(village, plugin.getVillageMessages().text(Lang.VILLAGE_ATTACKED_MESSAGE))
						.with("attacker", attacker.getName())
						.with("time", formattedDuration);
				title = replaceWithM(village, plugin.getVillageMessages().text(Lang.VILLAGE_ATTACKED_TITLE))
						.with("attacker", attacker.getName())
						.with("time", formattedDuration);
				subtitle = replaceWithM(village, plugin.getVillageMessages().text(Lang.VILLAGE_ATTACKED_SUBTITLE))
						.with("attacker", attacker.getName())
						.with("time", formattedDuration);

				user.sendMessage(message.toText());
				plugin.getVillageMessages().sendTitle(Bukkit.getPlayer(user.getUUID()),
						title.toText(), subtitle.toText(), times);
			}
			plugin.getVillageRemoveManager().destroyVillage(village, true);
		} else {
			for (User user : village.getOnlineMembers()) {
				message = replaceWithM(village, plugin.getVillageMessages().text(Lang.VILLAGE_DESTROYED_MESSAGE))
						.with("attacker", attacker.getName());
				title = replaceWithM(village, plugin.getVillageMessages().text(Lang.VILLAGE_DESTROYED_TITLE))
						.with("attacker", attacker.getName());
				subtitle = replaceWithM(village, plugin.getVillageMessages().text(Lang.VILLAGE_DESTROYED_SUBTITLE))
						.with("attacker", attacker.getName());

				user.sendMessage(message.toText());
				plugin.getVillageMessages().sendTitle(Bukkit.getPlayer(user.getUUID()),
						title.toText(), subtitle.toText(), times);
			}
			plugin.getVillageRemoveManager().removeVillage(village, true);
		}
	}

	public boolean isBlacklisted(World world) {
		List<String> worlds = Settings.VILLAGE_BLACKLISTED_WORLDS.getStringList();
		String worldName = world.getName();

		boolean isListed = worlds.stream()
				.anyMatch(name -> name.equalsIgnoreCase(worldName));

		return Settings.VILLAGE_BLACKLIST_AS_WHITELIST.getBoolean() != isListed;
	}

	@Nullable
	public Village getVillageAt(Location loc) {
		if (loc == null || loc.getWorld() == null) return null;
		for (Village village : manager.getVillagesView()) {
			if (village.getVillageAt(loc) != null) return village;
		}
		return null;
	}

	public boolean isVillageNearby(Location location, int radius) {
		double radiusSquared = (double) radius * radius;
		for (Village village : manager.getVillagesView()) {
			Location vLoc = village.getLocation().orElse(null);
			if (vLoc == null || vLoc.getWorld() == null) continue;
			if (!vLoc.getWorld().equals(location.getWorld())) continue;

			if (vLoc.distanceSquared(location) <= radiusSquared) {
				return true;
			}
		}
		return false;
	}

	public boolean isSpawnNearby(Location location, int radius) {
		Location vLoc = plugin.getTeleportManager().getSpawn().orElse(null);
		if (vLoc == null || vLoc.getWorld() == null) return false;
		if (!vLoc.getWorld().equals(location.getWorld())) return false;

		return vLoc.distanceSquared(location) + 40 <= (double) radius * radius;
	}

	private static final String liveSymbol = "\u2764";
	public static RawString full = new RawString("&c"+liveSymbol);
	public static RawString empty = new RawString("&8"+liveSymbol);

	public static String getLivesSymbol(int lives, boolean limited) {
		int maxLives = Settings.VILLAGE_MAX_LIVES.getInt();
		int shown = limited ? Math.min(lives, maxLives) : lives;
		String result = full.getValue().repeat(Math.max(0, shown));
		if (!limited) {
			return result;
		}
		result += empty.getValue().repeat(Math.max(0, maxLives - lives));
		return lives > maxLives ? result + "&a+" : result;
	}

	public static List<String> replaceWithList(Village village, List<String> strings) {
		List<String> list = new ArrayList<>(strings.size());
		for (String s : strings) {
			list.add(replaceWith(village, s));
		}
		return list;
	}

	public static VillageMessage replaceWithM(Village village, String string) {
		return replaceWithM(village, string, "");
	}

	public static VillageMessage replaceWithM(Village village, String string, String noVillage) {
		VillageMessage message = instance.getVillageMessages().raw(string);
		applyGeneralPlaceholders(message);
		applyVillagePlaceholders(message, village, noVillage);
		return message;
	}

	public static String replaceWith(Village village, String string) {
		return replaceWithM(village, string).toText();
	}

	public static VillageMessage replacePlayer(Player player, String message) {
		return applyPlayerPlaceholders(instance.getVillageMessages().raw(message), player);
	}

	private static void applyGeneralPlaceholders(VillageMessage message) {
		message.with("max_lives", Settings.VILLAGE_MAX_LIVES.getInt())
				.with("default_lives", Settings.VILLAGE_DEFAULT_LIVES.getInt())
				.with("separator", instance.getVillageMessages().get(Lang.SEPARATOR).toText());
	}

	private static void applyVillagePlaceholders(VillageMessage message, @Nullable Village village, String noVillage) {
		if (village == null) {
			for (String placeholder : VILLAGE_PLACEHOLDERS) {
				message.with(placeholder, noVillage);
			}
			return;
		}

		String noTag = instance.getVillageMessages().textOrDefault(Lang.TAG_NO, "&cNONE");
		String on = instance.getVillageMessages().textOrDefault(Lang.ON, "&aON");
		String off = instance.getVillageMessages().textOrDefault(Lang.OFF, "&cOFF");
		int lives = village.getLives();

		message.with("village_level", village.getLevel().getLevel())
				.with("village_next_level", village.getLevel().getLevel() + 1)
				.with("village_cost", village.getLevel().getCostEconomy())
				.with("village_teleport", village.tpToString())
				.with("village_name", village.getName())
				.with("village_owner", village.getOwner().getName())
				.with("village_bank", village.getBank())
				.with("village_life", lives)
				.with("village_lives", lives)
				.with("village_life_as_symbol", getLivesSymbol(village.getLives(), true))
				.with("village_size", village.getLevel().getSize())
				.with("village_tag", village.isTag() ? village.getTag() : noTag)
				.with("village_pvp", village.isPvp() ? on : off)
				.with("village_tnt", village.isTnt() ? on : off)
				.with("village_animations", village.isAnimationsEnabled() ? on : off)
				.with("village_allies", instance.getDiplomacyManager().getAllies(village).size())
				.with("village_wars", instance.getDiplomacyManager().countCurrentWars(village))
				.with("istagset", village.isTag()
						? instance.getGuiSettings().text("guis.village.settings.tag.tag_set", "&7Set")
						: instance.getGuiSettings().text("guis.village.settings.tag.tag_not_set", "&7Click to set"));
	}

	private static VillageMessage applyPlayerPlaceholders(VillageMessage message, Player player) {
		String lastOnline = TimeUtils.getStringDate(player.getLastPlayed());
		String nowOnline = instance.getVillageMessages().get(Lang.PLAYER_ONLINE).toString();
		return message.with("player", player.getName())
				.with("player_name", player.getName())
				.with("player_uuid", player.getUniqueId().toString())
				.with("player_last_online", player.isOnline() ? nowOnline : lastOnline)
				.with("player_money", instance.getEconomy().getBalance(player))
				.with("player_ping", player.getPing())
				.with("player_world", player.getWorld().getName());
	}

	public static VillageMessage replaceWith(Player player, Village village, Lang lang) {
		return replaceWith(player, village, instance.getVillageMessages().get(lang).toText());
	}

	public static VillageMessage replaceWith(Player player, Village village, String message) {
		String noVillage = replacePlayer(player, instance.getScoreboardHandler().scoreboardNoVillage()).toText();
		return applyPlayerPlaceholders(replaceWithM(village, message, noVillage), player);
	}
}
