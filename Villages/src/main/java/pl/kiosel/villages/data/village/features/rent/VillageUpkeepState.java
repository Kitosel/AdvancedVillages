package pl.kiosel.villages.data.village.features.rent;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public final class VillageUpkeepState {
	@Getter private final UUID villageId;
	@Getter private volatile Instant nextPayment;
	@Getter private volatile int missedPayments;
	@Getter private volatile boolean automaticPayment;
	private final AtomicLong version = new AtomicLong(1L);
	private volatile long persistedVersion;

	public VillageUpkeepState(UUID villageId, Instant nextPayment, int missedPayments) {
		this(villageId, nextPayment, missedPayments, true);
	}

	public VillageUpkeepState(UUID villageId, Instant nextPayment, int missedPayments, boolean automaticPayment) {
		this.villageId = villageId;
		this.nextPayment = nextPayment;
		this.missedPayments = Math.max(0, missedPayments);
		this.automaticPayment = automaticPayment;
	}

	public synchronized void update(Instant nextPayment, int missedPayments) {
		this.nextPayment = nextPayment;
		this.missedPayments = Math.max(0, missedPayments);
		this.version.incrementAndGet();
	}

	public synchronized void setAutomaticPayment(boolean automaticPayment) {
		if (this.automaticPayment == automaticPayment) return;
		this.automaticPayment = automaticPayment;
		this.version.incrementAndGet();
	}

	public boolean wasChanged() {
		return this.version.get() != this.persistedVersion;
	}

	public synchronized Snapshot snapshot() {
		return new Snapshot(this.villageId, this.nextPayment, this.automaticPayment, this.missedPayments, this.version.get());
	}

	public void markUnchanged(long expectedVersion) {
		if (this.version.get() == expectedVersion) this.persistedVersion = expectedVersion;
	}

	@Getter
	public static final class Snapshot {
		private final UUID villageId;
		private final Instant nextPayment;
		private final boolean automaticPayment;
		private final int missedPayments;
		private final long version;

		private Snapshot(UUID villageId, Instant nextPayment, boolean automaticPayment, int missedPayments, long version) {
			this.villageId = villageId;
			this.nextPayment = nextPayment;
			this.automaticPayment = automaticPayment;
			this.missedPayments = missedPayments;
			this.version = version;
		}
	}
}
