package pl.kiosel.villages.data.village.features.quest;

import lombok.Getter;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/** Mutable, persistable quest progress for one village. */
public final class VillageQuestState {

	@Getter
	private final UUID villageId;
	private final AtomicLong changeVersion = new AtomicLong(1L);
	private volatile long persistedVersion;
	private final Map<QuestPeriod, Map<String, Integer>> progress = new EnumMap<>(QuestPeriod.class);
	private final Map<QuestPeriod, Set<String>> completed = new EnumMap<>(QuestPeriod.class);
	private final Map<QuestPeriod, Set<String>> active = new EnumMap<>(QuestPeriod.class);
	private String dailyPeriodKey;
	private String weeklyPeriodKey;

	public VillageQuestState(UUID villageId) {
		this(villageId, "", "", Collections.emptyMap(), Collections.emptyMap(),
				Collections.emptySet(), Collections.emptySet(),
				Collections.emptySet(), Collections.emptySet());
	}

	public VillageQuestState(UUID villageId, String dailyPeriodKey, String weeklyPeriodKey,
	                         Map<String, Integer> dailyProgress, Map<String, Integer> weeklyProgress,
	                         Set<String> dailyCompleted, Set<String> weeklyCompleted,
	                         Set<String> dailyActive, Set<String> weeklyActive) {
		this.villageId = Objects.requireNonNull(villageId, "villageId");
		this.dailyPeriodKey = valueOrEmpty(dailyPeriodKey);
		this.weeklyPeriodKey = valueOrEmpty(weeklyPeriodKey);
		this.progress.put(QuestPeriod.DAILY, new HashMap<>(dailyProgress));
		this.progress.put(QuestPeriod.WEEKLY, new HashMap<>(weeklyProgress));
		this.completed.put(QuestPeriod.DAILY, new HashSet<>(dailyCompleted));
		this.completed.put(QuestPeriod.WEEKLY, new HashSet<>(weeklyCompleted));
		this.active.put(QuestPeriod.DAILY, new HashSet<>(dailyActive));
		this.active.put(QuestPeriod.WEEKLY, new HashSet<>(weeklyActive));
	}

	public void markChanged() {
		this.changeVersion.incrementAndGet();
	}

	public void markUnchanged() {
		this.persistedVersion = this.changeVersion.get();
	}

	public boolean markUnchanged(long expectedVersion) {
		if (this.changeVersion.get() != expectedVersion) {
			return false;
		}
		this.persistedVersion = expectedVersion;
		return true;
	}

	public boolean wasChanged() {
		return this.changeVersion.get() != this.persistedVersion;
	}

	public long getChangeVersion() {
		return this.changeVersion.get();
	}

	public synchronized void ensurePeriods(String dailyKey, String weeklyKey) {
		boolean changed = false;
		PeriodTransition dailyTransition = periodTransition(this.dailyPeriodKey, dailyKey);
		if (dailyTransition != PeriodTransition.NONE) {
			this.dailyPeriodKey = dailyKey;
		}
		if (dailyTransition == PeriodTransition.ADVANCED) {
			this.progress.get(QuestPeriod.DAILY).clear();
			this.completed.get(QuestPeriod.DAILY).clear();
			this.active.get(QuestPeriod.DAILY).clear();
		}
		changed |= dailyTransition != PeriodTransition.NONE;

		PeriodTransition weeklyTransition = periodTransition(this.weeklyPeriodKey, weeklyKey);
		if (weeklyTransition != PeriodTransition.NONE) {
			this.weeklyPeriodKey = weeklyKey;
		}
		if (weeklyTransition == PeriodTransition.ADVANCED) {
			this.progress.get(QuestPeriod.WEEKLY).clear();
			this.completed.get(QuestPeriod.WEEKLY).clear();
			this.active.get(QuestPeriod.WEEKLY).clear();
		}
		changed |= weeklyTransition != PeriodTransition.NONE;

		if (changed) {
			this.markChanged();
		}
	}

	public synchronized ProgressUpdate addProgress(QuestDefinition definition, int amount) {
		if (amount <= 0 || !this.isActive(definition)
				|| this.completed.get(definition.getPeriod()).contains(definition.getId())) {
			return new ProgressUpdate(this.getProgress(definition), false);
		}

		Map<String, Integer> periodProgress = this.progress.get(definition.getPeriod());
		int previous = periodProgress.getOrDefault(definition.getId(), 0);
		int updated = (int) Math.min(
				definition.getRequiredAmount(),
				(long) previous + amount
		);
		if (updated == previous) {
			return new ProgressUpdate(updated, false);
		}

		periodProgress.put(definition.getId(), updated);
		boolean completedNow = updated >= definition.getRequiredAmount();
		if (completedNow) {
			this.completed.get(definition.getPeriod()).add(definition.getId());
		}
		this.markChanged();
		return new ProgressUpdate(updated, completedNow);
	}

