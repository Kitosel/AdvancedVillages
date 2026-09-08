package pl.kiosel.villages.data.village.features.development;

import lombok.Getter;

import java.util.*;

@Getter
public final class DevelopmentNode {

	private final String id;
	private final int cost;
	private final int requiredVillageLevel;
	private final Set<String> requirements;
	private final Map<DevelopmentBonus, Integer> bonuses;

	public DevelopmentNode(String id, int cost, int requiredVillageLevel,
	                       Set<String> requirements, Map<DevelopmentBonus, Integer> bonuses) {
		this.id = id;
		this.cost = cost;
		this.requiredVillageLevel = requiredVillageLevel;
		this.requirements = Collections.unmodifiableSet(new LinkedHashSet<>(requirements));
		this.bonuses = Collections.unmodifiableMap(new EnumMap<>(bonuses));
	}
}
