package pl.kiosel.villages.addons.tablist;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.utils.NumberRange;
import pl.kiosel.rosacore.utils.format.Formater;
import pl.kiosel.rosacore.utils.format.RangeFormatting;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.config.TempMessages;
import pl.kiosel.villages.data.rank.DefaultTops;
import pl.kiosel.villages.data.rank.RankPlaceholdersService;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.UserRank;
import pl.kiosel.villages.data.village.Region;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageRank;
import pl.kiosel.villages.manager.VillageUtilsManager;
import pl.kiosel.villages.config.Settings;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
		StringBuffer result = new StringBuffer(rankedText.length());
		Map<String, String> cache = new HashMap<>();
		OffsetDateTime now = OffsetDateTime.now();
		Village village = user.getVillage().orElse(null);

		while (matcher.find()) {
			String key = matcher.group(1).toLowerCase(Locale.ROOT);
			String replacement = cache.computeIfAbsent(key, ignored -> this.resolve(key, user, player, village, now));
			matcher.appendReplacement(result, Matcher.quoteReplacement(replacement == null ? matcher.group() : replacement));
		}
		matcher.appendTail(result);
		return result.toString();
	}

	private String resolve(String key, User user, Player player, Village village, OffsetDateTime now) {
		UserRank rank = user.getRank();
		Locale language = languageLocale();
		switch (key) {
			case "tps":
				return Objects.toString(this.plugin.getNMS().getNmsServer().getTpsInLastMinute());
			case "players":
				return Integer.toString(Bukkit.getOnlinePlayers().size());
			case "villages":
				return Integer.toString(this.plugin.getVillageManager().countVillage());
			case "online":
				return Long.toString(Bukkit.getOnlinePlayers().stream().filter(player::canSee).count());

			case "name":
			case "player":
				return player.getName();
			case "displayname":
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
			case "exp":
				return formatNumber(player.getExp());
			case "world":
				return player.getWorld().getName();
			case "wg-region":
				return worldGuardRegions(player).stream().findFirst().orElse(TempMessages.noValue);
			case "wg-regions":
				List<String> regions = worldGuardRegions(player);
				return regions.isEmpty() ? TempMessages.noValue : String.join(", ", regions);
			case "vault-money":
				return this.plugin.getHookManager().getEconomy().getActiveHook().isPresent()
						? String.format(Locale.US, "%.2f", this.plugin.getEconomy().getBalance(player))
						: "";

			case "has-village":
				return Boolean.toString(user.hasVillage());
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
			case "logouts":
				return Integer.toString(rank.getLogouts());
			case "kdr":
				return formatNumber(rank.getKDR());
			case "kda":
				return formatNumber(rank.getKDA());

			case "hour":
				return twoDigits(now.getHour());
			case "minute":
				return twoDigits(now.getMinute());
			case "second":
				return twoDigits(now.getSecond());
			case "day_of_week":
				return now.getDayOfWeek().getDisplayName(TextStyle.FULL, language);
			case "day_of_month":
				return twoDigits(now.getDayOfMonth());
			case "month":
				return now.getMonth().getDisplayName(TextStyle.FULL, language);
			case "month_number":
				return twoDigits(now.getMonthValue());
			case "year":
				return Integer.toString(now.getYear());
			default:
				return key.startsWith("g-") ? resolveVillage(key.substring(2), village) : null;
		}
	}

	private String resolveVillage(String key, Village village) {
		if (village == null) {
			return villageFallback(key);
		}

		VillageRank rank = village.getRank();
		switch (key) {
			case "name":
			case "village":
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
				return village.getRegion().map(Region::getSize).map(String::valueOf).orElse(TempMessages.noValue);
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
				return VillageUtilsManager.getLivesSymbol(village.getLives(), true);
			case "lives-symbol-all":
				return VillageUtilsManager.getLivesSymbol(village.getLives(), false);
			case "position":
			case "rank":
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
			case "logouts":
				return Integer.toString(rank.getLogouts());
			case "avg-logouts":
				return Integer.toString(rank.getAverageLogouts());
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
			case "logouts":
			case "avg-logouts":
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
				: DATE_FORMAT.withZone(ZoneId.systemDefault()).format(protection);
	}

	private static String twoDigits(int value) {
		return value < 10 ? "0" + value : Integer.toString(value);
	}

	private static Locale languageLocale() {
		String language = Settings.LANGUAGE_MODE.getString();
		return Locale.forLanguageTag(language == null ? "en-US" : language.replace('_', '-'));
	}

	private static String formatNumber(Number value) {
		return value instanceof Float || value instanceof Double
				? String.format(Locale.US, "%.2f", value.doubleValue())
				: value.toString();
	}
}
