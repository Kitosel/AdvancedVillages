package pl.kiosel.villages.data.village.features.logs;

import java.time.ZoneId;

public record VillageLogSettings(boolean enabled, int maxEntriesPerVillage, int retentionDays,
								 int antiSpamWindowSeconds, ZoneId zoneId, String dateFormat) {

}
