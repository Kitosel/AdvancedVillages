package pl.kiosel.villages.data.village.features.diplomacy;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.features.logs.VillageLogType;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.storage.DiplomacyStorage;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class DiplomacyManager {

	private final AdvancedVillages plugin;
	private final DiplomacyConfiguration configuration;
	private final DiplomacyStorage storage;
	private final Map<String, VillageAlliance> alliances = new ConcurrentHashMap<>();
	private final Map<String, AllianceRequest> requests = new ConcurrentHashMap<>();
	private final Map<UUID, VillageWar> wars = new ConcurrentHashMap<>();
	private final Map<UUID, WarState> observedWarStates = new ConcurrentHashMap<>();
	private final Set<UUID> removedAllianceIds = ConcurrentHashMap.newKeySet();
	private final Set<UUID> removedWarIds = ConcurrentHashMap.newKeySet();
	private final AtomicLong changeVersion = new AtomicLong(1L);
	private volatile long persistedVersion;
	@Getter
	private volatile DiplomacySettings settings;
	private volatile BukkitTask transitionTask;

	public DiplomacyManager(AdvancedVillages plugin, DiplomacyConfiguration configuration) {
		this.plugin = plugin;
		this.configuration = configuration;
		this.storage = new DiplomacyStorage(plugin);
		this.settings = configuration.snapshot();
	}

	public void load() {
		if (!this.plugin.isDev()) return;
		this.alliances.clear();
		this.requests.clear();
		this.wars.clear();
		this.observedWarStates.clear();
		this.removedAllianceIds.clear();
		this.removedWarIds.clear();

		DiplomacyStorage.LoadedDiplomacy loaded = this.storage.load();
		boolean invalidData = false;
		for (VillageAlliance alliance : loaded.getAlliances()) {
			if (!this.validPair(alliance.getFirstVillageId(), alliance.getSecondVillageId())) {
				this.removedAllianceIds.add(alliance.getId());
				invalidData = true;
				continue;
			}
			VillageAlliance previous = this.alliances.put(
					pairKey(alliance.getFirstVillageId(), alliance.getSecondVillageId()), alliance);
			if (previous != null) {
				this.removedAllianceIds.add(previous.getId());
				invalidData = true;
			}
		}
		for (VillageWar war : loaded.getWars()) {
			if (!this.validPair(war.getAttackerVillageId(), war.getDefenderVillageId())) {
				this.removedWarIds.add(war.getId());
				invalidData = true;
				continue;
			}
			this.wars.put(war.getId(), war);
		}

		this.persistedVersion = this.changeVersion.get();
		if (invalidData) {
			this.markChanged();
		}
		this.refreshTransitions(false);
		Instant now = Instant.now();
		this.wars.values().forEach(war -> this.observedWarStates.put(war.getId(), war.getState(now)));
	}

	public synchronized void start() {
		this.shutdown();
		if (!this.isEnabled()) return;
		this.transitionTask = Bukkit.getScheduler().runTaskTimer(
				this.plugin, () -> this.refreshTransitions(true), 20L, 20L * 10L);
	}

	public synchronized void shutdown() {
		if (this.transitionTask != null) {
			this.transitionTask.cancel();
			this.transitionTask = null;
		}
	}

	public void reload() {
		this.configuration.reload();
		this.settings = this.configuration.snapshot();
		if (!this.isEnabled()) {
			this.shutdown();
			return;
		}
		this.refreshTransitions(true);
	}

	public boolean isEnabled() {
		return this.plugin.isDev() && this.settings.isEnabled();
	}

	public void save(boolean ignoreUnchanged) {
		if (!this.plugin.isDev()) return;
		long version = this.changeVersion.get();
		if (ignoreUnchanged && version == this.persistedVersion) {
			return;
		}
		List<VillageWar.Snapshot> warSnapshots = this.wars.values().stream()
				.map(VillageWar::snapshot)
				.collect(Collectors.toList());
		Set<UUID> removedAlliances = new HashSet<>(this.removedAllianceIds);
		Set<UUID> removedWars = new HashSet<>(this.removedWarIds);
		try {
			this.storage.save(new ArrayList<>(this.alliances.values()),
					warSnapshots, removedAlliances, removedWars);
			this.removedAllianceIds.removeAll(removedAlliances);
			this.removedWarIds.removeAll(removedWars);
			if (this.changeVersion.get() == version) {
				this.persistedVersion = version;
			}
		} catch (RuntimeException exception) {
			this.plugin.getRosaLogger().log(Level.SEVERE, "Could not save village diplomacy", exception);
		}
	}

	public boolean areAllied(@Nullable Village first, @Nullable Village second) {
		return this.isEnabled() && first != null && second != null
				&& this.alliances.containsKey(pairKey(first.getUUID(), second.getUUID()));
	}

	public List<Village> getAllies(Village village) {
		if (!this.isEnabled() || village == null) return Collections.emptyList();
		return this.alliances.values().stream()
				.filter(alliance -> alliance.contains(village.getUUID()))
				.map(alliance -> alliance.getOther(village.getUUID()))
				.map(this::findVillage)
				.filter(Objects::nonNull)
				.sorted(Comparator.comparing(Village::getName, String.CASE_INSENSITIVE_ORDER))
				.toList();
	}

	public String getAlliesTags(Village village) {
		return getAllies(village).stream()
				.filter(Village::isTag)
				.map(Village::getTag)
				.filter(tag -> tag != null && !tag.isBlank())
				.collect(Collectors.joining(";"));
	}

	public List<Village> getPendingAllianceSenders(Village target) {
		if (!this.isEnabled() || target == null) return Collections.emptyList();
		Instant now = Instant.now();
		return this.requests.values().stream()
				.filter(request -> request.getTargetVillageId().equals(target.getUUID()))
				.filter(request -> !request.isExpired(now))
				.map(AllianceRequest::getSenderVillageId)
				.map(this::findVillage)
				.filter(Objects::nonNull)
				.sorted(Comparator.comparing(Village::getName, String.CASE_INSENSITIVE_ORDER))
				.toList();
	}

	public List<AllianceRequest> getRequests(Village village) {
		if (!this.isEnabled() || village == null) return Collections.emptyList();
		Instant now = Instant.now();
		return this.requests.values().stream()
				.filter(request -> request.getSenderVillageId().equals(village.getUUID())
						|| request.getTargetVillageId().equals(village.getUUID()))
				.filter(request -> !request.isExpired(now))
				.sorted(Comparator.comparing(AllianceRequest::getCreatedAt))
				.toList();
	}

	public DiplomacyResult requestAlliance(Village sender, Village target) {
		DiplomacySettings current = this.settings;
		if (!this.isEnabled() || !current.isAlliancesEnabled()) return DiplomacyResult.DISABLED;
		if (same(sender, target)) return DiplomacyResult.SAME_VILLAGE;
		if (this.areAllied(sender, target)) return DiplomacyResult.ALREADY_ALLIED;
		if (this.hasBlockingWar(sender, target)) return DiplomacyResult.WAR_EXISTS;
		if (this.getAllies(sender).size() >= current.getMaximumAlliances()
				|| this.getAllies(target).size() >= current.getMaximumAlliances()) {
			return DiplomacyResult.ALLIANCE_LIMIT;
		}
		if (this.hasRequestBetween(sender.getUUID(), target.getUUID())) {
			return DiplomacyResult.REQUEST_EXISTS;
		}

		Instant now = Instant.now();
		AllianceRequest request = new AllianceRequest(sender.getUUID(), target.getUUID(),
				now, now.plus(current.getRequestExpiration()));
		this.requests.put(requestKey(sender.getUUID(), target.getUUID()), request);
		this.broadcast(sender, Lang.DIPLOMACY_ALLIANCE_REQUEST_SENT,
				"village", target.getName(),
				"time", this.plugin.getVillageMessages().formatDuration(current.getRequestExpiration()));
		this.broadcast(target, Lang.DIPLOMACY_ALLIANCE_REQUEST_RECEIVED,
				"village", sender.getName(),
				"time", this.plugin.getVillageMessages().formatDuration(current.getRequestExpiration()));
		return DiplomacyResult.SUCCESS;
	}

	public DiplomacyResult acceptAlliance(Village target, Village sender) {
		DiplomacySettings current = this.settings;
		if (!this.isEnabled() || !current.isAlliancesEnabled()) return DiplomacyResult.DISABLED;
		if (same(target, sender)) return DiplomacyResult.SAME_VILLAGE;
		if (this.areAllied(target, sender)) return DiplomacyResult.ALREADY_ALLIED;
		String requestKey = requestKey(sender.getUUID(), target.getUUID());
		AllianceRequest request = this.requests.get(requestKey);
		if (request == null || request.isExpired(Instant.now())) {
			if (request != null) {
				this.requests.remove(requestKey, request);
			}
			return DiplomacyResult.NO_REQUEST;
		}
		if (this.hasBlockingWar(target, sender)) return DiplomacyResult.WAR_EXISTS;
		if (this.getAllies(target).size() >= current.getMaximumAlliances()
				|| this.getAllies(sender).size() >= current.getMaximumAlliances()) {
			return DiplomacyResult.ALLIANCE_LIMIT;
		}

		this.removeRequestsBetween(sender.getUUID(), target.getUUID());
		VillageAlliance alliance = new VillageAlliance(null, sender.getUUID(), target.getUUID(), Instant.now());
		this.alliances.put(pairKey(sender.getUUID(), target.getUUID()), alliance);
		this.markChanged();
		this.broadcastBoth(sender, target, Lang.DIPLOMACY_ALLIANCE_CREATED);
		this.logBoth(sender, target, VillageLogType.ALLIANCE_CREATED);
		return DiplomacyResult.SUCCESS;
	}

	public DiplomacyResult denyAlliance(Village target, Village sender) {
		if (!this.isEnabled() || !this.settings.isAlliancesEnabled()) return DiplomacyResult.DISABLED;
		String key = requestKey(sender.getUUID(), target.getUUID());
		AllianceRequest removed = this.requests.remove(key);
		if (removed == null || removed.isExpired(Instant.now())) return DiplomacyResult.NO_REQUEST;
		this.broadcast(sender, Lang.DIPLOMACY_ALLIANCE_REQUEST_DENIED, "village", target.getName());
		this.broadcast(target, Lang.DIPLOMACY_ALLIANCE_REQUEST_DENIED_SELF, "village", sender.getName());
		return DiplomacyResult.SUCCESS;
	}

	public DiplomacyResult breakAlliance(Village village, Village ally) {
		if (!this.isEnabled() || !this.settings.isAlliancesEnabled()) return DiplomacyResult.DISABLED;
		VillageAlliance removed = this.alliances.remove(pairKey(village.getUUID(), ally.getUUID()));
		if (removed == null) return DiplomacyResult.NOT_ALLIED;
		this.removedAllianceIds.add(removed.getId());
		this.markChanged();
		this.broadcastBoth(village, ally, Lang.DIPLOMACY_ALLIANCE_ENDED);
		this.logBoth(village, ally, VillageLogType.ALLIANCE_ENDED);
		return DiplomacyResult.SUCCESS;
	}

	public DiplomacyResult declareWar(Village attacker, Village defender) {
		this.refreshTransitions(true);
		DiplomacySettings current = this.settings;
		if (!this.isEnabled() || !current.isWarsEnabled()) return DiplomacyResult.DISABLED;
		if (same(attacker, defender)) return DiplomacyResult.SAME_VILLAGE;
		if (this.areAllied(attacker, defender)) return DiplomacyResult.ALREADY_ALLIED;
		VillageWar previous = this.findWarBetween(attacker, defender, true);
		if (previous != null) {
			return previous.getState(Instant.now()) == WarState.FINISHED
					? DiplomacyResult.WAR_COOLDOWN : DiplomacyResult.WAR_EXISTS;
		}
		if (this.countCurrentWars(attacker) >= current.getMaximumWars()
				|| this.countCurrentWars(defender) >= current.getMaximumWars()) {
			return DiplomacyResult.WAR_LIMIT;
		}
		if (attacker.getMembers().size() < current.getMinimumMembers()
				|| defender.getMembers().size() < current.getMinimumMembers()) {
			return DiplomacyResult.NOT_ENOUGH_MEMBERS;
		}
		if (attacker.getOnlineMembers().size() < current.getMinimumOnlineMembers()
				|| defender.getOnlineMembers().size() < current.getMinimumOnlineMembers()) {
			return DiplomacyResult.NOT_ENOUGH_ONLINE;
		}
		if (attacker.getBank() < current.getDeclarationCost()) {
			return DiplomacyResult.NOT_ENOUGH_BANK;
		}

		attacker.removeBank(current.getDeclarationCost());
		VillageWar war = VillageWar.create(attacker.getUUID(), defender.getUUID(), Instant.now(),
				current.getPreparationTime(), current.getWarDuration());
		this.wars.put(war.getId(), war);
		this.observedWarStates.put(war.getId(), war.getState(Instant.now()));
		this.markChanged();
		this.broadcast(attacker, Lang.DIPLOMACY_WAR_DECLARED_ATTACKER,
				"village", defender.getName(),
				"time", this.plugin.getVillageMessages().formatDuration(current.getPreparationTime()));
		this.broadcast(defender, Lang.DIPLOMACY_WAR_DECLARED_DEFENDER,
				"village", attacker.getName(),
				"time", this.plugin.getVillageMessages().formatDuration(current.getPreparationTime()));
		this.logBoth(attacker, defender, VillageLogType.WAR_DECLARED);
		if (current.getPreparationTime().isZero()) {
			this.announceWarStarted(war);
		}
		return DiplomacyResult.SUCCESS;
	}

	public DiplomacyResult surrender(Village village, Village enemy) {
		if (!this.isEnabled() || !this.settings.isWarsEnabled()) return DiplomacyResult.DISABLED;
		VillageWar war = this.findCurrentWarBetween(village, enemy);
		if (war == null) return DiplomacyResult.NO_WAR;
		this.finishWar(war, enemy.getUUID(), WarEndReason.SURRENDER, true);
		return DiplomacyResult.SUCCESS;
	}

	public DiplomacyAttackResult canAttack(@Nullable Village attacker, Village defender) {
		DiplomacySettings current = this.settings;
		if (!this.isEnabled()) return DiplomacyAttackResult.ALLOWED;
		if (attacker != null && current.isPreventAlliedVillageAttacks() && this.areAllied(attacker, defender)) {
			return DiplomacyAttackResult.ALLIED;
		}
		if (!current.isWarsEnabled() || !current.isRequireWarToAttack()) {
			return DiplomacyAttackResult.ALLOWED;
		}
		if (attacker == null) return DiplomacyAttackResult.ATTACKER_HAS_NO_VILLAGE;
		VillageWar war = this.findCurrentWarBetween(attacker, defender);
		if (war == null) return DiplomacyAttackResult.WAR_REQUIRED;
		return war.getState(Instant.now()) == WarState.PREPARING
				? DiplomacyAttackResult.WAR_PREPARING : DiplomacyAttackResult.ALLOWED;
	}

	public Duration getPreparationRemaining(Village first, Village second) {
		VillageWar war = this.findCurrentWarBetween(first, second);
		if (war == null) return Duration.ZERO;
		Duration remaining = Duration.between(Instant.now(), war.getStartsAt());
		return remaining.isNegative() ? Duration.ZERO : remaining;
	}

	public boolean isAtActiveWar(@Nullable Village first, @Nullable Village second) {
		if (first == null || second == null) return false;
		VillageWar war = this.findCurrentWarBetween(first, second);
		return war != null && war.getState(Instant.now()) == WarState.ACTIVE;
	}

	public List<VillageWar> getWars(Village village) {
		if (!this.isEnabled() || village == null) return Collections.emptyList();
		return this.wars.values().stream()
				.filter(war -> war.contains(village.getUUID()))
				.sorted(Comparator.comparing(VillageWar::getDeclaredAt).reversed())
				.toList();
	}

	public int countCurrentWars(Village village) {
		if (!this.isEnabled() || village == null) return 0;
		Instant now = Instant.now();
		return (int) this.wars.values().stream()
				.filter(war -> war.contains(village.getUUID()))
				.filter(war -> war.getState(now) != WarState.FINISHED)
				.count();
	}

	public void recordKill(Village killerVillage, Village victimVillage) {
		if (!this.isEnabled()) return;
		VillageWar war = this.findCurrentWarBetween(killerVillage, victimVillage);
		if (war == null || war.getState(Instant.now()) != WarState.ACTIVE) return;
		int score = this.plugin.getDevelopmentManager()
				.applyWarScore(killerVillage, this.settings.getKillScore());
		war.addScore(killerVillage.getUUID(), score);
		this.markChanged();
	}

	public void recordVillageLifeLost(Village attackerVillage, Village defenderVillage) {
		if (!this.isEnabled()) return;
		VillageWar war = this.findCurrentWarBetween(attackerVillage, defenderVillage);
		if (war == null || war.getState(Instant.now()) != WarState.ACTIVE) return;
		int score = this.plugin.getDevelopmentManager()
				.applyWarScore(attackerVillage, this.settings.getVillageLifeScore());
		war.addScore(attackerVillage.getUUID(), score);
		this.markChanged();
		if (defenderVillage.getLives() <= 1) {
			this.finishWar(war, attackerVillage.getUUID(), WarEndReason.VILLAGE_DESTROYED, true);
		}
	}

	public void removeVillage(Village village) {
		if (!this.plugin.isDev() || village == null) return;
		UUID villageId = village.getUUID();
		for (VillageWar war : new ArrayList<>(this.wars.values())) {
			if (!war.contains(villageId)) continue;
			if (war.getState(Instant.now()) != WarState.FINISHED) {
				this.finishWar(war, war.getOther(villageId), WarEndReason.VILLAGE_REMOVED, true);
			}
			if (this.wars.remove(war.getId(), war)) this.removedWarIds.add(war.getId());
			this.observedWarStates.remove(war.getId());
		}
		for (Map.Entry<String, VillageAlliance> entry : new ArrayList<>(this.alliances.entrySet())) {
			VillageAlliance alliance = entry.getValue();
			if (alliance.contains(villageId) && this.alliances.remove(entry.getKey(), alliance)) {
				this.removedAllianceIds.add(alliance.getId());
			}
		}
		this.requests.values().removeIf(request -> request.getSenderVillageId().equals(villageId)
				|| request.getTargetVillageId().equals(villageId));
		this.markChanged();
	}

	@Nullable
	public Village getOtherVillage(VillageWar war, Village village) {
		return war == null || village == null ? null : this.findVillage(war.getOther(village.getUUID()));
	}

	private void refreshTransitions(boolean broadcast) {
		Instant now = Instant.now();
		this.requests.values().removeIf(request -> request.isExpired(now));
		boolean changed = false;
		for (VillageWar war : new ArrayList<>(this.wars.values())) {
			VillageWar.Snapshot snapshot = war.snapshot();
			WarState previous = this.observedWarStates.get(war.getId());
			WarState current = war.getState(now);
			if (current == WarState.FINISHED && snapshot.getEndedAt() == null) {
				UUID winner = this.scoreWinner(snapshot);
				this.finishWar(war, winner, WarEndReason.TIME_LIMIT, broadcast);
				current = WarState.FINISHED;
				changed = true;
			} else if (current == WarState.ACTIVE && previous == WarState.PREPARING) {
				if (broadcast) this.announceWarStarted(war);
			}
			if (war.getState(now) == WarState.FINISHED && !war.isInCooldown(now)) {
				if (this.wars.remove(war.getId(), war)) this.removedWarIds.add(war.getId());
				this.observedWarStates.remove(war.getId());
				changed = true;
			} else {
				this.observedWarStates.put(war.getId(), current);
			}
		}
		if (changed) this.markChanged();
	}

	private void announceWarStarted(VillageWar war) {
		Village attacker = this.findVillage(war.getAttackerVillageId());
		Village defender = this.findVillage(war.getDefenderVillageId());
		if (attacker == null || defender == null) return;
		this.broadcastBoth(attacker, defender, Lang.DIPLOMACY_WAR_STARTED);
		this.logBoth(attacker, defender, VillageLogType.WAR_STARTED);
	}

	private void finishWar(VillageWar war, @Nullable UUID winnerId, WarEndReason reason, boolean broadcast) {
		if (!war.finish(Instant.now(), this.settings.getWarCooldown(), winnerId, reason)) return;
		this.markChanged();
		Village attacker = this.findVillage(war.getAttackerVillageId());
		Village defender = this.findVillage(war.getDefenderVillageId());
		Village winner = this.findVillage(winnerId);
		if (winner != null && this.settings.getWinnerBankReward() > 0) {
			long rewarded = (long) winner.getBank() + this.settings.getWinnerBankReward();
			winner.setBank((int) Math.min(Integer.MAX_VALUE, rewarded));
		}
		if (attacker != null && defender != null) {
			VillageWar.Snapshot snapshot = war.snapshot();
			if (broadcast) {
				String winnerName = winner == null
						? this.plugin.getVillageMessages().text(Lang.DIPLOMACY_WAR_DRAW)
						: winner.getName();
				this.broadcast(attacker, Lang.DIPLOMACY_WAR_FINISHED,
						"enemy", defender.getName(), "winner", winnerName,
						"our_score", snapshot.getAttackerScore(), "enemy_score", snapshot.getDefenderScore());
				this.broadcast(defender, Lang.DIPLOMACY_WAR_FINISHED,
						"enemy", attacker.getName(), "winner", winnerName,
						"our_score", snapshot.getDefenderScore(), "enemy_score", snapshot.getAttackerScore());
			}
			this.logBoth(attacker, defender, VillageLogType.WAR_ENDED);
		}
	}

	@Nullable
	private UUID scoreWinner(VillageWar.Snapshot war) {
		if (war.getAttackerScore() > war.getDefenderScore()) return war.getAttackerVillageId();
		if (war.getDefenderScore() > war.getAttackerScore()) return war.getDefenderVillageId();
		return null;
	}

	@Nullable
	private VillageWar findCurrentWarBetween(Village first, Village second) {
		if (first == null || second == null) return null;
		Instant now = Instant.now();
		return this.wars.values().stream()
				.filter(war -> war.contains(first.getUUID()) && war.contains(second.getUUID()))
				.filter(war -> war.getState(now) != WarState.FINISHED)
				.findFirst().orElse(null);
	}

	@Nullable
	private VillageWar findWarBetween(Village first, Village second, boolean includeCooldown) {
		if (first == null || second == null) return null;
		Instant now = Instant.now();
		return this.wars.values().stream()
				.filter(war -> war.contains(first.getUUID()) && war.contains(second.getUUID()))
				.filter(war -> war.getState(now) != WarState.FINISHED
						|| (includeCooldown && war.isInCooldown(now)))
				.findFirst().orElse(null);
	}

	private boolean hasBlockingWar(Village first, Village second) {
		return this.findWarBetween(first, second, true) != null;
	}

	private boolean hasRequestBetween(UUID first, UUID second) {
		Instant now = Instant.now();
		AllianceRequest direct = this.requests.get(requestKey(first, second));
		AllianceRequest reverse = this.requests.get(requestKey(second, first));
		return (direct != null && !direct.isExpired(now)) || (reverse != null && !reverse.isExpired(now));
	}

	private void removeRequestsBetween(UUID first, UUID second) {
		this.requests.remove(requestKey(first, second));
		this.requests.remove(requestKey(second, first));
	}

	private boolean validPair(UUID first, UUID second) {
		return first != null && second != null && !first.equals(second)
				&& this.findVillage(first) != null && this.findVillage(second) != null;
	}

	@Nullable
	private Village findVillage(@Nullable UUID villageId) {
		return villageId == null ? null : this.plugin.getVillageManager().findByUuid(villageId).orElse(null);
	}

	private void broadcastBoth(Village first, Village second, Lang message) {
		this.broadcast(first, message, "village", second.getName());
		this.broadcast(second, message, "village", first.getName());
	}

	private void broadcast(Village village, Lang message, Object... placeholders) {
		if (village != null) {
			village.broadcast(this.plugin.getVillageMessages().prefixedText(message, placeholders));
		}
	}

	private void logBoth(Village first, Village second, VillageLogType type) {
		if (this.plugin.getLogManager() == null) return;
		this.plugin.getLogManager().recordSystem(first, type, "village", second.getName());
		this.plugin.getLogManager().recordSystem(second, type, "village", first.getName());
	}

	private void markChanged() {
		this.changeVersion.incrementAndGet();
	}

	private static boolean same(Village first, Village second) {
		return first == null || second == null || first.getUUID().equals(second.getUUID());
	}

	private static String pairKey(UUID first, UUID second) {
		String firstText = first.toString();
		String secondText = second.toString();
		return firstText.compareTo(secondText) <= 0
				? firstText + ':' + secondText : secondText + ':' + firstText;
	}

	private static String requestKey(UUID sender, UUID target) {
		return sender + ">" + target;
	}
}
