package pl.kiosel.villages.data.village.features.quest;

import lombok.Getter;

import java.time.Duration;

@Getter
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

}
