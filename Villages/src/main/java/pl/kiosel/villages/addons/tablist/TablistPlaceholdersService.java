package pl.kiosel.villages.addons.tablist;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.utils.NumberRange;
import pl.kiosel.rosacore.utils.NumberUtils;
import pl.kiosel.rosacore.utils.TimeUtils;
import pl.kiosel.rosacore.utils.format.Formater;
import pl.kiosel.rosacore.utils.format.RangeFormatting;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.config.Settings;
import pl.kiosel.villages.config.TempMessages;
import pl.kiosel.villages.data.rank.DefaultTops;
import pl.kiosel.villages.data.rank.RankPlaceholdersService;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.UserRank;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageRank;
import pl.kiosel.villages.data.village.VillageRegion;
import pl.kiosel.villages.manager.VillageUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class TablistPlaceholdersService {

	private static final Pattern PLACEHOLDER = Pattern.compile("%([A-Za-z][A-Za-z0-9_-]*)%");
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

	private final AdvancedVillages plugin;
	private final RankPlaceholdersService rankPlaceholders;

	public TablistPlaceholdersService(AdvancedVillages plugin, RankPlaceholdersService rankPlaceholders) {
		this.plugin = plugin;
		this.rankPlaceholders = rankPlaceholders;
	}

	public String format(String text, User user, Player player) {
		if (text == null || text.isEmpty()) {
			return "";
		}

		String rankedText = this.rankPlaceholders.format(text, user);
		Matcher matcher = PLACEHOLDER.matcher(rankedText);
		StringBuilder result = new StringBuilder(rankedText.length());
		Map<String, String> cache = new HashMap<>();
		ZonedDateTime now = ZonedDateTime.now(configuredZoneId());
		Village village = user.getVillage().orElse(null);

		while (matcher.find()) {
			String key = matcher.group(1).toLowerCase(Locale.ROOT);
			String replacement = cache.computeIfAbsent(key, ignored -> this.resolve(key, user, player, village, now));
			matcher.appendReplacement(result, Matcher.quoteReplacement(replacement == null ? matcher.group() : replacement));
		}
		matcher.appendTail(result);
		return result.toString();
	}

	private String resolve(String key, User user, Player player, Village village, ZonedDateTime now) {
		if (key.startsWith("server-")) {
			return resolveServer(key.substring("server-".length()), player);
		}
		if (key.startsWith("player-")) {
			return resolvePlayer(key.substring("player-".length()), user, player);
		}
		if (key.startsWith("village-")) {
			return resolveVillage(key.substring("village-".length()), village);
		}
		if (key.startsWith("time-")) {
			return resolveTime(key.substring("time-".length()), now);
		}
		return null;
	}

	private String resolveServer(String key, Player player) {
		switch (key) {
			case "tps":
				return NumberUtils.formatTps(this.plugin.getNMS().getNmsServer().getTpsInLastMinute());
			case "player-count":
				return Integer.toString(Bukkit.getOnlinePlayers().size());
			case "village-count":
				return Integer.toString(this.plugin.getVillageManager().countVillage());
			case "online":
				return Long.toString(Bukkit.getOnlinePlayers().stream().filter(player::canSee).count());
			default:
				return null;
		}
	}

	private String resolvePlayer(String key, User user, Player player) {
		UserRank rank = user.getRank();
		switch (key) {
			case "name":
				return player.getName();
			case "display-name":
				return player.getDisplayName();
			case "ping":
				return Integer.toString(player.getPing());
			case "ping-format":
				return formatRange(player.getPing(), TempMessages.pingFormat, "PING");
			case "health":
				return formatNumber(player.getHealth());
			case "address":
				return Objects.toString(player.getAddress(), "");
			case "level":
				return Integer.toString(player.getLevel());
			case "experience":
				return formatNumber(player.getExp());
			case "world":
				return player.getWorld().getName();
			case "worldguard-region":
				return worldGuardRegions(player).stream().findFirst().orElse(TempMessages.noValue);
			case "worldguard-regions":
				List<String> regions = worldGuardRegions(player);
				return regions.isEmpty() ? TempMessages.noValue : String.join(", ", regions);
			case "balance":
				return this.plugin.getHookManager().getEconomy().getActiveHook().isPresent()
						? String.format(Locale.US, "%.2f", this.plugin.getEconomy().getBalance(player))
						: "";

			case "has-village":
				return Boolean.toString(user.hasVillage());
			case "specialization":
				return user.getSpecialization()
						.map(this.plugin.getSpecializationManager()::getDisplayName)
						.orElse(TempMessages.noValue);
			case "role":
				return user.hasVillage()
						? this.plugin.getRoleManager().getRole(user).getName()
						: TempMessages.noValue;
			case "position":
				return Integer.toString(rank.getPosition(DefaultTops.USER_POINTS_TOP));
			case "points":
				return Integer.toString(rank.getPoints());
			case "points-format":
				return formatRange(rank.getPoints(), TempMessages.pointsFormat, "POINTS");
			case "kills":
				return Integer.toString(rank.getKills());
			case "deaths":
				return Integer.toString(rank.getDeaths());
			case "assists":
				return Integer.toString(rank.getAssists());
			case "kdr":
				return formatNumber(rank.getKDR());
			case "kda":
				return formatNumber(rank.getKDA());
			default:
				return null;
		}
	}

	private String resolveTime(String key, ZonedDateTime now) {
		Locale language = languageLocale();
		switch (key) {
			case "hour":
				return twoDigits(now.getHour());
			case "minute":
				return twoDigits(now.getMinute());
			case "second":
				return twoDigits(now.getSecond());
			case "day-of-week":
				return now.getDayOfWeek().getDisplayName(TextStyle.FULL, language);
			case "day-of-month":
				return twoDigits(now.getDayOfMonth());
			case "month":
				return now.getMonth().getDisplayName(TextStyle.FULL, language);
			case "month-number":
				return twoDigits(now.getMonthValue());
			case "year":
				return Integer.toString(now.getYear());
			default:
				return null;
		}
	}

	private String resolveVillage(String key, Village village) {
		if (village == null) {
			return villageFallback(key);
		}

		VillageRank rank = village.getRank();
		switch (key) {
			case "name":
				return village.getName();
			case "tag":
				return village.getTag();
			case "owner":
				return village.getOwner().getName();
			case "level":
				return village.getLevel() == null ? "0" : Integer.toString(village.getLevel().getLevel());
			case "members":
				return village.getMembers().stream()
						.map(member -> member.isOnline() ? "&a" + member.getName() + "&r" : member.getName())
						.collect(Collectors.joining(", "));
			case "members-online":
				return Integer.toString(village.getOnlineMembers().size());
			case "members-all":
				return Integer.toString(village.getMembers().size());
			case "allies":
				return Integer.toString(this.plugin.getDiplomacyManager().getAllies(village).size());
			case "allies-tag":
				return this.plugin.getDiplomacyManager().getAlliesTags(village);
			case "wars":
				return Integer.toString(this.plugin.getDiplomacyManager().countCurrentWars(village));
			case "region-size":
				return village.getRegion().map(VillageRegion::getSize).map(String::valueOf).orElse(TempMessages.noValue);
			case "upkeep-cost":
				return this.plugin.getUpkeepManager().isEnabled()
						? Integer.toString(this.plugin.getUpkeepManager().calculateCost(village)) : "0";
			case "upkeep-time":
				return this.plugin.getUpkeepManager().isEnabled()
						? this.plugin.getVillageMessages().formatDuration(this.plugin.getUpkeepManager().getRemaining(village))
						: TempMessages.noValue;
			case "upkeep-missed":
				return this.plugin.getUpkeepManager().isEnabled()
						? Integer.toString(this.plugin.getUpkeepManager().getMissedPayments(village)) : "0";
			case "pvp":
				String pvpOn = plugin.getVillageMessages().textOrDefault(Lang.ON, "&aON");
				String pvpOff = plugin.getVillageMessages().textOrDefault(Lang.OFF, "&cOFF");
				return village.hasPvPEnabled() ? pvpOn : pvpOff;
			case "protection":
				return formatProtection(village.getProtection(), false);
			case "protection-time":
				return formatProtection(village.getProtection(), true);
			case "lives":
				return Integer.toString(village.getLives());
			case "lives-symbol":
				return VillageUtils.getLivesSymbol(village.getLives(), true);
			case "lives-symbol-all":
				return VillageUtils.getLivesSymbol(village.getLives(), false);
			case "position":
				return this.plugin.getVillageRankManager().isRankedVillage(village)
						? Integer.toString(rank.getPosition(DefaultTops.VILLAGE_AVG_POINTS_TOP))
						: TempMessages.noValue;
			case "points":
				return Integer.toString(rank.getPoints());
			case "avg-points":
				return Integer.toString(rank.getAveragePoints());
			case "points-format":
				return formatRange(rank.getAveragePoints(), TempMessages.pointsFormat, "POINTS");
			case "kills":
				return Integer.toString(rank.getKills());
			case "avg-kills":
				return Integer.toString(rank.getAverageKills());
			case "deaths":
				return Integer.toString(rank.getDeaths());
			case "avg-deaths":
				return Integer.toString(rank.getAverageDeaths());
			case "assists":
				return Integer.toString(rank.getAssists());
			case "avg-assists":
				return Integer.toString(rank.getAverageAssists());
			case "kdr":
				return formatNumber(rank.getKDR());
			case "avg-kdr":
				return formatNumber(rank.getAverageKDR());
			case "kda":
				return formatNumber(rank.getKDA());
			case "avg-kda":
				return formatNumber(rank.getAverageKDA());
			default:
				return null;
		}
	}

	private static String villageFallback(String key) {
		switch (key) {
			case "members-online":
			case "members-all":
			case "allies":
			case "wars":
			case "lives":
			case "points":
			case "avg-points":
			case "kills":
			case "avg-kills":
			case "deaths":
			case "avg-deaths":
			case "assists":
			case "avg-assists":
			case "kdr":
			case "avg-kdr":
			case "kda":
			case "avg-kda":
			case "pvp":
			case "upkeep-cost":
			case "upkeep-missed":
				return "0";
			default:
				return TempMessages.noValue;
		}
	}

	private static String formatRange(Number value, List<RangeFormatting> ranges, String placeholder) {
		String format = NumberRange.inRangeToString(value, ranges);
		return Formater.format(format, "%" + placeholder + "%", value);
	}

	private List<String> worldGuardRegions(Player player) {
		if (!this.plugin.getHookManager().getWorldGuard().isEnabled()) {
			return List.of();
		}
		List<String> names = this.plugin.getHookManager().getWorldGuard().getRegionNames(player.getLocation());
		return names == null ? List.of() : names;
	}

	private String formatProtection(Instant protection, boolean remainingTime) {
		if (protection == null || !protection.isAfter(Instant.now())) {
			return TempMessages.noValue;
		}
		return remainingTime
				? this.plugin.getVillageMessages().formatDuration(Duration.between(Instant.now(), protection))
				: DATE_FORMAT.withZone(configuredZoneId()).format(protection);
	}

	private static String twoDigits(int value) {
		return value < 10 ? "0" + value : Integer.toString(value);
	}

	private static Locale languageLocale() {
		String language = Settings.LANGUAGE_MODE.getString();
		return Locale.forLanguageTag(language == null ? "en-US" : language.replace('_', '-'));
	}

	private static ZoneId configuredZoneId() {
		return TimeUtils.readZoneId(Settings.TIME_ZONE.getString());
	}

	private static String formatNumber(Number value) {
		return NumberUtils.formatNumber(value, 2);
	}
}
