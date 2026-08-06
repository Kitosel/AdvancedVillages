package pl.kiosel.villages.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import pl.kiosel.core.database.Callback;
import pl.kiosel.core.nms.Nms;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.tablist.IndividualPlayerList;
import pl.kiosel.villages.data.user.*;
import pl.kiosel.villages.settings.Settings;

public class QuitListener implements Listener {

    private final AdvancedVillages plugin;
	private final UserManager userManager;

    public QuitListener(AdvancedVillages plugin) {
        this.plugin = plugin;
		this.userManager = plugin.getUserManager();
    }

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onKick(PlayerKickEvent event) {
		this.handleQuit(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent event) {
		this.handleQuit(event.getPlayer());
	}

	private void handleQuit(Player player) {
		plugin.getScoreboardManager().removeBoard(player);

		plugin.getTeleportManager().getCooldown().remove(player.getUniqueId());
		plugin.getTeleportManager().getTeleportCooldowns().remove(player);
		plugin.getTeleportManager().getTeleportingPlayers().remove(player);
		plugin.getTeleportManager().getTeleportTasks().remove(player);

		if (plugin.getTeleportManager().isTeleportTask(player))
			plugin.getTeleportManager().removeTeleportTask(player);

		if (plugin.getInviteManager().isPlayerInvited(player))
			plugin.getInviteManager().denyInvite(player);

		this.userManager.findByUuid(player.getUniqueId()).peek(user -> {
			UserCache cache = user.getCache();
//			DamageState damageState = damageManager.getDamageState(user.getUUID());
//
//			if (damageState.isInCombat()) {
//				LogoutsChangeEvent logoutsChangeEvent = new LogoutsChangeEvent(FunnyEvent.EventCause.USER, user, user, 1);
//
//				if (SimpleEventHandler.handle(logoutsChangeEvent)) {
//					user.getRank().updateLogouts(currentValue -> currentValue + logoutsChangeEvent.getLogoutsChange());
//				}
//			}

			cache.setPlayerList(null);
//			damageState.clear();
		});
	}
}