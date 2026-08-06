package pl.kiosel.villages.addons.tablist.services;

import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.tablist.services.service.BasicPlaceholdersService;
import pl.kiosel.villages.addons.tablist.placeholders.PlaceholdersService;
import pl.kiosel.villages.addons.tablist.services.service.PlayerPlaceholdersService;
import pl.kiosel.villages.addons.tablist.services.service.TimePlaceholdersService;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.placeholders.UserPlaceholdersService;
import pl.kiosel.villages.data.village.placeholders.VillagePlaceholdersService;

import java.time.OffsetDateTime;
import java.util.Locale;

public class TablistPlaceholdersService implements PlaceholdersService<Player> {

	private final AdvancedVillages plugin;
    private final BasicPlaceholdersService basicPlaceholdersService;
    private final TimePlaceholdersService timePlaceholdersService;
	private final PlayerPlaceholdersService playerPlaceholdersService;
	private final UserPlaceholdersService userPlaceholdersService;
	private final VillagePlaceholdersService villagePlaceholdersService;

    public TablistPlaceholdersService(AdvancedVillages plugin,
									  BasicPlaceholdersService basicPlaceholdersService,
									  TimePlaceholdersService timePlaceholdersService,
									  PlayerPlaceholdersService playerPlaceholdersService,
									  UserPlaceholdersService userPlaceholdersService,
									  VillagePlaceholdersService villagePlaceholdersService) {
		this.plugin = plugin;
        this.basicPlaceholdersService = basicPlaceholdersService;
        this.timePlaceholdersService = timePlaceholdersService;
		this.playerPlaceholdersService = playerPlaceholdersService;
		this.userPlaceholdersService = userPlaceholdersService;
		this.villagePlaceholdersService = villagePlaceholdersService;
    }

    @Override
    public String format(Object entity, String text, Player player) {
		User user = plugin.getUserManager().findByUuid(player.getUniqueId()).get();
        text = this.basicPlaceholdersService.format(entity, text, null);
		text = this.timePlaceholdersService.format(entity, text, OffsetDateTime.now());
		text = this.playerPlaceholdersService.format(entity, text, player);
        text = this.userPlaceholdersService.format(entity, text, user);
        text = this.villagePlaceholdersService.formatCustom(entity, text, user.getVillage().orNull(), "{G-", "}", name -> name.toUpperCase(Locale.ROOT));

        return text;
    }

    public String formatIdentifier(Object entity, String identifier, Player user) {
        return this.format(entity, "{" + identifier.toUpperCase(Locale.ROOT) + "}", user);
    }
}