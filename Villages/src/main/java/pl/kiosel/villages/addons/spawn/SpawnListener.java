package pl.kiosel.villages.addons.spawn;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.Lang;

public class SpawnListener implements Listener {

	private final AdvancedVillages plugin;
	private final SpawnManager spawnManager;

	public SpawnListener(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.spawnManager = plugin.getSpawnManager();
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (!this.spawnManager.isCancelOnMove()
				|| !this.spawnManager.isTeleporting(player.getUniqueId())) {
			return;
		}

		Location from = event.getFrom();
		Location to = event.getTo();
		if (to == null || (from.getBlockX() == to.getBlockX()
				&& from.getBlockY() == to.getBlockY()
				&& from.getBlockZ() == to.getBlockZ())) {
			return;
		}

		int refunded = this.spawnManager.cancelTeleport(player, true);
		this.plugin.getLocale().getMessage(Lang.TELEPORT_MOVE.getPath()).sendPrefixedMessage(player);
		if (refunded > 0) {
			this.plugin.getLocale().getMessage(Lang.MONEY_ADD.getPath())
					.processPlaceholder("money", refunded)
					.sendPrefixedMessage(player);
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		this.spawnManager.cancelTeleport(event.getEntity(), true);
	}
}
