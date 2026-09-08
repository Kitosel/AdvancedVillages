package pl.kiosel.villages.data.village.features.logs;

import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.rosacore.utils.NumberUtils;
import pl.kiosel.rosacore.utils.TimeUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.Settings;

import static pl.kiosel.rosacore.utils.ColorUtils.tl;

public final class VillageLogConfiguration {

	private final AdvancedVillages plugin;
	private final RosaConfig file;
	private volatile VillageLogSettings settings;

	public VillageLogConfiguration(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.file = plugin.getLogFile();
		this.settings = new VillageLogSettings(false, 100, 30, 10,
				TimeUtils.readZoneId(Settings.TIME_ZONE.getString()));
		this.reload();
	}

	public synchronized void reload() {
		if (!this.plugin.isDev()) return;
		this.settings = new VillageLogSettings(
				this.file.getBoolean("enabled", true),
				NumberUtils.clamp(this.file.getInt("max-entries-per-village", 100), 10, 500),
				NumberUtils.clamp(this.file.getInt("retention-days", 30), 1, 3650),
				NumberUtils.clamp(this.file.getInt("anti-spam-window-seconds", 10), 0, 300),
				TimeUtils.readZoneId(Settings.TIME_ZONE.getString())
		);
	}

	public VillageLogSettings snapshot() {
		return this.settings;
	}

	public String text(String path, String fallback, Object... placeholders) {
		String configured = this.file.getString(path, fallback);
		String result = tl(configured == null ? fallback : configured);
		for (int index = 0; index + 1 < placeholders.length; index += 2) {
			result = result.replace("%" + placeholders[index] + "%",
					String.valueOf(placeholders[index + 1]));
		}
		return result;
	}
}
