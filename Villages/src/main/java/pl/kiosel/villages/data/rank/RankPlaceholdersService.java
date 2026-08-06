package pl.kiosel.villages.data.rank;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import panda.std.Option;
import pl.kiosel.core.chat.AdventureUtils;
import pl.kiosel.core.dependencies.net.kyori.adventure.text.Component;
import pl.kiosel.core.dependencies.net.kyori.adventure.text.TextReplacementConfig;
import pl.kiosel.core.utils.NumberRange;
import pl.kiosel.core.utils.format.RangeFormatting;
import pl.kiosel.core.utils.format.RawString;
import pl.kiosel.core.utils.format.Replaceable;
import pl.kiosel.villages.config.TablistConfiguration;
import pl.kiosel.villages.config.TempMessages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.UserRankManager;
import pl.kiosel.villages.data.user.top.UserTop;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageRankManager;
import pl.kiosel.villages.data.village.top.VillageTop;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Replaces every ranking placeholder in a single pass. */
public final class RankPlaceholdersService {

	private static final Pattern TOP = Pattern.compile(
			"%(PTOP|GTOP)(?:-([A-Za-z_]+))?-([0-9]+)%",
			Pattern.CASE_INSENSITIVE
	);
	private static final Pattern POSITION = Pattern.compile(
			"%((?:G-)?POSITION)-([A-Za-z_]+)%",
			Pattern.CASE_INSENSITIVE
	);
	private static final Pattern RANK_PLACEHOLDER = Pattern.compile(
			"%(?:(?:PTOP|GTOP)(?:-[A-Za-z_]+)?-[0-9]+|(?:G-)?POSITION-[A-Za-z_]+)%",
			Pattern.CASE_INSENSITIVE
	);

	private final TablistConfiguration tablistConfig;
	private final UserRankManager userRankManager;
	private final VillageRankManager villageRankManager;

	public RankPlaceholdersService(TablistConfiguration tablistConfig,
	                               UserRankManager userRankManager,
	                               VillageRankManager villageRankManager) {
		this.tablistConfig = tablistConfig;
		this.userRankManager = userRankManager;
		this.villageRankManager = villageRankManager;
	}

	public String format(String text, @Nullable User targetUser) {
		if (text == null || text.isEmpty()) {
			return "";
		}
		return replaceAll(POSITION, replaceAll(TOP, text, matcher -> resolveTop(matcher, targetUser)),
				matcher -> resolvePosition(matcher, targetUser));
	}

	/** Kept for API compatibility with the previous placeholder service. */
	public String format(@Nullable Object entity, String text, @Nullable User targetUser) {
		return this.format(text, targetUser);
	}

	private String resolveTop(Matcher matcher, @Nullable User targetUser) {
		String type = matcher.group(1).toUpperCase(Locale.ROOT);
		boolean legacy = matcher.group(2) == null;
		String comparator = legacy
				? (type.equals("PTOP") ? DefaultTops.USER_POINTS_TOP : DefaultTops.VILLAGE_AVG_POINTS_TOP)
				: matcher.group(2).toLowerCase(Locale.ROOT);

		int index;
		try {
			index = Integer.parseInt(matcher.group(3));
		} catch (NumberFormatException ignored) {
			return TempMessages.noValue;
		}
		if (index < 1) {
			return TempMessages.noValue;
		}

		if (type.equals("PTOP")) {
			Option<UserTop> top = this.userRankManager.getTop(comparator);
			if (top.isEmpty()) {
				return TempMessages.noValue;
			}
			Option<User> user = top.get().getUser(index);
			if (user.isEmpty()) {
				return TempMessages.noValue;
			}

			Number value = top.get().getComparator().getValue(user.get().getRank());
			String suffix = legacy
					? formatLegacyValue(value, TempMessages.ptopPoints.getValue())
					: formatTopValue(value, TempMessages.ptop.getValue(), TempMessages.ptopValueFormatting.get(comparator));
			return formatUser(user.get(), suffix);
		}

		Option<VillageTop> top = this.villageRankManager.getTop(comparator);
		if (top.isEmpty()) {
			return TempMessages.noValue;
		}
		Option<Village> village = top.get().getVillage(index);
		if (village.isEmpty()) {
			return TempMessages.noValue;
		}

		Number value = top.get().getComparator().getValue(village.get().getRank());
		String suffix = legacy
				? formatLegacyValue(value, TempMessages.gtopPoints.getValue())
				: formatTopValue(value, TempMessages.gtop.getValue(), TempMessages.gtopValueFormatting.get(comparator));
		return this.formatVillage(targetUser, village.get(), suffix);
	}

