package pl.kiosel.villages.addons.ranking;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.kiosel.rosacore.listener.RosaListener;
import pl.kiosel.villages.AdvancedVillages;

public final class RankingListener extends RosaListener {

	private final RankingManager rankingManager;

	public RankingListener(AdvancedVillages plugin) {
		super(plugin);
		this.rankingManager = plugin.getRankingManager();
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
