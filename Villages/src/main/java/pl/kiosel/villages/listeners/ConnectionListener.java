package pl.kiosel.villages.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import pl.kiosel.common.Callback;
import pl.kiosel.villages.Wioski;

public class ConnectionListener implements Listener {

    private final Wioski plugin;

    public ConnectionListener(Wioski plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getUserManager().isUserInDatabase(player.getUniqueId(), new Callback<>(plugin) {

			@Override
			public void onResult(Boolean result) {
				if (result == false) {
					plugin.getUserManager().createUser(player);
				}
			}

			@Override
			public void onError(Throwable throwable) {
				plugin.getDebug().debug("Error while connecting to database", throwable);
			}
		});
		plugin.getScoreboardManager().createBoard(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
		plugin.getScoreboardManager().removeBoard(player);

		plugin.getTeleportManager().getCooldown().remove(player.getUniqueId());
		plugin.getTeleportManager().getTeleportCooldowns().remove(player);
		plugin.getTeleportManager().getTeleportingPlayers().remove(player);
		plugin.getTeleportManager().getTeleportTasks().remove(player);

		if (plugin.getTeleportManager().isTeleportTask(player))
			plugin.getTeleportManager().removeTeleportTask(player);

		if (plugin.getInviteManager().isPlayerInvited(player))
			plugin.getInviteManager().denyInvite(player);
    }
}