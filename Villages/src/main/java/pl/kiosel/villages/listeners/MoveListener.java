package pl.kiosel.villages.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import pl.kiosel.villages.Wioski;
import pl.kiosel.villages.config.Config;
import pl.kiosel.villages.enums.Lang;

public class MoveListener implements Listener {

	private final Wioski plugin;

	public MoveListener(Wioski plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();

		if (plugin.getTeleportManager().getTeleportingPlayers().contains(player)) {
			if (!Config.cancel_on_move) return;

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
				player.sendMessage(plugin.getLang().getMessage(Lang.TELEPORT_MOVE));
			}
		}
	}
}