	private String resolvePosition(Matcher matcher, @Nullable User targetUser) {
		String type = matcher.group(1).toUpperCase(Locale.ROOT);
		String comparator = matcher.group(2).toLowerCase(Locale.ROOT);
		if (type.equals("POSITION")) {
			return targetUser == null ? "0" : Integer.toString(targetUser.getRank().getPosition(comparator));
		}

		if (targetUser == null || targetUser.getVillage().isEmpty()) {
			return TempMessages.noValue;
		}
		Village village = targetUser.getVillage().get();
		return this.villageRankManager.isRankedVillage(village)
				? Integer.toString(village.getRank().getPosition(comparator))
				: TempMessages.noValue;
	}

	private static String formatTopValue(Number value, String format, @Nullable List<RangeFormatting> ranges) {
		String rawValue = formatNumber(value);
		String formattedValue = ranges == null ? rawValue : NumberRange.inRangeToString(value, ranges);
		return format.replace("%VALUE-FORMAT%", formattedValue).replace("%VALUE%", rawValue);
	}

	private static String formatLegacyValue(Number value, String format) {
		String rawValue = formatNumber(value);
		String formattedValue = NumberRange.inRangeToString(value, TempMessages.pointsFormat);
		return format.replace("%POINTS-FORMAT%", formattedValue).replace("%POINTS%", rawValue);
	}

	private static String formatUser(User user, String suffix) {
		boolean online = user.isOnline() && !user.isVanished();
		RawString color = online ? TempMessages.online : TempMessages.offline;
		return color + user.getName() + suffix;
	}

	private String formatVillage(@Nullable User targetUser, Village village, String suffix) {
		String tag = village.getTag();
		if (this.tablistConfig.shouldUseRelationshipColors()) {
			Village viewerVillage = targetUser == null ? null : targetUser.getVillage().orNull();
			tag = TempMessages.relationalTag.chooseAndPrepareTag(viewerVillage, village);
		}
		return tag + suffix;
	}

	private static String replaceAll(Pattern pattern, String text, Function<Matcher, String> resolver) {
		Matcher matcher = pattern.matcher(text);
		if (!matcher.find()) {
			return text;
		}

		StringBuffer result = new StringBuffer(text.length());
		do {
			matcher.appendReplacement(result, Matcher.quoteReplacement(resolver.apply(matcher)));
		} while (matcher.find());
		matcher.appendTail(result);
		return result.toString();
	}

	private static String formatNumber(Number value) {
		return value instanceof Float || value instanceof Double
				? String.format(Locale.US, "%.2f", value.doubleValue())
				: value.toString();
	}

	public Replaceable prepareReplacement(User targetUser) {
		return new Replaceable() {
			@Override
			public @NotNull String replace(@Nullable Locale locale, @NotNull String text) {
				return RankPlaceholdersService.this.format(text, targetUser);
			}

			@Override
			public @NotNull Component replace(@Nullable Locale locale, @NotNull Component text) {
				TextReplacementConfig replacement = TextReplacementConfig.builder()
						.match(RANK_PLACEHOLDER)
						.replacement((result, input) -> AdventureUtils.formatComponent(
								RankPlaceholdersService.this.format(result.group(), targetUser)))
						.build();
				return text.replaceText(replacement);
			}
		};
	}
}
