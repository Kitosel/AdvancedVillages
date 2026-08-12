package pl.kiosel.villages.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.Lang;

public class MoveListener implements Listener {

	private final AdvancedVillages plugin;

	public MoveListener(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (!this.plugin.getTeleportManager().shouldCancelOnMove(player)) {
			return;
		}

		Location from = event.getFrom();
		Location to = event.getTo();
		if (to == null || (from.getBlockX() == to.getBlockX()
				&& from.getBlockY() == to.getBlockY()
				&& from.getBlockZ() == to.getBlockZ())) {
			return;
		}

		int refunded = this.plugin.getTeleportManager().cancelTeleport(player, true);
		if (refunded >= 0) {
			this.plugin.getMessages().get(Lang.TELEPORT_MOVE).sendPrefixedMessage(player);
			if (refunded > 0) {
				this.plugin.getMessages().get(Lang.MONEY_ADD)
						.processPlaceholder("money", refunded)
						.sendPrefixedMessage(player);
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		this.plugin.getTeleportManager().cancelTeleport(event.getEntity(), true);
	}
}
