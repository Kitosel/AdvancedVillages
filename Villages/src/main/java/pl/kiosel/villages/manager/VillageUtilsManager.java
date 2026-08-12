package pl.kiosel.villages.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import pl.kiosel.core.dependencies.net.kyori.adventure.title.Title;
import pl.kiosel.core.locale.Message;
import pl.kiosel.core.utils.TimeUtils;
import pl.kiosel.core.utils.format.RawString;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.logs.VillageLogType;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageManager;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.settings.Settings;
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
		Message title;
		Message subtitle;
		Message message;
		Title.Times times = Title.Times.times(Duration.ofMillis(20), Duration.ofSeconds(4), Duration.ofMillis(20));
		if (village.getLives() > 1) {
			Duration duration = TimeUtils.getDuration(Settings.VILLAGE_DESTROY_UNIT.getString(), Settings.VILLAGE_DESTROY_DURATION.getInt());
			if (duration == null) {
				duration = Duration.ofHours(12);
				plugin.getLogger().severe("Duration of village protect is wrong in config.yml using default 12 hours");
			}
			village.updateLives(lives -> lives - hearth);
			village.setProtection(Instant.now().plus(duration));

			String formattedDuration = plugin.getMessages().formatDuration(duration);
			for (User user : village.getOnlineMembers()) {
				message = replaceWithM(village, plugin.getMessages().text(Lang.VILLAGE_ATTACKED_MESSAGE))
						.processPlaceholder("attacker", attacker.getName())
						.processPlaceholder("time", formattedDuration);
				title = replaceWithM(village, plugin.getMessages().text(Lang.VILLAGE_ATTACKED_TITLE))
						.processPlaceholder("attacker", attacker.getName())
						.processPlaceholder("time", formattedDuration);
				subtitle = replaceWithM(village, plugin.getMessages().text(Lang.VILLAGE_ATTACKED_SUBTITLE))
						.processPlaceholder("attacker", attacker.getName())
						.processPlaceholder("time", formattedDuration);

				user.sendMessage(message.toText());
				plugin.getMessages().sendTitle(Bukkit.getPlayer(user.getUUID()),
						title.toText(), subtitle.toText(), times);
			}
			plugin.getVillageRemoveManager().destroyVillage(village, true);
		} else {
			for (User user : village.getOnlineMembers()) {
				message = replaceWithM(village, plugin.getMessages().text(Lang.VILLAGE_DESTROYED_MESSAGE))
						.processPlaceholder("attacker", attacker.getName());
				title = replaceWithM(village, plugin.getMessages().text(Lang.VILLAGE_DESTROYED_TITLE))
						.processPlaceholder("attacker", attacker.getName());
				subtitle = replaceWithM(village, plugin.getMessages().text(Lang.VILLAGE_DESTROYED_SUBTITLE))
						.processPlaceholder("attacker", attacker.getName());

				user.sendMessage(message.toText());
				plugin.getMessages().sendTitle(Bukkit.getPlayer(user.getUUID()),
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
			Location vLoc = village.getLocation().orNull();
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

	public static Message replaceWithM(Village village, String string) {
		return replaceWithM(village, string, "");
	}

	public static Message replaceWithM(Village village, String string, String noVillage) {
		Message message = instance.getMessages().get(string);
		applyGeneralPlaceholders(message);
		applyVillagePlaceholders(message, village, noVillage);
		return message;
	}

	public static String replaceWith(Village village, String string) {
		return replaceWithM(village, string).toText();
	}

	public static Message replacePlayer(Player player, String message) {
		return applyPlayerPlaceholders(instance.getMessages().get(message), player);
	}

	private static void applyGeneralPlaceholders(Message message) {
		message.processPlaceholder("max_lives", Settings.VILLAGE_MAX_LIVES.getInt())
				.processPlaceholder("default_lives", Settings.VILLAGE_DEFAULT_LIVES.getInt())
				.processPlaceholder("separator", instance.getMessages().get(Lang.SEPARATOR).toText());
	}

	private static void applyVillagePlaceholders(Message message, @Nullable Village village, String noVillage) {
		if (village == null) {
			for (String placeholder : VILLAGE_PLACEHOLDERS) {
				message.processPlaceholder(placeholder, noVillage);
			}
			return;
		}

		String noTag = instance.getMessages().textOrDefault(Lang.TAG_NO, "&cNONE");
		String on = instance.getMessages().textOrDefault(Lang.ON, "&aON");
		String off = instance.getMessages().textOrDefault(Lang.OFF, "&cOFF");
		int lives = village.getLives();

		message.processPlaceholder("village_level", village.getLevel().getLevel())
				.processPlaceholder("village_next_level", village.getLevel().getLevel() + 1)
				.processPlaceholder("village_cost", village.getLevel().getCostEconomy())
				.processPlaceholder("village_teleport", village.tpToString())
				.processPlaceholder("village_name", village.getName())
				.processPlaceholder("village_owner", village.getOwner().getName())
				.processPlaceholder("village_bank", village.getBank())
				.processPlaceholder("village_life", lives)
				.processPlaceholder("village_lives", lives)
				.processPlaceholder("village_life_as_symbol", getLivesSymbol(village.getLives(), true))
				.processPlaceholder("village_size", village.getLevel().getSize())
				.processPlaceholder("village_tag", village.isTag() ? village.getTag() : noTag)
				.processPlaceholder("village_pvp", village.isPvp() ? on : off)
				.processPlaceholder("village_tnt", village.isTnt() ? on : off)
				.processPlaceholder("village_animations", village.isAnimationsEnabled() ? on : off)
				.processPlaceholder("istagset", village.isTag()
						? instance.getGuiSettings().text("guis.village.settings.tag.tag_set", "&7Set")
						: instance.getGuiSettings().text("guis.village.settings.tag.tag_not_set", "&7Click to set"));
	}

	private static Message applyPlayerPlaceholders(Message message, Player player) {
		String lastOnline = TimeUtils.getStringDate(player.getLastPlayed());
		String nowOnline = instance.getMessages().get(Lang.PLAYER_ONLINE).toString();
		return message.processPlaceholder("player", player.getName())
				.processPlaceholder("player_name", player.getName())
				.processPlaceholder("player_uuid", player.getUniqueId().toString())
				.processPlaceholder("player_last_online", player.isOnline() ? nowOnline : lastOnline)
				.processPlaceholder("player_money", instance.getEconomy().getBalance(player))
				.processPlaceholder("player_ping", player.getPing())
				.processPlaceholder("player_world", player.getWorld().getName());
	}

	public static Message replaceWith(Player player, Village village, Lang lang) {
		return replaceWith(player, village, instance.getMessages().get(lang).toText());
	}

	public static Message replaceWith(Player player, Village village, String message) {
		String noVillage = replacePlayer(player, instance.getScoreboardHandler().scoreboardNoVillage()).toText();
		return applyPlayerPlaceholders(replaceWithM(village, message, noVillage), player);
	}
}
