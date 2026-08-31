package pl.kiosel.villages.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import pl.kiosel.rosacore.listener.RosaListener;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.Lang;

public class MoveListener extends RosaListener {

	private final AdvancedVillages plugin;

	public MoveListener(AdvancedVillages plugin) {
		super(plugin);
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
			this.plugin.getVillageMessages().get(Lang.TELEPORT_MOVE).sendPrefixed(player);
			if (refunded > 0) {
				this.plugin.getVillageMessages().get(Lang.MONEY_ADD)
						.with("money", refunded)
						.sendPrefixed(player);
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		this.plugin.getTeleportManager().cancelTeleport(event.getEntity(), true);
	}
}
