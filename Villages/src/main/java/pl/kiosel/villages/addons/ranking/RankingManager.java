package pl.kiosel.villages.addons.ranking;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.rank.RankSystem;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.UserRank;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.Lang;

import java.util.*;

public final class RankingManager {

	private final AdvancedVillages plugin;
	private final RankingConfiguration configuration;
	private final Map<UUID, Map<UUID, Long>> recentDamagers = new HashMap<>();
	private final Map<KillPair, Long> lastRankedKills = new HashMap<>();

	@Getter
	private volatile RankingSettings settings;
	private BukkitTask refreshTask;

	public RankingManager(AdvancedVillages plugin, RankingConfiguration configuration) {
		this.plugin = plugin;
		this.configuration = configuration;
		this.settings = configuration.snapshot();
	}

	public synchronized void reload() {
		this.stopRefreshTask();
		this.configuration.reload();
		this.settings = this.configuration.snapshot();

		if (!this.settings.isEnabled()) {
			this.clearTransientState();
			return;
		}
		if (!this.plugin.isDataReady()) {
			return;
		}

		this.recalculateNow();
		long interval = this.settings.getTopRefreshSeconds() * 20L;
		this.refreshTask = Bukkit.getScheduler().runTaskTimer(
				this.plugin,
				this::refresh,
				interval,
				interval
		);
	}

	public synchronized void shutdown() {
		this.stopRefreshTask();
		this.clearTransientState();
	}

	public boolean isEnabled() {
		return this.settings.isEnabled();
	}

	public int getStartingPoints() {
		return this.settings.getStartingPoints();
	}

	public void handleDamage(Entity rawAttacker, Entity rawVictim) {
		if (!this.settings.isEnabled() || !(rawVictim instanceof Player)) {
			return;
		}
		Player attacker = resolvePlayer(rawAttacker);
		Player victim = (Player) rawVictim;
		if (attacker == null || attacker.getUniqueId().equals(victim.getUniqueId())) {
			return;
		}

		this.recentDamagers
				.computeIfAbsent(victim.getUniqueId(), ignored -> new HashMap<>())
				.put(attacker.getUniqueId(), System.currentTimeMillis());
	}

	public void handleDeath(Player victimPlayer) {
		if (!this.settings.isEnabled()) {
			return;
		}

		long now = System.currentTimeMillis();
		Map<UUID, Long> damagers = this.recentDamagers.remove(victimPlayer.getUniqueId());
		User victim = this.plugin.getUserManager().getOrCreate(victimPlayer);
		incrementDeaths(victim.getRank());

		Player killerPlayer = victimPlayer.getKiller();
		if (killerPlayer == null) {
			killerPlayer = this.findLatestOnlineDamager(damagers, now);
		}
		if (killerPlayer == null || killerPlayer.getUniqueId().equals(victimPlayer.getUniqueId())) {
			return;
		}

		User killer = this.plugin.getUserManager().getOrCreate(killerPlayer);
		if (!this.settings.isCountSameVillageKills() && areVillageMembers(killer, victim)) {
			return;
		}

		KillPair pair = new KillPair(killer.getUUID(), victim.getUUID());
		if (this.isFarmBlocked(pair, now)) {
			return;
		}
		this.lastRankedKills.put(pair, now);

		incrementKills(killer.getRank());
		RankSystem.RankResult result = this.settings.getRankSystem().calculate(
				killer.getRank().getPoints(),
				victim.getRank().getPoints()
		);

		int gained = addPoints(killer.getRank(), result.getWinnerGain(), this.settings.getMinimumPoints());
		int lost = addPoints(victim.getRank(), -result.getLoserLoss(), this.settings.getMinimumPoints());
		this.notifyFight(killerPlayer, victimPlayer, gained, -lost);
		this.awardAssists(damagers, killer, victim, now);
	}

	public void handleQuit(Player player) {
		UUID playerId = player.getUniqueId();
		this.recentDamagers.remove(playerId);
		for (Map<UUID, Long> damagers : this.recentDamagers.values()) {
			damagers.remove(playerId);
		}
	}

	public void recalculateNow() {
		if (!this.plugin.isDataReady()) {
			return;
		}
		this.plugin.getUserRankManager().recalculateTops();
		this.plugin.getVillageRankManager().recalculateTops();
	}

	private void refresh() {
		this.cleanupTransientState(System.currentTimeMillis());
		this.recalculateNow();
	}

