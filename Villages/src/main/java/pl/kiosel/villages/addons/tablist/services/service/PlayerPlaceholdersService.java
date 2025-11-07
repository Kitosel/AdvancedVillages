package pl.kiosel.villages.addons.tablist.services.service;

import com.google.common.base.Joiner;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import pl.kiosel.core.hooks.EconomyManager;
import pl.kiosel.core.hooks.WorldGuardHook;
import pl.kiosel.core.utils.format.Formater;
import pl.kiosel.villages.addons.tablist.placeholders.PlayerPlaceholders;
import pl.kiosel.villages.addons.tablist.placeholders.StaticPlaceholdersService;
import pl.kiosel.villages.addons.tablist.utils.NumberRange;
import pl.kiosel.villages.addons.tablist.utils.RangeFormatting;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PlayerPlaceholdersService extends StaticPlaceholdersService<Player, PlayerPlaceholders> {

	private static final List<RangeFormatting> pingFormat = Arrays.asList(
			new RangeFormatting(0, 75, "&a{PING}"),
			new RangeFormatting(76, 150, "&e{PING}"),
			new RangeFormatting(151, 300, "&c{PING}"),
			new RangeFormatting(301, Integer.MAX_VALUE, "&c{PING}")
	);

	public static PlayerPlaceholders createPlayerPlaceholders() {
		return new PlayerPlaceholders()
				.property("name", Player::getName)
				.property("player", Player::getName)
				.property("ping", Player::getPing)
				.property("health", Player::getHealth)
				.property("address", Player::getAddress)
				.property("level", Player::getLevel)
				.property("exp", Player::getExp)
				.property("displayname", Player::getDisplayName)
				.property("ping-format", user -> Formater.format(NumberRange.inRangeToString(user.getPing(), pingFormat), "{PING}", user.getPing()));
	}

	public static PlayerPlaceholders createPlayerWorldPlaceholders() {
		String wgRegionNoValue = "Brak (WG-REGION)";
		return new PlayerPlaceholders()
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