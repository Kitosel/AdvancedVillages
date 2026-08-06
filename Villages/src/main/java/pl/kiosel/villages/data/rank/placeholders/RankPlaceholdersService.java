package pl.kiosel.villages.data.rank.placeholders;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import panda.std.Option;
import pl.kiosel.core.chat.AdventureUtils;
import pl.kiosel.core.dependencies.net.kyori.adventure.text.Component;
import pl.kiosel.core.dependencies.net.kyori.adventure.text.TextReplacementConfig;
import pl.kiosel.core.utils.format.Formater;
import pl.kiosel.core.utils.format.RawString;
import pl.kiosel.core.utils.format.Replaceable;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.tablist.placeholders.PlaceholdersService;
import pl.kiosel.villages.addons.tablist.utils.NumberRange;
import pl.kiosel.villages.addons.tablist.utils.RangeFormatting;
import pl.kiosel.villages.config.TablistConfiguration;
import pl.kiosel.villages.config.TempMessages;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageRankManager;
import pl.kiosel.villages.data.village.top.VillageTop;
import pl.kiosel.villages.data.rank.DefaultTops;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.UserRankManager;
import pl.kiosel.villages.data.user.top.UserTop;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RankPlaceholdersService implements PlaceholdersService<User> {

    private static final Pattern TOP_PATTERN = Pattern.compile("\\{(PTOP|GTOP)-([A-Za-z_]+)-([0-9]+)}");
    private static final Pattern TOP_POSITION_PATTERN = Pattern.compile("\\{(POSITION|G-POSITION)-([A-Za-z_]+)}");
    private static final Pattern LEGACY_TOP_PATTERN = Pattern.compile("\\{(PTOP|GTOP)-([0-9]+)}");

    private final TablistConfiguration tablistConfig;
    private final UserRankManager userRankManager;
    private final VillageRankManager villageRankManager;

    public RankPlaceholdersService(
            TablistConfiguration tablistConfig,
            UserRankManager userRankManager,
            VillageRankManager villageRankManager
    ) {
        this.tablistConfig = tablistConfig;
        this.userRankManager = userRankManager;
        this.villageRankManager = villageRankManager;
    }

    /**
     * Format top and top position placeholders in text.
     *
     * @param text       text to format
     * @param targetUser user for which text will be formatted
     * @return formatted text
     */
    @Override
    public String format(@Nullable Object entity, String text, User targetUser) {
        if (entity == null) {
            entity = targetUser;
        }

        text = this.formatTop(text, targetUser);
        text = this.formatTopPosition(text, targetUser);

        if (true) {
            text = this.formatRank(text, targetUser);
        }

        return text;
    }

    /**
     * Format top placeholders (PTOP/GTOP-type-x) in text.
     *
     * @param text       text to format
     * @param targetUser user for which text will be formatted
     * @return formatted text
     */
    public String formatTop(String text, @Nullable User targetUser) {
        if (text == null) {
            return "";
        }

        if (!text.contains("TOP-")) {
            return text;
        }

        Matcher matcher = TOP_PATTERN.matcher(text);
        if (!matcher.find()) {
            return text;
        }

        String topType = matcher.group(1);
        String comparatorType = matcher.group(2);
        String indexString = matcher.group(3);

        Option<Integer> indexOption = Option.attempt(NumberFormatException.class, () -> Integer.parseInt(indexString));
        if (indexOption.isEmpty()) {
			AdvancedVillages.getInstance().getDebug().error(indexString + "is invalid " + topType + " index!");
            return text;
        }

        int index = indexOption.get();
        if (index < 1) {
			AdvancedVillages.getInstance().getDebug().error("Index in " + topType + " must be greater or equal to 1!");
            return text;
        }

        if (topType.equalsIgnoreCase("PTOP")) {
            String placeholder = "{PTOP-" + comparatorType + "-" + index + "}";
            String noValue = TempMessages.noValue;

            Option<UserTop> userTopOption = this.userRankManager.getTop(comparatorType);
            if (userTopOption.isEmpty()) {
                return Formater.format(text, placeholder, noValue);
            }

            UserTop userTop = userTopOption.get();

            Option<User> userOption = userTop.getUser(index);
            if (userOption.isEmpty()) {
                return Formater.format(text, placeholder, noValue);
            }

            User user = userOption.get();
            Number topValue = userTop.getComparator().getValue(user.getRank());
            String topFormat = TempMessages.ptop.getValue();

            if (!topFormat.isEmpty()) {
                List<RangeFormatting> formats = TempMessages.ptopValueFormatting.get(comparatorType.toLowerCase(Locale.ROOT));
                topFormat = formatTopValue(topValue, topFormat, formats);
            }

            return this.formatUserRank(text, placeholder, user, topFormat);
        }

        if (topType.equalsIgnoreCase("GTOP")) {
            String placeholder = "{GTOP-" + comparatorType + "-" + index + "}";
            String noValue = TempMessages.noValue;

            Option<VillageTop> villageTopOption = this.villageRankManager.getTop(comparatorType);
            if (villageTopOption.isEmpty()) {
                return Formater.format(text, placeholder, noValue);
            }

			VillageTop villageTop = villageTopOption.get();

            Option<Village> villageOption = villageTop.getVillage(index);
            if (villageOption.isEmpty()) {
                return Formater.format(text, placeholder, noValue);
            }

            Village village = villageOption.get();
            Number topValue = villageTop.getComparator().getValue(village.getRank());
            String topFormat = TempMessages.gtop.getValue();

            if (!topFormat.isEmpty()) {
                List<RangeFormatting> formats = TempMessages.gtopValueFormatting.get(comparatorType.toLowerCase(Locale.ROOT));
                topFormat = formatTopValue(topValue, topFormat, formats);
            }

            return this.formatVillageRank(text, placeholder, targetUser, village, topFormat);
        }

        return text;
    }

    /**
     * Format top position placeholders (POSITION/G-POSITION-type) in text.
     *
     * @param text       text to format
     * @param targetUser user for which text will be formatted
     * @return formatted text
     */
    public String formatTopPosition(String text, @Nullable User targetUser) {
        if (text == null) {
            return "";
        }

        if (!text.contains("POSITION-")) {
            return text;
        }

        Matcher matcher = TOP_POSITION_PATTERN.matcher(text);
        if (!matcher.find()) {
            return text;
        }

        String positionType = matcher.group(1);
        String comparatorType = matcher.group(2);

        if (positionType.equalsIgnoreCase("POSITION")) {
            if (targetUser == null) {
                return Formater.format(text, "{POSITION}", 0);
            }

            int position = targetUser.getRank().getPosition(comparatorType);
            return Formater.format(text, "{POSITION-" + comparatorType + "}", position);
        }

        if (positionType.equalsIgnoreCase("G-POSITION")) {
            String minMembersToIncludeNoValue = TempMessages.noValue;
            if (targetUser == null) {
                return Formater.format(text, "{G-POSITION}", minMembersToIncludeNoValue);
            }

            String placeholder = "{G-POSITION-" + comparatorType + "}";

            Option<Village> villageOption = targetUser.getVillage();
            if (villageOption.isEmpty()) {
                return Formater.format(text, placeholder, minMembersToIncludeNoValue);
            }

            Village village = villageOption.get();
            if (!this.villageRankManager.isRankedVillage(village)) {
                return Formater.format(text, placeholder, minMembersToIncludeNoValue);
            }

            return Formater.format(text, placeholder, village.getRank().getPosition(comparatorType));
        }

        return text;
    }

    // TODO Migrate all {PTOP/GTOP-x} placeholders to new {PTOP/GTOP-type-x} and remove this method

    /**
     * Format legacy top placeholders (PTOP/GTOP-x) in text
     *
     * @param text       text to format
     * @param targetUser user for which text will be formatted
     * @return formatted text
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "4.11.0")
    public String formatRank(String text, @Nullable User targetUser) {
        if (text == null) {
            return "";
        }

        if (!text.contains("TOP-")) {
            return text;
        }

        Matcher matcher = LEGACY_TOP_PATTERN.matcher(text);
        if (!matcher.find()) {
            return text;
        }

        String topType = matcher.group(1);
        String indexString = matcher.group(2);

        Option<Integer> indexOption = Option.attempt(NumberFormatException.class, () -> Integer.parseInt(indexString));
        if (indexOption.isEmpty()) {
			AdvancedVillages.getInstance().getDebug().error(indexString + "is invalid " + topType + " index!");
            return text;
        }

        int index = indexOption.get();
        if (index < 1) {
			AdvancedVillages.getInstance().getDebug().error("Index in " + topType + " must be greater or equal to 1!");
            return text;
        }

        if (topType.equalsIgnoreCase("PTOP")) {
            String placeholder = "{PTOP-" + index + "}";
            String noValue = TempMessages.noValue;

            Option<User> userOption = this.userRankManager.getUser(DefaultTops.USER_POINTS_TOP, index);
            if (userOption.isEmpty()) {
                return Formater.format(text, placeholder, noValue);
            }

            User user = userOption.get();
            int points = user.getRank().getPoints();
            String pointsFormat = TempMessages.ptopPoints.getValue();

            if (!pointsFormat.isEmpty()) {
				Formater formatter = new Formater()
                        .register("{POINTS-FORMAT}", NumberRange.inRangeToString(points, TempMessages.pointsFormat))
                        .register("{POINTS}", points);

                pointsFormat = formatter.format(pointsFormat);
            }

            return this.formatUserRank(text, placeholder, user, pointsFormat);
        }

        if (topType.equalsIgnoreCase("GTOP")) {
            String placeholder = "{GTOP-" + index + "}";
            String noValue = TempMessages.noValue;

            Option<Village> villageOption = this.villageRankManager.getVillage(DefaultTops.VILLAGE_AVG_POINTS_TOP, index);
            if (villageOption.isEmpty()) {
                return Formater.format(text, placeholder, noValue);
            }

            Village village = villageOption.get();
            int points = village.getRank().getAveragePoints();
            String pointsFormat = TempMessages.gtopPoints.getValue();

            if (!pointsFormat.isEmpty()) {
				Formater formatter = new Formater()
                        .register("{POINTS-FORMAT}", NumberRange.inRangeToString(points, TempMessages.pointsFormat))
                        .register("{POINTS}", points);

                pointsFormat = formatter.format(pointsFormat);
            }

            return this.formatVillageRank(text, placeholder, targetUser, village, pointsFormat);
        }

        return text;
    }

    private static String formatTopValue(Number topValue, String topFormat, @Nullable List<RangeFormatting> formats) {
        String valueString = topValue instanceof Float || topValue instanceof Double
                ? String.format(Locale.US, "%.2f", topValue.floatValue())
                : topValue.toString();
        String valueFormat = formats == null
                ? valueString
                : NumberRange.inRangeToString(topValue, formats);

		Formater formatter = new Formater()
                .register("{VALUE-FORMAT}", valueFormat)
                .register("{VALUE}", valueString);

        return formatter.format(topFormat);
    }

    private String formatUserRank(String text, String placeholder, User user, String topFormat) {
        boolean online = user.isOnline();
        if (online && true) {
            online = !user.isVanished();
        }

        RawString onlineColor = online ? TempMessages.ptopOnline : TempMessages.ptopOffline;
        return Formater.format(text, placeholder, onlineColor + user.getName() + topFormat);
    }

    private String formatVillageRank(String text, String placeholder, @Nullable User targetUser, Village village, String topFormat) {
        String prefix = "{TAG}";

        if (this.tablistConfig.useRelationshipColors) {
            Village viewerVillage = targetUser != null ? targetUser.getVillage().orNull() : null;
            prefix = TempMessages.relationalTag.chooseAndPrepareTag(viewerVillage, village);
        }

        String formattedPrefix = Formater.format(prefix, "{TAG}", village.getTag());
        return Formater.format(text, placeholder, formattedPrefix + topFormat);
    }

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([A-Za-z0-9-_)]+)}");

    public Replaceable prepareReplacement(User targetUser) {
        return new Replaceable() {
            @Override
            public @NotNull String replace(@Nullable Locale locale, @NotNull String text) {
                return RankPlaceholdersService.this.format(targetUser, text, targetUser);
            }

            @Override
            public @NotNull Component replace(@Nullable Locale locale, @NotNull Component text) {
                TextReplacementConfig topReplacement = TextReplacementConfig.builder()
                        .match(PLACEHOLDER_PATTERN)
                        .replacement(((result, input) -> {
                            String replacement = RankPlaceholdersService.this.format(targetUser, result.group(), targetUser);
                            return AdventureUtils.formatComponent(replacement);
                        }))
                        .build();
                return text.replaceText(topReplacement);
            }
        };
    }

}