	private void awardAssists(Map<UUID, Long> damagers, User killer, User victim, long now) {
		if (damagers == null || damagers.isEmpty()) {
			return;
		}
		long assistWindowMillis = this.settings.getAssistWindowSeconds() * 1000L;
		for (Map.Entry<UUID, Long> entry : damagers.entrySet()) {
			UUID assistantId = entry.getKey();
			if (assistantId.equals(killer.getUUID())
					|| assistantId.equals(victim.getUUID())
					|| now - entry.getValue() > assistWindowMillis) {
				continue;
			}

			User assistant = this.plugin.getUserManager().findByUuid(assistantId).orNull();
			if (assistant == null
					|| (!this.settings.isCountSameVillageKills() && areVillageMembers(assistant, victim))) {
				continue;
			}

			incrementAssists(assistant.getRank());
			int gained = addPoints(assistant.getRank(), this.settings.getAssistPoints(), this.settings.getMinimumPoints());
			Player assistantPlayer = Bukkit.getPlayer(assistantId);
			if (assistantPlayer != null && assistantPlayer.isOnline()) {
				this.plugin.getMessages().sendPrefixed(
						assistantPlayer,
						Lang.RANKING_ASSIST,
						"victim", victim.getName(),
						"points", gained
				);
			}
		}
	}

	private void notifyFight(Player killer, Player victim, int gained, int lost) {
		this.plugin.getMessages().sendPrefixed(
				killer,
				Lang.RANKING_KILL,
				"victim", victim.getName(),
				"points", gained
		);
		this.plugin.getMessages().sendPrefixed(
				victim,
				Lang.RANKING_DEATH,
				"killer", killer.getName(),
				"points", lost
		);
	}

	private boolean isFarmBlocked(KillPair pair, long now) {
		int cooldownSeconds = this.settings.getFarmCooldownSeconds();
		if (cooldownSeconds <= 0) {
			return false;
		}
		Long lastKill = this.lastRankedKills.get(pair);
		return lastKill != null && now - lastKill < cooldownSeconds * 1000L;
	}

	private Player findLatestOnlineDamager(Map<UUID, Long> damagers, long now) {
		if (damagers == null) {
			return null;
		}
		long windowMillis = this.settings.getAssistWindowSeconds() * 1000L;
		Player latest = null;
		long latestDamage = Long.MIN_VALUE;
		for (Map.Entry<UUID, Long> entry : damagers.entrySet()) {
			if (now - entry.getValue() > windowMillis || entry.getValue() <= latestDamage) {
				continue;
			}
			Player player = Bukkit.getPlayer(entry.getKey());
			if (player != null && player.isOnline()) {
				latest = player;
				latestDamage = entry.getValue();
			}
		}
		return latest;
	}

	private void cleanupTransientState(long now) {
		long damageExpiry = this.settings.getAssistWindowSeconds() * 1000L;
		Iterator<Map.Entry<UUID, Map<UUID, Long>>> damageIterator = this.recentDamagers.entrySet().iterator();
		while (damageIterator.hasNext()) {
			Map<UUID, Long> damagers = damageIterator.next().getValue();
			damagers.values().removeIf(timestamp -> now - timestamp > damageExpiry);
			if (damagers.isEmpty()) {
				damageIterator.remove();
			}
		}

		long farmExpiry = this.settings.getFarmCooldownSeconds() * 1000L;
		if (farmExpiry <= 0L) {
			this.lastRankedKills.clear();
		} else {
			this.lastRankedKills.values().removeIf(timestamp -> now - timestamp > farmExpiry);
		}
	}

	private void clearTransientState() {
		this.recentDamagers.clear();
		this.lastRankedKills.clear();
	}

	private void stopRefreshTask() {
		if (this.refreshTask != null) {
			this.refreshTask.cancel();
			this.refreshTask = null;
		}
	}

	private static Player resolvePlayer(Entity entity) {
		if (entity instanceof Player) {
			return (Player) entity;
		}
		if (entity instanceof Projectile) {
			ProjectileSource shooter = ((Projectile) entity).getShooter();
			return shooter instanceof Player ? (Player) shooter : null;
		}
		return null;
	}

	private static boolean areVillageMembers(User first, User second) {
		Village firstVillage = first.getPresentVillage();
		return firstVillage != null && Objects.equals(firstVillage, second.getPresentVillage());
	}

	private static void incrementKills(UserRank rank) {
		rank.setKills(safeIncrement(rank.getKills()));
	}

	private static void incrementDeaths(UserRank rank) {
		rank.setDeaths(safeIncrement(rank.getDeaths()));
	}

	private static void incrementAssists(UserRank rank) {
		rank.setAssists(safeIncrement(rank.getAssists()));
	}

	private static int safeIncrement(int value) {
		return value == Integer.MAX_VALUE ? value : value + 1;
	}

	private static int addPoints(UserRank rank, int delta, int minimum) {
		int previous = rank.getPoints();
		long updated = (long) previous + delta;
		int next = (int) Math.max(minimum, Math.min(Integer.MAX_VALUE, updated));
		rank.setPoints(next);
		return next - previous;
	}

	private static final class KillPair {
		private final UUID killer;
		private final UUID victim;

		private KillPair(UUID killer, UUID victim) {
			this.killer = killer;
			this.victim = victim;
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}
			if (!(object instanceof KillPair)) {
				return false;
			}
			KillPair pair = (KillPair) object;
			return this.killer.equals(pair.killer) && this.victim.equals(pair.victim);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.killer, this.victim);
		}
	}
}
