package pl.kiosel.villages.addons.antylogout.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import pl.kiosel.villages.addons.antylogout.CombatConfig;
import pl.kiosel.villages.addons.antylogout.CombatManager;

import java.util.Set;

public class CombatRegionListener implements Listener {

	private final CombatManager combatManager;
	private final CombatConfig combatConfig;
	private final WorldGuard worldGuard;

	public CombatRegionListener(CombatManager combatManager, CombatConfig combatConfig) {
		this.combatConfig = combatConfig;
		this.combatManager = combatManager;
		this.worldGuard = WorldGuard.getInstance();
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Location to = event.getTo();
		Location from = event.getFrom();
		if (to == null || from.distance(to) == 0.0 || !this.combatManager.getCombat(player.getUniqueId()).isPresent() || player.hasPermission(this.combatConfig.getCombatBypassPermission())) {
			return;
		}
		RegionContainer regionContainer = worldGuard.getPlatform().getRegionContainer();
		RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(player.getWorld()));
		if (regionManager == null) {
			return;
		}
		BlockVector3 playerVector = BukkitAdapter.adapt(player.getLocation()).toVector().toBlockPoint();
		Set<ProtectedRegion> regions = regionManager.getApplicableRegions(playerVector).getRegions();
		regions.stream().filter(region -> this.combatConfig.getCombatBlockedRegions().contains(region.getId())).findAny().ifPresent(region -> {
			Location regionCenter = this.getRegionCenter(region, player);
			Location playerOffset = player.getLocation().subtract(regionCenter);
			Vector knockbackDirection = new Vector(playerOffset.getX(), 0.0, playerOffset.getZ()).normalize();
			double knockbackMultiplier = this.combatConfig.getCombatBlockedRegionKnockbackMultiplier();
			Vector knockbackStrength = new Vector(knockbackMultiplier, 0.5, knockbackMultiplier);
			player.setVelocity(knockbackDirection.multiply(knockbackStrength));
			this.combatConfig.getCombatBlockedRegionEnterMessage().forEach(message -> message.send(player, "region", region.getId()));
		});
	}

	private Location getRegionCenter(ProtectedRegion region, Player player) {
		BlockVector3 regionMinCorner = region.getMinimumPoint();
		BlockVector3 regionMaxCorner = region.getMaximumPoint();
		double regionCenterX = (double) (regionMinCorner.getX() + regionMaxCorner.getX()) / 2.0;
		double regionCenterY = (double) (regionMinCorner.getY() + regionMaxCorner.getY()) / 2.0;
		double regionCenterZ = (double) (regionMinCorner.getZ() + regionMaxCorner.getZ()) / 2.0;
		return new Location(player.getWorld(), regionCenterX, regionCenterY, regionCenterZ);
	}
}