package pl.kiosel.villages.addons.quests;

import lombok.Getter;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.*;

public final class QuestSettings {

	@Getter
	private final boolean enabled;
	@Getter
	private final ZoneId zoneId;
	@Getter
	private final int resetHour;
	@Getter
	private final DayOfWeek weeklyResetDay;
	private final Map<QuestPeriod, List<QuestDefinition>> definitions;
	private final Map<QuestType, List<QuestDefinition>> definitionsByType;

	public QuestSettings(boolean enabled, ZoneId zoneId, int resetHour,
	                     DayOfWeek weeklyResetDay,
	                     Map<QuestPeriod, List<QuestDefinition>> definitions) {
		this.enabled = enabled;
		this.zoneId = zoneId;
		this.resetHour = Math.max(0, Math.min(23, resetHour));
		this.weeklyResetDay = weeklyResetDay;
		Map<QuestPeriod, List<QuestDefinition>> copy = new EnumMap<>(QuestPeriod.class);
		for (QuestPeriod period : QuestPeriod.values()) {
			copy.put(period, Collections.unmodifiableList(
					new ArrayList<>(definitions.getOrDefault(period, Collections.emptyList()))));
		}
		this.definitions = Collections.unmodifiableMap(copy);
		Map<QuestType, List<QuestDefinition>> byType = new EnumMap<>(QuestType.class);
		for (QuestType type : QuestType.values()) {
			byType.put(type, new ArrayList<>());
		}
		for (List<QuestDefinition> periodDefinitions : copy.values()) {
			for (QuestDefinition definition : periodDefinitions) {
				byType.get(definition.getType()).add(definition);
			}
		}
		for (QuestType type : QuestType.values()) {
			byType.put(type, Collections.unmodifiableList(byType.get(type)));
		}
		this.definitionsByType = Collections.unmodifiableMap(byType);
	}

	public List<QuestDefinition> getDefinitions(QuestPeriod period) {
		return this.definitions.get(period);
	}

	public List<QuestDefinition> getDefinitions(QuestType type) {
		return this.definitionsByType.get(type);
	}

	public List<QuestDefinition> getAllDefinitions() {
		List<QuestDefinition> all = new ArrayList<>();
		for (QuestPeriod period : QuestPeriod.values()) {
			all.addAll(this.getDefinitions(period));
		}
		return Collections.unmodifiableList(all);
	}
}
