package pl.kiosel.villages.addons.quests;

import java.time.Duration;

public final class VillageQuestView {

	private final QuestDefinition definition;
	private final int progress;
	private final boolean completed;
	private final boolean active;
	private final Duration untilReset;

	public VillageQuestView(QuestDefinition definition, int progress,
	                        boolean completed, boolean active, Duration untilReset) {
		this.definition = definition;
		this.progress = progress;
		this.completed = completed;
		this.active = active;
		this.untilReset = untilReset;
	}

	public QuestDefinition getDefinition() {
		return this.definition;
	}

	public int getProgress() {
		return this.progress;
	}

	public boolean isCompleted() {
		return this.completed;
	}

	public boolean isActive() {
		return this.active;
	}

	public Duration getUntilReset() {
		return this.untilReset;
	}
}
