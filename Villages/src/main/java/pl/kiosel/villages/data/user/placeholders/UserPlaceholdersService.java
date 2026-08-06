package pl.kiosel.villages.data.user.placeholders;

import com.google.common.base.Joiner;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import pl.kiosel.core.hooks.EconomyManager;
import pl.kiosel.core.hooks.WorldGuardHook;
import pl.kiosel.core.utils.format.Formater;
import pl.kiosel.villages.addons.tablist.placeholders.StaticPlaceholdersService;
import pl.kiosel.villages.addons.tablist.utils.NumberRange;
import pl.kiosel.villages.config.TempMessages;
import pl.kiosel.villages.data.rank.DefaultTops;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.UserRank;
import pl.kiosel.villages.data.user.UserUtils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class UserPlaceholdersService extends StaticPlaceholdersService<User, UserPlaceholders> {

    public static UserPlaceholders createUserPlaceholders() {
        return new UserPlaceholders()
                .property("ping-format", user -> Formater.format(NumberRange.inRangeToString(user.getPing(),
						TempMessages.pingFormat), "{PING}", user.getPing()))
                .property("has-village", User::hasVillage)
                .property("village-position", UserUtils::getUserPosition)
                .rankProperty("position", (rank) -> rank.getPosition(DefaultTops.USER_POINTS_TOP))
                .rankProperty("points", UserRank::getPoints)
                .rankProperty("points-format", (UserRank rank) -> Formater.format(NumberRange.inRangeToString(rank.getPoints(),
						TempMessages.pointsFormat), "{POINTS}", rank.getPoints()))
                .rankProperty("kills", UserRank::getKills)
                .rankProperty("deaths", UserRank::getDeaths)
                .rankProperty("kdr", UserRank::getKDR)
                .rankProperty("kda", UserRank::getKDA)
                .rankProperty("assists", UserRank::getAssists)
                .rankProperty("logouts", UserRank::getLogouts);
    }

    public static UserPlaceholders createPlayerPlaceholders() {
        String wgRegionNoValue = TempMessages.noValue;
        return new UserPlaceholders()
                .playerOptionProperty("world", playerOption -> playerOption
                        .map(Player::getWorld)
                        .map(World::getName)
                        .orElseGet(""))
                .playerOptionProperty("online", playerOption -> playerOption
                        .map(player -> Bukkit.getOnlinePlayers().stream().filter(player::canSee).count())
                        .orElseGet(0L))
                .playerProperty("wg-region", player -> {
                    List<String> regionNames = getWorldGuardRegionNames(player);
                    return !regionNames.isEmpty() ? regionNames.get(0) : wgRegionNoValue;
                })
                .playerProperty("wg-regions", player -> {
                    List<String> regionNames = getWorldGuardRegionNames(player);
                    return !regionNames.isEmpty() ? Joiner.on(", ").join(regionNames) : wgRegionNoValue;
                })
                .playerOptionProperty("vault-money", playerOption -> playerOption
                        .filter(player -> EconomyManager.isEnabled())
                        .map(EconomyManager::getBalance)
                        .map(value -> String.format(Locale.US, "%.2f", value))
                        .orElseGet(""));
    }

	private static List<String> getWorldGuardRegionNames(Player player) {
		if (player == null) {
			return Collections.emptyList();
		}

		Location location = player.getLocation();
		List<String> regionNames = WorldGuardHook.isEnabled() ? WorldGuardHook.getRegionNames(location) : null;

		if (regionNames != null && !regionNames.isEmpty()) {
			return regionNames;
		}

		return Collections.emptyList();
	}

}
