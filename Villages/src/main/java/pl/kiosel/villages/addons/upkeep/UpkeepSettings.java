package pl.kiosel.villages.addons.upkeep;

import lombok.Getter;

import java.time.Duration;

@Getter
public final class UpkeepSettings {
	private final boolean enabled;
	private final Duration interval;
	private final int baseCost;
	private final int costPerLevel;
	private final int costPerMember;
	private final int regionBlockUnit;
	private final int costPerRegionUnit;
	private final int missedBeforePenalty;
	private final int lifePenalty;

	public UpkeepSettings(boolean enabled, Duration interval, int baseCost, int costPerLevel,
	                      int costPerMember, int regionBlockUnit, int costPerRegionUnit,
	                      int missedBeforePenalty, int lifePenalty) {
		this.enabled = enabled;
		this.interval = interval;
		this.baseCost = baseCost;
		this.costPerLevel = costPerLevel;
		this.costPerMember = costPerMember;
		this.regionBlockUnit = regionBlockUnit;
		this.costPerRegionUnit = costPerRegionUnit;
		this.missedBeforePenalty = missedBeforePenalty;
		this.lifePenalty = lifePenalty;
	}
}
