package pl.kiosel.villages.data.village.features.quest;

import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public final class QuestDefinition {

	@Getter
	private final String id;
	@Getter
	private final QuestPeriod period;
	@Getter
	private final QuestType type;
	private final Set<String> targets;
	@Getter
	private final int requiredAmount;
	@Getter
	private final QuestReward reward;

	public QuestDefinition(String id, QuestPeriod period, QuestType type, Set<String> targets,
	                       int requiredAmount, QuestReward reward) {
		this.id = id;
		this.period = period;
		this.type = type;
		Set<String> normalizedTargets = new LinkedHashSet<>();
		for (String target : targets) {
			if (target != null && !target.trim().isEmpty()) {
				normalizedTargets.add(target.trim().toUpperCase(Locale.ROOT));
			}
		}
		this.targets = Collections.unmodifiableSet(normalizedTargets);
		this.requiredAmount = Math.max(1, requiredAmount);
		this.reward = reward;
	}

	public boolean matches(QuestType eventType, String target) {
		if (this.type != eventType) {
			return false;
		}
		if (this.targets.isEmpty()) {
			return true;
		}
		return target != null && this.targets.contains(target.toUpperCase(Locale.ROOT));
	}
}
