package pl.kiosel.villages.addons.tablist.services;

import org.bukkit.entity.Player;
import pl.kiosel.villages.addons.tablist.services.service.BasicPlaceholdersService;
import pl.kiosel.villages.addons.tablist.placeholders.PlaceholdersService;
import pl.kiosel.villages.addons.tablist.services.service.PlayerPlaceholdersService;
import pl.kiosel.villages.addons.tablist.services.service.TimePlaceholdersService;

import java.time.OffsetDateTime;
import java.util.Locale;

public class TablistPlaceholdersService implements PlaceholdersService<Player> {

    private final BasicPlaceholdersService basicPlaceholdersService;
    private final TimePlaceholdersService timePlaceholdersService;
	private final PlayerPlaceholdersService playerPlaceholdersService;

    public TablistPlaceholdersService(BasicPlaceholdersService basicPlaceholdersService,
									  TimePlaceholdersService timePlaceholdersService,
									  PlayerPlaceholdersService playerPlaceholdersService) {
        this.basicPlaceholdersService = basicPlaceholdersService;
        this.timePlaceholdersService = timePlaceholdersService;
		this.playerPlaceholdersService = playerPlaceholdersService;
    }

    @Override
    public String format(Object entity, String text, Player user) {
        text = this.basicPlaceholdersService.format(entity, text, null);
		text = this.timePlaceholdersService.format(entity, text, OffsetDateTime.now());
		text = this.playerPlaceholdersService.format(entity, text, user);
//        text = this.userPlaceholdersService.format(entity, text, user);
//        text = this.guildPlaceholdersService.formatCustom(entity, text, user.getGuild().orNull(), "{G-", "}", name -> name.toUpperCase(Locale.ROOT));

        return text;
    }

    public String formatIdentifier(Object entity, String identifier, Player user) {
        return this.format(entity, "{" + identifier.toUpperCase(Locale.ROOT) + "}", user);
    }
}