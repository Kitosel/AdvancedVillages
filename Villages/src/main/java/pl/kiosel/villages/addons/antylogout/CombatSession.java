package pl.kiosel.villages.addons.antylogout;

import java.util.UUID;

final class CombatSession {

	private final UUID opponent;
	private final long taggedAtMillis;
	private long expiresAtMillis;

	CombatSession(UUID opponent, long durationSeconds, long nowMillis) {
		this.opponent = opponent;
		this.taggedAtMillis = nowMillis;
		this.updateDuration(durationSeconds);
	}

	UUID getOpponent() {
		return this.opponent;
	}

	void updateDuration(long durationSeconds) {
		long safeSeconds = Math.max(1L, durationSeconds);
		long durationMillis = safeSeconds > Long.MAX_VALUE / 1000L
				? Long.MAX_VALUE
				: safeSeconds * 1000L;
		this.expiresAtMillis = durationMillis > Long.MAX_VALUE - this.taggedAtMillis
				? Long.MAX_VALUE
				: this.taggedAtMillis + durationMillis;
	}

	boolean hasExpired(long nowMillis) {
		return nowMillis >= this.expiresAtMillis;
	}

	long getRemainingSeconds(long nowMillis) {
		long remainingMillis = Math.max(0L, this.expiresAtMillis - nowMillis);
		return (remainingMillis + 999L) / 1000L;
	}
}
