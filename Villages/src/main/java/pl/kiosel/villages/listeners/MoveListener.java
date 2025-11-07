package pl.kiosel.villages.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.settings.Settings;

public class MoveListener implements Listener {

	private final AdvancedVillages plugin;

	public MoveListener(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();

		if (plugin.getTeleportManager().getTeleportingPlayers().contains(player)) {
			if (!Settings.TELEPORT_CANCEL_ON_MOVE.getBoolean()) return;

			Location from = event.getFrom();
			Location to = event.getTo();

			if (from.getBlockX() != to.getBlockX() ||
					from.getBlockY() != to.getBlockY() ||
					from.getBlockZ() != to.getBlockZ()) {

				BukkitRunnable teleportTask = plugin.getTeleportManager().getTeleportTasks().get(player);
				if (teleportTask != null) {
					teleportTask.cancel();
					plugin.getTeleportManager().getTeleportTasks().remove(player);
				}

				plugin.getTeleportManager().getTeleportingPlayers().remove(player);
				if (teleportTask != null) {
					teleportTask.cancel();
				}
				plugin.getTeleportManager().getTeleportTasks().remove(player);
				plugin.getTeleportManager().getCooldown().remove(player.getUniqueId());
				player.resetTitle();
				plugin.getLocale().getMessage(Lang.TELEPORT_MOVE.getPath()).sendPrefixedMessage(player);
			}
		}
	}
}