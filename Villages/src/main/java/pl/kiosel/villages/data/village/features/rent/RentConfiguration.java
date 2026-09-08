package pl.kiosel.villages.data.village.features.rent;

import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.rosacore.utils.NumberUtils;
import pl.kiosel.rosacore.utils.TimeUtils;
import pl.kiosel.villages.AdvancedVillages;

import java.time.Duration;

public final class RentConfiguration {
	private final RosaConfig file;
	private volatile RentSettings settings;

	public RentConfiguration(AdvancedVillages plugin) {
		this.file = plugin.getVillageFile();
		this.reload();
	}

	public synchronized void reload() {
		Duration interval = TimeUtils.duration(
				this.file.getString("rent.payment-interval", "24h"), Duration.ofHours(24), false);
		this.settings = new RentSettings(
				this.file.getBoolean("rent.enabled", true),
				interval,
				positive("rent.cost.base", 100),
				positive("rent.cost.per-level", 50),
				positive("rent.cost.per-member", 25),
				NumberUtils.clamp(this.file.getInt("rent.cost.region-block-unit", 100), 1, 1_000_000),
				positive("rent.cost.per-region-unit", 2),
				NumberUtils.clamp(this.file.getInt("rent.penalty.missed-payments", 3), 1, 100),
				NumberUtils.clamp(this.file.getInt("rent.penalty.lives", 1), 0, 100)
		);
	}

	public RentSettings snapshot() {
		return this.settings;
	}

	private int positive(String path, int fallback) {
		return NumberUtils.clamp(this.file.getInt(path, fallback), 0, Integer.MAX_VALUE);
	}
}
