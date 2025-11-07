package pl.kiosel.villages.addons.tablist.services;

import org.bukkit.entity.Player;
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
import pl.kiosel.villages.manager.VillageManager;
import pl.kiosel.villages.data.village.Village;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RankPlaceholdersService implements PlaceholdersService<Player> {

    private static final Pattern TOP_PATTERN = Pattern.compile("\\{(PTOP|GTOP)-([A-Za-z_]+)-([0-9]+)}");
    private static final Pattern TOP_POSITION_PATTERN = Pattern.compile("\\{(POSITION|G-POSITION)-([A-Za-z_]+)}");
    private static final Pattern LEGACY_TOP_PATTERN = Pattern.compile("\\{(PTOP|GTOP)-([0-9]+)}");

	public String gtopNoValue = "Brak (GTOP-x)";
	public String ptopNoValue = "Brak (PTOP-x)";
	public String gtop = " &7[{VALUE}&7]";
	public String ptop = " &7[{VALUE}&7]";

	public List<RangeFormatting> pointsFormat = Arrays.asList(
			new RangeFormatting(0, 749, "&4{POINTS}"),
			new RangeFormatting(750, 999, "&c{POINTS}"),
			new RangeFormatting(1000, 1499, "&a{POINTS}"),
			new RangeFormatting(1500, Integer.MAX_VALUE, "&6&l{POINTS}")
	);

	private final AdvancedVillages plugin;
//    private final PluginConfiguration config;
    private final TablistConfiguration tablistConfig;
//    private final MessageService messageService;
//    private final UserRankManager userRankManager;
//    private final GuildRankManager guildRankManager;

    public RankPlaceholdersService(
			AdvancedVillages plugin,
//            PluginConfiguration config,
            TablistConfiguration tablistConfig
//            MessageService messageService,
//            UserRankManager userRankManager,
//            GuildRankManager guildRankManager
    ) {
		this.plugin = plugin;
//        this.config = config;
        this.tablistConfig = tablistConfig;
//        this.messageService = messageService;
//        this.userRankManager = userRankManager;
//        this.guildRankManager = guildRankManager;
    }

    /**
     * Format top and top position placeholders in text.
     *
     * @param text       text to format
     * @param targetUser user for which text will be formatted
     * @return formatted text
     */
    @Override
    public String format(@Nullable Object entity, String text, Player targetUser) {
        if (entity == null) {
            entity = targetUser;
        }

        text = this.formatTop(text, targetUser);
        text = this.formatTopPosition(text, targetUser);

//        if (this.config.top.enableLegacyPlaceholders) {
            text = this.formatRank(text, targetUser);
//        }

        return text;
    }

    /**
     * Format top placeholders (PTOP/GTOP-type-x) in text.
     *
     * @param text       text to format
     * @param targetUser user for which text will be formatted
     * @return formatted text
     */
    public String formatTop(String text, @Nullable Player targetUser) {
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
			plugin.getDebug().debug(indexString + "is invalid " + topType + " index!");
            return text;
        }

        int index = indexOption.get();
        if (index < 1) {
			plugin.getDebug().debug("Index in " + topType + " must be greater or equal to 1!");
            return text;
        }

        if (topType.equalsIgnoreCase("PTOP")) {
            String placeholder = "{PTOP-" + comparatorType + "-" + index + "}";
            String noValue = ptopNoValue;

//            Option<UserTop> userTopOption = this.userRankManager.getTop(comparatorType);
//            if (userTopOption.isEmpty()) {
//                return Formater.format(text, placeholder, noValue);
//            }
//
//            UserTop userTop = userTopOption.get();
//
//            Option<Player> userOption = userTop.getUser(index);
//            if (userOption.isEmpty()) {
//                return Formater.format(text, placeholder, noValue);
//            }

//            Player user = userOption.get();
//            Number topValue = userTop.getComparator().getValue(user.getRank());
            String topFormat = ptop/*this.config.top.format.ptop.getValue()*/;

//            if (!topFormat.isEmpty()) {
//                List<RangeFormatting> formats = this.config.top.format.ptopValueFormatting.get(comparatorType.toLowerCase(Locale.ROOT));
//                topFormat = formatTopValue(topValue, topFormat, formats);
//            }

            return this.formatUserRank(text, placeholder, targetUser, topFormat);
        }

        if (topType.equalsIgnoreCase("GTOP")) {
            String placeholder = "{GTOP-" + comparatorType + "-" + index + "}";
            String noValue = gtopNoValue;

//            Option<GuildTop> guildTopOption = this.guildRankManager.getTop(comparatorType);
//            if (guildTopOption.isEmpty()) {
//                return Formater.format(text, placeholder, noValue);
//            }

//            GuildTop guildTop = guildTopOption.get();

//            Option<Village> guildOption = guildTop.getGuild(index);
//            if (guildOption.isEmpty()) {
//                return Formater.format(text, placeholder, noValue);
//            }

            Village guild = VillageManager.getVillageByOfflineOwner(targetUser)/*guildOption.get()*/;
//            Number topValue = guildTop.getComparator().getValue(guild.getRank());
            String topFormat = gtop/*this.config.top.format.gtop.getValue()*/;

//            if (!topFormat.isEmpty()) {
//                List<RangeFormatting> formats = this.config.top.format.gtopValueFormatting.get(comparatorType.toLowerCase(Locale.ROOT));
//                topFormat = formatTopValue(topValue, topFormat, formats);
//            }

            return this.formatVillageRank(text, placeholder, targetUser, guild, topFormat);
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
    public String formatTopPosition(String text, @Nullable Player targetUser) {
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

            int position = /*targetUser.getRank().getPosition(comparatorType)*/0;
            return Formater.format(text, "{POSITION-" + comparatorType + "}", position);
        }

        if (positionType.equalsIgnoreCase("G-POSITION")) {
            String minMembersToIncludeNoValue = "Brak (guild-min-members w config.yml)" /*this.messageService.get(targetUser, config -> config.minMembersToIncludeNoValue)*/;
            if (targetUser == null) {
                return Formater.format(text, "{G-POSITION}", minMembersToIncludeNoValue);
            }

            String placeholder = "{G-POSITION-" + comparatorType + "}";

//            Option<Village> guildOption = targetUser.getGuild();
//            if (guildOption.isEmpty()) {
//                return Formater.format(text, placeholder, minMembersToIncludeNoValue);
//            }

//            Village guild = guildOption.get();
//            if (!this.guildRankManager.isRankedGuild(guild)) {
//                return Formater.format(text, placeholder, minMembersToIncludeNoValue);
//            }

            return Formater.format(text, placeholder, ""/*guild.getRank().getPosition(comparatorType)*/);
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
    public String formatRank(String text, @Nullable Player targetUser) {
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
			plugin.getDebug().debug(indexString + "is invalid " + topType + " index!");
            return text;
        }

        int index = indexOption.get();
        if (index < 1) {
			plugin.getDebug().debug("Index in " + topType + " must be greater or equal to 1!");
            return text;
        }

        if (topType.equalsIgnoreCase("PTOP")) {
            String placeholder = "{PTOP-" + index + "}";
            String noValue = ptopNoValue;

//            Option<Player> userOption = this.userRankManager.getUser(DefaultTops.USER_POINTS_TOP, index);
//            if (userOption.isEmpty()) {
//                return Formater.format(text, placeholder, noValue);
//            }
//
//			Player user = userOption.get();
            int points = 0/*user.getRank().getPoints()*/;
            String pointsFormat = " &7[{POINTS}&7]"/*this.config.ptopPoints.getValue()*/;

            if (!pointsFormat.isEmpty()) {
				Formater formatter = new Formater()
                        .register("{POINTS-FORMAT}", NumberRange.inRangeToString(points, this.pointsFormat))
                        .register("{POINTS}", points);

                pointsFormat = formatter.format(pointsFormat);
            }

            return this.formatUserRank(text, placeholder, targetUser, pointsFormat);
        }

        if (topType.equalsIgnoreCase("GTOP")) {
            String placeholder = "{GTOP-" + index + "}";
            String noValue = gtopNoValue;

//            Option<Village> guildOption = this.guildRankManager.getGuild(DefaultTops.GUILD_AVG_POINTS_TOP, index);
//            if (guildOption.isEmpty()) {
//                return Formater.format(text, placeholder, noValue);
//            }

            Village guild = VillageManager.getVillageByOwner(targetUser);
            int points = 11; /*guild.getRank().getAveragePoints();*/
            String pointsFormat = " &7[&b{POINTS-FORMAT}&7]" /*this.config.gtopPoints.getValue()*/;

            if (!pointsFormat.isEmpty()) {
				Formater formatter = new Formater()
                        .register("{POINTS-FORMAT}", NumberRange.inRangeToString(points, this.pointsFormat))
                        .register("{POINTS}", points);

                pointsFormat = formatter.format(pointsFormat);
            }

            return this.formatVillageRank(text, placeholder, targetUser, guild, pointsFormat);
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

    private String formatUserRank(String text, String placeholder, Player user, String topFormat) {
        boolean online = user.isOnline();
//        if (online && this.config.ptopRespectVanish) {
//            online = !user.isVanished();
//        }

        RawString onlineColor = online ? new RawString("&a") : new RawString("&c");
        return Formater.format(text, placeholder, onlineColor + user.getName() + topFormat);
    }

    private String formatVillageRank(String text, String placeholder, @Nullable Player targetUser, Village village, String topFormat) {
        String prefix = "{TAG}";

//        if (this.tablistConfig.useRelationshipColors) {
//			Village viewerVillage = targetUser != null ? targetUser.getGuild().orNull() : null;
//            prefix = this.config.relationalTag.chooseAndPrepareTag(viewerVillage, village);
//        }

        String formattedPrefix = Formater.format(prefix, "{TAG}", village.getTag());
        return Formater.format(text, placeholder, formattedPrefix + topFormat);
    }

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([A-Za-z0-9-_)]+)}");

    public Replaceable prepareReplacement(Player targetUser) {
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
