package pl.kiosel.villages.addons.tablist.placeholders;

import org.bukkit.entity.Player;
import panda.std.Option;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.tablist.placeholders.resolver.MonoResolver;

public class PlayerPlaceholders extends Placeholders<Player, PlayerPlaceholders> {

	public PlayerPlaceholders playerProperty(String name, MonoResolver<Player> resolver) {
		return this.property(name, user -> resolver.resolve(user, AdvancedVillages.getInstance().getMetaServer().getPlayer(user.getUniqueId()).orNull()));
	}

	public PlayerPlaceholders playerOptionProperty(String name, MonoResolver<Option<Player>> resolver) {
		return this.playerProperty(name, player -> resolver.resolve(player, Option.of(player)));
	}

	@Override
	public PlayerPlaceholders create() {
		return new PlayerPlaceholders();
	}
}
