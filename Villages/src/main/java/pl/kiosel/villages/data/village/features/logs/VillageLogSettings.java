package pl.kiosel.villages.data.village.features.logs;

import lombok.Getter;

import java.time.ZoneId;

@Getter
public final class VillageLogSettings {

	private final boolean enabled;
	private final int maxEntriesPerVillage;
	private final int retentionDays;
	private final int antiSpamWindowSeconds;
	private final ZoneId zoneId;

	public VillageLogSettings(boolean enabled, int maxEntriesPerVillage,
	                          int retentionDays, int antiSpamWindowSeconds, ZoneId zoneId) {
		this.enabled = enabled;
		this.maxEntriesPerVillage = maxEntriesPerVillage;
		this.retentionDays = retentionDays;
		this.antiSpamWindowSeconds = antiSpamWindowSeconds;
		this.zoneId = zoneId;
	}
}
