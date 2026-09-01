package pl.kiosel.villages.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import pl.kiosel.rosacore.listener.RosaListener;
import pl.kiosel.rosacore.utils.ColorUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.Settings;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.manager.VillageUtils;

public class ChatListener extends RosaListener {

	private final AdvancedVillages plugin;

	public ChatListener(AdvancedVillages plugin) {
		super(plugin);
		this.plugin = plugin;
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		if (!Settings.CHAT_FORMAT_ENABLED.getBoolean())
			return;

		Player player = event.getPlayer();
		User user = plugin.getUserManager().findByUuid(player.getUniqueId()).orElseThrow();

		String messageFormat;
		if (user.hasVillage()) {
			messageFormat = VillageUtils.replaceWith(player, user.getPresentVillage(), Settings.CHAT_FORMAT_VILLAGE.getString()).toText();
		} else {
			messageFormat = VillageUtils.replacePlayer(player, Settings.CHAT_FORMAT_NO_VILLAGE.getString()).toText();
		}
		if (plugin.isPlaceholder()) {
			messageFormat = plugin.getPlaceholder().replacePlaceholder(player, messageFormat);
		}
		event.setFormat(ColorUtils.color(messageFormat.replace("%message%", event.getMessage())));
	}
}