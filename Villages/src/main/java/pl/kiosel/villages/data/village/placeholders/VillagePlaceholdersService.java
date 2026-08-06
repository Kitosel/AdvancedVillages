package pl.kiosel.villages.data.village.placeholders;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;
import panda.std.Option;
import panda.std.Pair;
import panda.utilities.StringUtils;
import pl.kiosel.core.utils.TextUtils;
import pl.kiosel.core.utils.format.Formater;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.tablist.placeholders.BasicPlaceholders;
import pl.kiosel.villages.addons.tablist.placeholders.StaticPlaceholdersService;
import pl.kiosel.villages.addons.tablist.services.service.BasicPlaceholdersService;
import pl.kiosel.villages.addons.tablist.utils.NumberRange;
import pl.kiosel.villages.config.TempMessages;
import pl.kiosel.villages.data.village.*;
import pl.kiosel.villages.data.rank.DefaultTops;
import pl.kiosel.villages.data.user.UserUtils;

import java.util.LinkedHashSet;
import java.util.Set;

public class VillagePlaceholdersService extends StaticPlaceholdersService<Village, VillagePlaceholders> {

    public static final BasicPlaceholders<Pair<String, Village>> VILLAGE_MEMBERS_COLOR_CONTEXT = new BasicPlaceholders<Pair<String, Village>>()
            .property("members", pair -> {
                String text = JOIN_OR_DEFAULT.apply(UserUtils.getOnlineNames(pair.getSecond().getMembers()), "");

                return !text.contains("<online>")
                        ? text
                        : BasicPlaceholdersService.ONLINE.toFormatter(pair.getFirst()).format(text);
            });

    private static Option<VillagePlaceholders> SIMPLE = Option.none();

    @Override
    public String format(@Nullable Object entity, String text, Village village) {
        text = super.format(entity, text, village);
        text = VILLAGE_MEMBERS_COLOR_CONTEXT.toVariablesFormatter(Pair.of(TextUtils.getLastColorBefore(text, "{MEMBERS}"), village))
                .format(text);
        return text;
    }

    public static Option<VillagePlaceholders> getSimplePlaceholders() {
        return SIMPLE;
    }

    public static VillagePlaceholders createSimplePlaceholders() {
        VillagePlaceholders placeholders = new VillagePlaceholders()
                .property("name", Village::getName, entity -> TempMessages.noValue)
                .property("village", Village::getName, entity -> TempMessages.noValue)
				.property("tag", Village::getTag, entity -> TempMessages.noValue)
				.property("lives", Village::getLives, entity -> TempMessages.noValue)
				.property("level", Village::getLevel, entity -> TempMessages.noValue);
        SIMPLE = Option.of(placeholders);

        return placeholders;
    }

    public static VillagePlaceholders createVillagePlaceholders(AdvancedVillages plugin) {
        VillageRankManager rankManager = plugin.getVillageRankManager();

        return new VillagePlaceholders()
                .property("owner", village -> village.getOwner().getName(), entity -> TempMessages.noValue)
                .property("members-online", village -> village.getOnlineMembers().size(), entity -> 0)
                .property("members-all", village -> village.getMembers().size(), entity -> 0)
                .property("region-size",
                        (entity, village) -> village.getRegion()
                                .map(Region::getSize)
                                .map(value -> Integer.toString(value))
                                .orElseGet(TempMessages.noValue),
                        entity -> TempMessages.noValue)
                .property("pvp",
                        (entity, village) -> village.hasPvPEnabled()
                                ? TempMessages.pvpStatusOn
                                : TempMessages.pvpStatusOff,
                        entity -> TempMessages.pvpStatusOff)
                .timeProperty("protection", Village::getProtection, TempMessages.noValue)
                .property("lives", Village::getLives, entity -> 0)
                .property("lives-symbol",
                        village -> {
                            int lives = village.getLives();
                            if (lives <= TempMessages.warLives) {
                                return StringUtils.repeated(lives, TempMessages.full.getValue()) +
                                        StringUtils.repeated(TempMessages.warLives - lives, TempMessages.empty.getValue());
                            } else {
                                return StringUtils.repeated(TempMessages.warLives, TempMessages.full.getValue()) + TempMessages.more.getValue();
                            }
                        }, entity -> TempMessages.noValue)
                .property("lives-symbol-all",
						village -> StringUtils.repeated(village.getLives(), TempMessages.full.getValue()),
                        entity -> TempMessages.noValue)
                .rankProperty("position",
                        (entity, village, rank) -> rankManager.isRankedVillage(village)
                                ? String.valueOf(rank.getPosition(DefaultTops.VILLAGE_AVG_POINTS_TOP))
                                : TempMessages.noValue,
                        entity -> TempMessages.noValue)
                .rankProperty("rank",
                        (entity, village, rank) -> rankManager.isRankedVillage(village)
                                ? String.valueOf(rank.getPosition(DefaultTops.VILLAGE_AVG_POINTS_TOP))
                                : TempMessages.noValue,
                        entity -> TempMessages.noValue)
                .rankProperty("points", VillageRank::getPoints, 0)
                .rankProperty("avg-points", VillageRank::getAveragePoints,0)
                .rankProperty("points", VillageRank::getAveragePoints, 0)
                .rankProperty("points-format",
                        (entity, village, rank) -> Formater.format(NumberRange.inRangeToString(rank.getAveragePoints(),
								TempMessages.pointsFormat), "{POINTS}", village.getRank().getAveragePoints()),
                        entity -> Formater.format(NumberRange.inRangeToString(0, TempMessages.pointsFormat), "{POINTS}", 0))
                .rankProperty("kills", VillageRank::getKills, 0)
                .rankProperty("avg-kills", VillageRank::getAverageKills, 0)
                .rankProperty("deaths", VillageRank::getDeaths, 0)
                .rankProperty("avg-deaths", VillageRank::getAverageDeaths, 0)
                .rankProperty("assists", VillageRank::getAssists, 0)
                .rankProperty("avg-assists", VillageRank::getAverageAssists, 0)
                .rankProperty("logouts", VillageRank::getLogouts, 0)
                .rankProperty("avg-logouts", VillageRank::getAverageLogouts, 0)
                .rankProperty("kdr", VillageRank::getKDR, 0.00)
                .rankProperty("avg-kdr", VillageRank::getAverageKDR, 0.00)
                .rankProperty("kda", VillageRank::getKDA, 0.00)
                .rankProperty("avg-kda", VillageRank::getAverageKDA, 0.00);
    }

    @Override
    public Set<Formater> prepareReplacements(@Nullable Object entity, Village data) {
        Set<Formater> formatters = new LinkedHashSet<>(super.prepareReplacements(entity, data));
        formatters.add(VILLAGE_MEMBERS_COLOR_CONTEXT.toVariablesFormatter(Pair.of(ChatColor.RESET.toString(), data)));
        return formatters;
    }

}
