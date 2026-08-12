package pl.kiosel.villages.addons.antylogout.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import pl.kiosel.core.hooks.WorldGuardHook;
import pl.kiosel.villages.addons.antylogout.CombatManager;
import pl.kiosel.villages.addons.antylogout.CombatSettings;

public final class CombatRegionListener implements Listener {

	private final CombatManager combatManager;

	public CombatRegionListener(CombatManager combatManager) {
		this.combatManager = combatManager;
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Location destination = event.getTo();
		if (destination == null || !hasChangedBlock(event.getFrom(), destination)) {
			return;
		}

		Player player = event.getPlayer();
		CombatSettings settings = this.combatManager.getSettings();
		if (!settings.isEnabled()
				|| !this.combatManager.isInCombat(player)
				|| this.combatManager.hasBypass(player)) {
			return;
		}

		for (String regionName : WorldGuardHook.getRegionNames(destination)) {
			if (!settings.isRegionBlocked(regionName)) {
				continue;
			}
			Location center = WorldGuardHook.getRegionCenter(destination, regionName);
			this.knockBack(player, event.getFrom(), destination, center,
					settings.getBlockedRegionKnockback());
			this.combatManager.notifyBlockedRegion(player, regionName);
			return;
		}
	}

	private void knockBack(Player player, Location from, Location destination,
	                       Location center, double multiplier) {
		Vector direction = center == null
				? from.toVector().subtract(destination.toVector()).setY(0.0D)
				: from.toVector().subtract(center.toVector()).setY(0.0D);
		if (direction.lengthSquared() < 0.0001D) {
			direction = from.toVector().subtract(destination.toVector()).setY(0.0D);
		}
		if (direction.lengthSquared() < 0.0001D) {
			direction = player.getLocation().getDirection().multiply(-1.0D).setY(0.0D);
		}
		if (direction.lengthSquared() >= 0.0001D) {
			direction.normalize().multiply(multiplier);
		}
		direction.setY(0.1D);
		player.setVelocity(direction);
	}

	private static boolean hasChangedBlock(Location from, Location to) {
		return from.getWorld() != to.getWorld()
				|| from.getBlockX() != to.getBlockX()
				|| from.getBlockY() != to.getBlockY()
				|| from.getBlockZ() != to.getBlockZ();
	}
}