	public synchronized int getProgress(QuestDefinition definition) {
		return this.progress.get(definition.getPeriod()).getOrDefault(definition.getId(), 0);
	}

	public synchronized boolean isCompleted(QuestDefinition definition) {
		return this.completed.get(definition.getPeriod()).contains(definition.getId());
	}

	public synchronized boolean activate(QuestDefinition definition) {
		if (this.isCompleted(definition)) {
			return false;
		}
		boolean activated = this.active.get(definition.getPeriod()).add(definition.getId());
		if (activated) {
			this.markChanged();
		}
		return activated;
	}

	public synchronized boolean isActive(QuestDefinition definition) {
		return this.active.get(definition.getPeriod()).contains(definition.getId());
	}

	public synchronized StoredState snapshot() {
		return new StoredState(
				this.villageId,
				this.dailyPeriodKey,
				this.weeklyPeriodKey,
				new HashMap<>(this.progress.get(QuestPeriod.DAILY)),
				new HashMap<>(this.progress.get(QuestPeriod.WEEKLY)),
				new HashSet<>(this.completed.get(QuestPeriod.DAILY)),
				new HashSet<>(this.completed.get(QuestPeriod.WEEKLY)),
				new HashSet<>(this.active.get(QuestPeriod.DAILY)),
				new HashSet<>(this.active.get(QuestPeriod.WEEKLY)),
				this.getChangeVersion()
		);
	}

	private static String valueOrEmpty(String value) {
		return value == null ? "" : value;
	}

	/**
	 * Only a later calendar key starts a new period. Missing, malformed or future
	 * keys are normalized without deleting progress, so a reload or a clock
	 * correction cannot reset quests in the middle of a period.
	 */
	private static PeriodTransition periodTransition(String storedKey, String currentKey) {
		if (Objects.equals(storedKey, currentKey)) {
			return PeriodTransition.NONE;
		}
		try {
			LocalDate stored = LocalDate.parse(storedKey);
			LocalDate current = LocalDate.parse(currentKey);
			return current.isAfter(stored)
					? PeriodTransition.ADVANCED
					: PeriodTransition.NORMALIZED;
		} catch (DateTimeException | NullPointerException exception) {
			return PeriodTransition.NORMALIZED;
		}
	}

	private enum PeriodTransition {
		NONE,
		NORMALIZED,
		ADVANCED
	}

	@Getter
	public static final class ProgressUpdate {
		private final int progress;
		private final boolean completedNow;

		ProgressUpdate(int progress, boolean completedNow) {
			this.progress = progress;
			this.completedNow = completedNow;
		}

	}

	@Getter
	public static final class StoredState {
		private final UUID villageId;
		private final String dailyPeriodKey;
		private final String weeklyPeriodKey;
		private final Map<String, Integer> dailyProgress;
		private final Map<String, Integer> weeklyProgress;
		private final Set<String> dailyCompleted;
		private final Set<String> weeklyCompleted;
		private final Set<String> dailyActive;
		private final Set<String> weeklyActive;
		private final long changeVersion;

		StoredState(UUID villageId, String dailyPeriodKey, String weeklyPeriodKey,
		            Map<String, Integer> dailyProgress, Map<String, Integer> weeklyProgress,
		            Set<String> dailyCompleted, Set<String> weeklyCompleted,
		            Set<String> dailyActive, Set<String> weeklyActive, long changeVersion) {
			this.villageId = villageId;
			this.dailyPeriodKey = dailyPeriodKey;
			this.weeklyPeriodKey = weeklyPeriodKey;
			this.dailyProgress = Collections.unmodifiableMap(dailyProgress);
			this.weeklyProgress = Collections.unmodifiableMap(weeklyProgress);
			this.dailyCompleted = Collections.unmodifiableSet(dailyCompleted);
			this.weeklyCompleted = Collections.unmodifiableSet(weeklyCompleted);
			this.dailyActive = Collections.unmodifiableSet(dailyActive);
			this.weeklyActive = Collections.unmodifiableSet(weeklyActive);
			this.changeVersion = changeVersion;
		}

	}
}
