package pl.kiosel.villages.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import pl.kiosel.core.database.Callback;
import pl.kiosel.core.nms.Nms;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.tablist.IndividualPlayerList;
import pl.kiosel.villages.settings.Settings;

public class ConnectionListener implements Listener {

    private final AdvancedVillages plugin;

    public ConnectionListener(AdvancedVillages plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getDatabaseUserManager().isUserInDatabase(player.getUniqueId(), new Callback<>(plugin) {

			@Override
			public void onResult(Boolean result) {
				if (result == false) {
					plugin.getDataHelper().createUserPlayer(player.getName(), player.getUniqueId());
				}
			}
			@Override
			public void onError(Throwable throwable) {
				plugin.getDebug().debug("Error while connecting to database", throwable);
			}
		});
		plugin.getScoreboardManager().createBoard(player);

		if (Settings.ADDONS_TABLIST_ENABLE.getBoolean()) {
			IndividualPlayerList individualPlayerList = new IndividualPlayerList(
					plugin, player,
					Nms.getImplementations().getPlayerListAccessor(),
					plugin.getMetaServer(),
					this.plugin.getTablistConfig().cells,
					this.plugin.getTablistConfig().header,
					this.plugin.getTablistConfig().footer,
					this.plugin.getTablistConfig().animated,
					this.plugin.getTablistConfig().pages,
					this.plugin.getTablistConfig().heads.textures,
					this.plugin.getTablistConfig().cellsPing,
					this.plugin.getTablistConfig().fillCells
			);

			individualPlayerList.send();
		}
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