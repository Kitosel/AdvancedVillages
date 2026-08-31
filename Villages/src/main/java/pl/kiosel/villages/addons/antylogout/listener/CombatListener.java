package pl.kiosel.villages.addons.antylogout.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.kiosel.rosacore.listener.RosaListener;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.antylogout.CombatManager;

public final class CombatListener extends RosaListener {

	private final CombatManager combatManager;

	public CombatListener(AdvancedVillages plugin) {
		super(plugin);
		this.combatManager = plugin.getCombatManager();
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		this.combatManager.handleDamage(event.getDamager(), event.getEntity());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		this.combatManager.handleQuit(event.getPlayer());
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		if (this.combatManager.shouldBlockCommand(event.getPlayer(), event.getMessage())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		this.combatManager.handleDeath(event.getEntity());
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (!(event instanceof PlayerDeathEvent)) {
			this.combatManager.handleDeath(event.getEntity());
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		this.combatManager.handleDeath(event.getEntity());
	}
}
