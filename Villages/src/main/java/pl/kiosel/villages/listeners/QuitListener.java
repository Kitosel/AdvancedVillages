package pl.kiosel.villages.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.kiosel.villages.AdvancedVillages;

public class QuitListener implements Listener {

    private final AdvancedVillages plugin;

    public QuitListener(AdvancedVillages plugin) {
        this.plugin = plugin;
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
		plugin.getTablistManager().handleQuit(player);
		plugin.getTeleportManager().cleanupPlayer(player);

		if (plugin.getInviteManager().isPlayerInvited(player))
			plugin.getInviteManager().denyInvite(player);
	}
}
