package pl.kiosel.villages.data.village.features.diplomacy;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.projectiles.ProjectileSource;
import pl.kiosel.rosacore.listener.RosaListener;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.village.Village;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DiplomacyListener extends RosaListener {

	private static final long MESSAGE_COOLDOWN_MILLIS = 2_000L;

	private final AdvancedVillages plugin;
	private final DiplomacyManager manager;
	private final Map<UUID, Long> lastFriendlyFireMessage = new ConcurrentHashMap<>();

	public DiplomacyListener(AdvancedVillages plugin) {
		super(plugin);
		this.plugin = plugin;
		this.manager = plugin.getDiplomacyManager();
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onDamage(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player victim)
				|| !this.manager.isEnabled()
				|| !this.manager.getSettings().isPreventAlliedFriendlyFire()) {
			return;
		}
		Player attacker = attacker(event.getDamager());
		if (attacker == null || attacker.equals(victim)) return;
		Village attackerVillage = this.plugin.getApi().getVillage(attacker);
		Village victimVillage = this.plugin.getApi().getVillage(victim);
		if (!this.manager.areAllied(attackerVillage, victimVillage)) return;

		event.setCancelled(true);
		long now = System.currentTimeMillis();
		Long previous = this.lastFriendlyFireMessage.put(attacker.getUniqueId(), now);
		if (previous == null || now - previous >= MESSAGE_COOLDOWN_MILLIS) {
			this.plugin.getVillageMessages().sendPrefixed(attacker, Lang.DIPLOMACY_ALLIANCE_FRIENDLY_FIRE);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onDeath(PlayerDeathEvent event) {
		Player victim = event.getEntity();
		Player killer = victim.getKiller();
		if (killer == null || killer.equals(victim) || !this.manager.isEnabled()) return;
		Village killerVillage = this.plugin.getApi().getVillage(killer);
		Village victimVillage = this.plugin.getApi().getVillage(victim);
		if (killerVillage != null && victimVillage != null) {
			this.manager.recordKill(killerVillage, victimVillage);
		}
	}

	private static Player attacker(Entity damager) {
		if (damager instanceof Player player) return player;
		if (damager instanceof Projectile projectile) {
			ProjectileSource shooter = projectile.getShooter();
			if (shooter instanceof Player player) return player;
		}
		return null;
	}
}
