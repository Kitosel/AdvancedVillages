package pl.kiosel.villages.addons.logs;

import pl.kiosel.core.configuration.Config;
import pl.kiosel.core.utils.NumberUtils;
import pl.kiosel.core.utils.TimeUtils;
import pl.kiosel.villages.AdvancedVillages;

public final class VillageLogConfiguration {

	private final Config file;
	private volatile VillageLogSettings settings;

	public VillageLogConfiguration(AdvancedVillages plugin) {
		this.file = plugin.getLogFile();
		this.reload();
	}

	public synchronized void reload() {
		this.settings = new VillageLogSettings(
				this.file.getBoolean("enabled", true),
				NumberUtils.clamp(this.file.getInt("max-entries-per-village", 100), 10, 500),
				NumberUtils.clamp(this.file.getInt("retention-days", 30), 1, 3650),
				NumberUtils.clamp(this.file.getInt("anti-spam-window-seconds", 10), 0, 300),
				TimeUtils.readZoneId(this.file.getString("time-zone", "Europe/Warsaw"))
		);
	}

	public VillageLogSettings snapshot() {
		return this.settings;
	}
}
