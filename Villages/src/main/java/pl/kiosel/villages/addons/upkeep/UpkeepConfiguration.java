package pl.kiosel.villages.addons.upkeep;

import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.rosacore.utils.NumberUtils;
import pl.kiosel.rosacore.utils.TimeUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.Settings;

import java.time.Duration;

public final class UpkeepConfiguration {
	private final RosaConfig file;
	private volatile UpkeepSettings settings;

	public UpkeepConfiguration(AdvancedVillages plugin) {
		this.file = plugin.getUpkeepFile();
		this.reload();
	}

	public synchronized void reload() {
		Duration interval = TimeUtils.duration(
				this.file.getString("payment-interval", "24h"), Duration.ofHours(24), false);
		this.settings = new UpkeepSettings(
				Settings.ADDONS_UPKEEP_ENABLE.getBoolean() && this.file.getBoolean("enabled", true),
				interval,
				positive("cost.base", 100),
				positive("cost.per-level", 50),
				positive("cost.per-member", 25),
				NumberUtils.clamp(this.file.getInt("cost.region-block-unit", 100), 1, 1_000_000),
				positive("cost.per-region-unit", 2),
				NumberUtils.clamp(this.file.getInt("penalty.missed-payments", 3), 1, 100),
				NumberUtils.clamp(this.file.getInt("penalty.lives", 1), 0, 100)
		);
	}

	public UpkeepSettings snapshot() {
		return this.settings;
	}

	private int positive(String path, int fallback) {
		return NumberUtils.clamp(this.file.getInt(path, fallback), 0, Integer.MAX_VALUE);
	}
}
