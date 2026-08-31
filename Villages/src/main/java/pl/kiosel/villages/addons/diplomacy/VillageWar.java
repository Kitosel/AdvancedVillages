package pl.kiosel.villages.addons.diplomacy;

import lombok.Getter;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class VillageWar {

	@Getter private final UUID id;
	@Getter private final UUID attackerVillageId;
	@Getter private final UUID defenderVillageId;
	@Getter private final Instant declaredAt;
	@Getter private final Instant startsAt;
	@Getter private final Instant scheduledEndsAt;

	private int attackerScore;
	private int defenderScore;
	private Instant endedAt;
	private Instant cooldownUntil;
	private UUID winnerVillageId;
	private WarEndReason endReason;

	public VillageWar(UUID id, UUID attackerVillageId, UUID defenderVillageId,
	                  Instant declaredAt, Instant startsAt, Instant scheduledEndsAt,
	                  int attackerScore, int defenderScore,
	                  @Nullable Instant endedAt, @Nullable Instant cooldownUntil,
	                  @Nullable UUID winnerVillageId, @Nullable WarEndReason endReason) {
		this.id = id == null ? UUID.randomUUID() : id;
		this.attackerVillageId = attackerVillageId;
		this.defenderVillageId = defenderVillageId;
		this.declaredAt = declaredAt;
		this.startsAt = startsAt;
		this.scheduledEndsAt = scheduledEndsAt;
		this.attackerScore = Math.max(0, attackerScore);
		this.defenderScore = Math.max(0, defenderScore);
		this.endedAt = endedAt;
		this.cooldownUntil = cooldownUntil;
		this.winnerVillageId = winnerVillageId;
		this.endReason = endReason;
	}

	public static VillageWar create(UUID attackerVillageId, UUID defenderVillageId,
	                                Instant now, Duration preparation, Duration duration) {
		Instant startsAt = now.plus(preparation);
		return new VillageWar(null, attackerVillageId, defenderVillageId, now,
				startsAt, startsAt.plus(duration), 0, 0,
				null, null, null, null);
	}

	public synchronized WarState getState(Instant now) {
		if (this.endedAt != null || !now.isBefore(this.scheduledEndsAt)) {
			return WarState.FINISHED;
		}
		return now.isBefore(this.startsAt) ? WarState.PREPARING : WarState.ACTIVE;
	}

	public synchronized boolean finish(Instant now, Duration cooldown,
	                                   @Nullable UUID winnerVillageId, WarEndReason reason) {
		if (this.endedAt != null) {
			return false;
		}
		this.endedAt = now;
		this.cooldownUntil = now.plus(cooldown);
		this.winnerVillageId = winnerVillageId;
		this.endReason = reason;
		return true;
	}

	public synchronized void addScore(UUID villageId, int amount) {
		int safeAmount = Math.max(0, amount);
		if (safeAmount == 0) {
			return;
		}
		if (this.attackerVillageId.equals(villageId)) {
			this.attackerScore = safeAdd(this.attackerScore, safeAmount);
		} else if (this.defenderVillageId.equals(villageId)) {
			this.defenderScore = safeAdd(this.defenderScore, safeAmount);
		}
	}

	public boolean contains(UUID villageId) {
		return this.attackerVillageId.equals(villageId) || this.defenderVillageId.equals(villageId);
	}

	@Nullable
	public UUID getOther(UUID villageId) {
		if (this.attackerVillageId.equals(villageId)) {
			return this.defenderVillageId;
		}
		if (this.defenderVillageId.equals(villageId)) {
			return this.attackerVillageId;
		}
		return null;
	}

	public synchronized boolean isInCooldown(Instant now) {
		return this.endedAt != null && this.cooldownUntil != null && this.cooldownUntil.isAfter(now);
	}

	public synchronized Snapshot snapshot() {
		return new Snapshot(this.id, this.attackerVillageId, this.defenderVillageId,
				this.declaredAt, this.startsAt, this.scheduledEndsAt,
				this.attackerScore, this.defenderScore, this.endedAt,
				this.cooldownUntil, this.winnerVillageId, this.endReason);
	}

	private static int safeAdd(int first, int second) {
		return first > Integer.MAX_VALUE - second ? Integer.MAX_VALUE : first + second;
	}

	@Getter
	public static final class Snapshot {
		private final UUID id;
		private final UUID attackerVillageId;
		private final UUID defenderVillageId;
		private final Instant declaredAt;
		private final Instant startsAt;
		private final Instant scheduledEndsAt;
		private final int attackerScore;
		private final int defenderScore;
		private final Instant endedAt;
		private final Instant cooldownUntil;
		private final UUID winnerVillageId;
		private final WarEndReason endReason;

		private Snapshot(UUID id, UUID attackerVillageId, UUID defenderVillageId,
		                 Instant declaredAt, Instant startsAt, Instant scheduledEndsAt,
		                 int attackerScore, int defenderScore, Instant endedAt,
		                 Instant cooldownUntil, UUID winnerVillageId, WarEndReason endReason) {
			this.id = id;
			this.attackerVillageId = attackerVillageId;
			this.defenderVillageId = defenderVillageId;
			this.declaredAt = declaredAt;
			this.startsAt = startsAt;
			this.scheduledEndsAt = scheduledEndsAt;
			this.attackerScore = attackerScore;
			this.defenderScore = defenderScore;
			this.endedAt = endedAt;
			this.cooldownUntil = cooldownUntil;
			this.winnerVillageId = winnerVillageId;
			this.endReason = endReason;
		}
	}
}
