package pl.kiosel.villages.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import pl.kiosel.core.utils.ColorUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.manager.VillageManager;
import pl.kiosel.villages.settings.Settings;

public class ChatListener implements Listener {

	private final AdvancedVillages plugin;

	public ChatListener(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if (Settings.CHAT_FORMAT_ENABLED.getBoolean()) {
			String messageFormat;
			if (plugin.getVillageManager().hasVillage(player)) {
				messageFormat = VillageManager.replaceWith(VillageManager.getVillageByOfflineOwner(player), Settings.CHAT_FORMAT_VILLAGE.getString())
						.replace("%message%", event.getMessage())
						.replace("%player%", player.getName());
			} else {
				messageFormat = ColorUtils.tl(Settings.CHAT_FORMAT_NO_VILLAGE.getString()
						.replace("%message%", event.getMessage())
						.replace("%player%", player.getName()));
			}
			if (plugin.isPlaceholder()) {
				event.setFormat(plugin.getPlaceholder().replacePlaceholder(player, messageFormat));
			} else {
				event.setFormat(messageFormat);
			}
		}
	}
}