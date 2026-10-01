package pl.kiosel.villages.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.kiosel.rosacore.compatibility.ZSound;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.firststeps.TutorialGUI;
import pl.kiosel.villages.api.events.VillageListener;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.config.Settings;
import pl.kiosel.villages.config.gui.GUIS;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.UserManager;

public class JoinListener extends VillageListener {

    private final AdvancedVillages plugin;
	private final UserManager userManager;

    public JoinListener(AdvancedVillages plugin) {
		super(plugin);
        this.plugin = plugin;
		this.userManager = plugin.getUserManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		User user = this.userManager.getOrCreate(player);
		this.plugin.getScoreboardManager().createBoard(player);
		this.plugin.getTablistManager().handleJoin(player, user);

		if (Settings.UPDATE_CHECKER.getBoolean()) {
			plugin.getRosaScheduler().runForEntityLater(player, () -> {
				String newVersion = plugin.getNewPluginVersion();
				if (newVersion == null || plugin.getDescription().getVersion().equals(newVersion)) return;

				if (!player.isOp() || !player.hasPermission("advancedvillages.admin")) return;

				ZSound.ENTITY_PLAYER_LEVELUP.play(player, 0.5f, 2f);
				getMessage().message(Lang.UPDATE).with("version", newVersion).sendPrefixed(player);
			}, () -> {
			}, 20 * 4);
		}

		if (!plugin.isDev()) return;
		if (!Settings.FIRST_STEPS.getBoolean() && (player.isOp() || player.hasPermission("advancedvillages.command.settings"))) {
			plugin.getMessenger().animatedTitle(
					player,
					plugin.getGuiSettings().text(GUIS.TUTORIAL,"welcome.title",
							"&aThanks for using &f&lADVANCED&6&lVILLAGES"),
					plugin.getGuiSettings().text(GUIS.TUTORIAL,"welcome.subtitle",
							"&7Personalize your experience"),
					1
			);
			plugin.getRosaScheduler().runForEntityLater(player,
					() -> plugin.getGuiManager().openGUI(player, new TutorialGUI(plugin, true)),
					() -> {}, 4 * 20L);
		}
	}
}
