package pl.kiosel.villages.addons.tablist.services.service;

import org.bukkit.entity.Player;
import pl.kiosel.core.utils.format.Formater;
import pl.kiosel.villages.addons.tablist.placeholders.PlayerPlaceholders;
import pl.kiosel.villages.addons.tablist.placeholders.StaticPlaceholdersService;
import pl.kiosel.villages.addons.tablist.utils.NumberRange;
import pl.kiosel.villages.config.TempMessages;

public class PlayerPlaceholdersService extends StaticPlaceholdersService<Player, PlayerPlaceholders> {

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
				.property("ping-format", user -> Formater.format(NumberRange.inRangeToString(user.getPing(), TempMessages.pingFormat), "{PING}", user.getPing()));
	}
}