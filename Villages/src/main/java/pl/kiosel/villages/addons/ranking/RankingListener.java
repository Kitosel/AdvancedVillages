package pl.kiosel.villages.addons.ranking;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class RankingListener implements Listener {

	private final RankingManager rankingManager;

	public RankingListener(RankingManager rankingManager) {
		this.rankingManager = rankingManager;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDamage(EntityDamageByEntityEvent event) {
		this.rankingManager.handleDamage(event.getDamager(), event.getEntity());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onDeath(PlayerDeathEvent event) {
		this.rankingManager.handleDeath(event.getEntity());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent event) {
		this.rankingManager.handleQuit(event.getPlayer());
	}
}
