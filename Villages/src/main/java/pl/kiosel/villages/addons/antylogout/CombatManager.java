package pl.kiosel.villages.addons.antylogout;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;
import pl.kiosel.villages.AdvancedVillages;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public final class CombatManager {

	private static final long UPDATE_INTERVAL_TICKS = 20L;
	private static final long REGION_NOTIFICATION_COOLDOWN_MILLIS = 1000L;

	private final AdvancedVillages plugin;
	private final CombatConfig configuration;
	private final CombatNotifier notifier;
	private final Map<UUID, CombatSession> sessions = new HashMap<>();
	private final Map<UUID, Long> regionNotifications = new HashMap<>();

	@Getter
	private CombatSettings settings;
	private BukkitTask updateTask;

	public CombatManager(AdvancedVillages plugin, CombatConfig configuration) {
		this.plugin = plugin;
		this.configuration = configuration;
		this.notifier = new CombatNotifier(plugin);
		this.settings = configuration.snapshot();
	}

	public void reload() {
		this.stopTask();
		this.configuration.reload();
		this.settings = this.configuration.snapshot();
		if (!this.settings.isEnabled()) {
			this.clear();
			this.plugin.getRosaLogger().info("Anti-logout reloaded: disabled");
			return;
		}

		this.refreshActiveSessions();
		this.updateTask = Bukkit.getScheduler().runTaskTimer(
				this.plugin,
				this::tick,
				UPDATE_INTERVAL_TICKS,
				UPDATE_INTERVAL_TICKS
		);
		this.plugin.getRosaLogger().info("Anti-logout reloaded: enabled, duration "
				+ this.settings.getDurationSeconds() + "s, active sessions " + this.sessions.size());
	}

	public void shutdown() {
		this.stopTask();
		this.clear();
	}

	public void handleDamage(Entity rawAttacker, Entity victim) {
		if (!this.settings.isEnabled() || !(victim instanceof LivingEntity)) {
			return;
		}

		LivingEntity attacker = this.resolveAttacker(rawAttacker);
		if (attacker == null || attacker.getUniqueId().equals(victim.getUniqueId())) {
			return;
		}

		boolean attackerIsPlayer = attacker instanceof Player;
		boolean victimIsPlayer = victim instanceof Player;
		if (!attackerIsPlayer && !victimIsPlayer) {
			return;
		}
		if (!this.settings.isCombatFromMobs() && !(attackerIsPlayer && victimIsPlayer)) {
			return;
		}

		if (attackerIsPlayer) {
			this.tag((Player) attacker, (LivingEntity) victim, true);
		}
		if (victimIsPlayer) {
			this.tag((Player) victim, attacker, false);
		}
	}

	public void handleQuit(Player player) {
		this.regionNotifications.remove(player.getUniqueId());
		CombatSession session = this.sessions.remove(player.getUniqueId());
		if (session == null || this.hasBypass(player)) {
			return;
		}

		this.plugin.getUserManager().findByPlayer(player)
				.ifPresent(user -> user.getRank().updateLogouts(logouts -> logouts + 1));
		if (this.settings.isQuitBroadcastEnabled()) {
			this.notifySafely(player, "quit broadcast", () -> this.notifier.broadcastCombatQuit(player));
		}
		if (this.settings.shouldRemoveOnOpponentDeath()) {
			this.endSessionsAgainst(player.getUniqueId(), player.getName());
		}
		if (!player.isDead() && player.getHealth() > 0.0D) {
			player.setHealth(0.0D);
		}
	}

	public boolean shouldBlockCommand(Player player, String rawCommand) {
		if (!this.settings.isEnabled()
				|| !this.settings.areCommandsBlocked()
				|| !this.isInCombat(player)
				|| this.hasBypass(player)) {
			return false;
		}

		String command = CombatSettings.normalizeCommand(rawCommand);
		if (command.isEmpty() || this.settings.isCommandAllowed(command)) {
			return false;
		}
		this.notifier.commandBlocked(player, "/" + command);
		return true;
	}

	public void handleDeath(Entity entity) {
		if (entity == null) {
			return;
		}
		this.sessions.remove(entity.getUniqueId());
		this.regionNotifications.remove(entity.getUniqueId());
		if (this.settings.shouldRemoveOnOpponentDeath()) {
			this.endSessionsAgainst(entity.getUniqueId(), getEntityName(entity));
		}
	}

	public boolean isInCombat(Player player) {
		return player != null && this.sessions.containsKey(player.getUniqueId());
	}

	public boolean hasBypass(Player player) {
		return this.settings.isBypassEnabled()
				&& player.hasPermission(this.settings.getBypassPermission());
	}

	public void notifyBlockedRegion(Player player, String regionId) {
		long nowMillis = System.currentTimeMillis();
		Long lastNotification = this.regionNotifications.get(player.getUniqueId());
		if (lastNotification != null
				&& nowMillis - lastNotification < REGION_NOTIFICATION_COOLDOWN_MILLIS) {
			return;
		}
		this.regionNotifications.put(player.getUniqueId(), nowMillis);
		this.notifySafely(player, "blocked region", () -> this.notifier.regionBlocked(player, regionId));
	}

	public void clear() {
		for (UUID playerId : this.sessions.keySet()) {
			Player player = Bukkit.getPlayer(playerId);
			if (player != null && player.isOnline()) {
				this.notifySafely(player, "action bar cleanup", () -> this.notifier.clearActionBar(player));
			}
		}
		this.sessions.clear();
		this.regionNotifications.clear();
	}

	private void tick() {
		long nowMillis = System.currentTimeMillis();
		Iterator<Map.Entry<UUID, CombatSession>> iterator = this.sessions.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, CombatSession> entry = iterator.next();
			Player player = Bukkit.getPlayer(entry.getKey());
			if (player == null || !player.isOnline()) {
				iterator.remove();
				continue;
			}

			CombatSession session = entry.getValue();
			if (session.hasExpired(nowMillis)) {
				iterator.remove();
				this.notifySafely(player, "combat end", () -> this.notifier.combatEnded(player));
				continue;
			}
			long remainingSeconds = session.getRemainingSeconds(nowMillis);
			this.notifySafely(player, "combat update",
					() -> this.notifier.combatTick(player, remainingSeconds));
		}
	}

	private void refreshActiveSessions() {
		long nowMillis = System.currentTimeMillis();
		Iterator<Map.Entry<UUID, CombatSession>> iterator = this.sessions.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, CombatSession> entry = iterator.next();
			CombatSession session = entry.getValue();
			session.updateDuration(this.settings.getDurationSeconds());
			if (!session.hasExpired(nowMillis)) {
				continue;
			}
			iterator.remove();
			Player player = Bukkit.getPlayer(entry.getKey());
			if (player != null && player.isOnline()) {
				this.notifySafely(player, "combat end after reload",
						() -> this.notifier.combatEnded(player));
			}
		}
	}

	private LivingEntity resolveAttacker(Entity rawAttacker) {
		if (rawAttacker instanceof Projectile) {
			if (!this.settings.isCombatFromProjectiles()) {
				return null;
			}
			ProjectileSource shooter = ((Projectile) rawAttacker).getShooter();
			return shooter instanceof LivingEntity ? (LivingEntity) shooter : null;
		}
		return rawAttacker instanceof LivingEntity ? (LivingEntity) rawAttacker : null;
	}

	private void tag(Player player, LivingEntity opponent, boolean playerAttacked) {
		if (this.hasBypass(player)) {
			return;
		}

		UUID playerId = player.getUniqueId();
		boolean newlyTagged = !this.sessions.containsKey(playerId);
		this.sessions.put(playerId, new CombatSession(
				opponent.getUniqueId(),
				this.settings.getDurationSeconds(),
				System.currentTimeMillis()
		));

		if (!newlyTagged || !this.settings.areStartNotificationsEnabled()) {
			return;
		}
		String opponentName = getEntityName(opponent);
		if (playerAttacked) {
			this.notifySafely(player, "combat start",
					() -> this.notifier.combatStarted(player, opponentName));
		} else {
			this.notifySafely(player, "combat start",
					() -> this.notifier.combatAttacked(player, opponentName));
		}
	}

	private void endSessionsAgainst(UUID opponentId, String opponentName) {
		Iterator<Map.Entry<UUID, CombatSession>> iterator = this.sessions.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, CombatSession> entry = iterator.next();
			if (!entry.getValue().getOpponent().equals(opponentId)) {
				continue;
			}
			iterator.remove();
			Player player = Bukkit.getPlayer(entry.getKey());
			if (player != null && player.isOnline()) {
				this.notifySafely(player, "opponent removal",
						() -> this.notifier.opponentRemoved(player, opponentName));
			}
		}
	}

	private void notifySafely(Player player, String operation, Runnable notification) {
		try {
			notification.run();
		} catch (RuntimeException exception) {
			this.plugin.getRosaLogger().log(Level.WARNING,
					"Could not send anti-logout " + operation + " to " + player.getName(), exception);
		}
	}

	private void stopTask() {
		if (this.updateTask != null) {
			this.updateTask.cancel();
			this.updateTask = null;
		}
	}

	private static String getEntityName(Entity entity) {
		if (entity instanceof Player) {
			return entity.getName();
		}
		if (entity instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity) entity;
			String customName = livingEntity.getCustomName();
			return customName == null
					? livingEntity.getType().name().toLowerCase().replace('_', ' ')
					: customName.trim();
		}
		return entity.getType().name().toLowerCase().replace('_', ' ');
	}
}
