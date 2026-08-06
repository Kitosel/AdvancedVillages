package pl.kiosel.villages.data.user.placeholders;

import org.bukkit.entity.Player;
import panda.std.Option;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.tablist.placeholders.Placeholders;
import pl.kiosel.villages.addons.tablist.placeholders.placeholder.FallbackPlaceholder;
import pl.kiosel.villages.addons.tablist.placeholders.resolver.LocaleMonoResolver;
import pl.kiosel.villages.addons.tablist.placeholders.resolver.LocalePairResolver;
import pl.kiosel.villages.addons.tablist.placeholders.resolver.LocaleSimpleResolver;
import pl.kiosel.villages.addons.tablist.placeholders.resolver.MonoResolver;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.UserRank;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageRank;
import pl.kiosel.villages.data.village.placeholders.VillagePlaceholders;

import java.util.Locale;
import java.util.Objects;

public class UserPlaceholders extends Placeholders<User, UserPlaceholders> {

    public UserPlaceholders playerProperty(String name, MonoResolver<Player> resolver) {
        return this.property(name, user -> resolver.resolve(user, AdvancedVillages.getInstance().getUserManager().getPlayer(user).orNull()));
    }

    public UserPlaceholders playerOptionProperty(String name, MonoResolver<Option<Player>> resolver) {
        return this.playerProperty(name, player -> resolver.resolve(player, Option.of(player)));
    }
	public UserPlaceholders rankProperty(String name, MonoResolver<UserRank> resolver) {
        return this.property(name, (Object entity, User user) -> {
            Object value = resolver.resolve(entity, user.getRank());
            if (value instanceof Float || value instanceof Double) {
                return String.format(Locale.US, "%.2f", ((Number) value).floatValue());
            }
            return Objects.toString(value);
        });
    }

    @Override
    public UserPlaceholders create() {
        return new UserPlaceholders();
    }

}
