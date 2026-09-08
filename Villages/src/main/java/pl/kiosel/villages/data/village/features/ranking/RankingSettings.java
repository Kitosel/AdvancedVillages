package pl.kiosel.villages.data.village.features.ranking;

import lombok.Getter;
import pl.kiosel.villages.data.rank.RankSystem;

@Getter
public final class RankingSettings {

	private final boolean enabled;
	private final int startingPoints;
	private final int minimumPoints;
	private final int assistPoints;
	private final int assistWindowSeconds;
	private final int farmCooldownSeconds;
	private final int topRefreshSeconds;
	private final boolean countSameVillageKills;
	private final RankSystem rankSystem;

	public RankingSettings(boolean enabled, int startingPoints, int minimumPoints,
	                       int assistPoints, int assistWindowSeconds,
	                       int farmCooldownSeconds, int topRefreshSeconds,
	                       boolean countSameVillageKills, RankSystem rankSystem) {
		this.enabled = enabled;
		this.startingPoints = startingPoints;
		this.minimumPoints = minimumPoints;
		this.assistPoints = assistPoints;
		this.assistWindowSeconds = assistWindowSeconds;
		this.farmCooldownSeconds = farmCooldownSeconds;
		this.topRefreshSeconds = topRefreshSeconds;
		this.countSameVillageKills = countSameVillageKills;
		this.rankSystem = rankSystem;
	}
}
