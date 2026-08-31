package pl.kiosel.villages.addons.diplomacy;

import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.rosacore.utils.NumberUtils;
import pl.kiosel.rosacore.utils.TimeUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.Settings;

import java.time.Duration;

public final class DiplomacyConfiguration {

	private final AdvancedVillages plugin;
	private final RosaConfig file;
	private volatile DiplomacySettings settings;

	public DiplomacyConfiguration(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.file = plugin.getDiplomacyFile();
		this.reload();
	}

	public synchronized void reload() {
		this.settings = new DiplomacySettings(
				Settings.ADDONS_DIPLOMACY_ENABLE.getBoolean() && this.file.getBoolean("enabled", true),
				this.file.getBoolean("alliances.enabled", true),
				NumberUtils.clamp(this.file.getInt("alliances.maximum-per-village", 3), 0, 100),
				this.duration("alliances.request-expiration", Duration.ofMinutes(10), false),
				this.file.getBoolean("alliances.prevent-friendly-fire", true),
				this.file.getBoolean("alliances.prevent-village-attacks", true),
				this.file.getBoolean("wars.enabled", true),
				this.file.getBoolean("wars.require-war-to-attack", true),
				this.duration("wars.preparation-time", Duration.ofMinutes(10), true),
				this.duration("wars.duration", Duration.ofHours(24), false),
				this.duration("wars.cooldown", Duration.ofHours(12), true),
				NumberUtils.clamp(this.file.getInt("wars.maximum-per-village", 1), 0, 20),
				NumberUtils.clamp(this.file.getInt("wars.minimum-members", 1), 1, 100),
				NumberUtils.clamp(this.file.getInt("wars.minimum-online-members", 1), 0, 100),
				NumberUtils.clamp(this.file.getInt("wars.declaration-cost", 0), 0, Integer.MAX_VALUE),
				NumberUtils.clamp(this.file.getInt("wars.scoring.player-kill", 1), 0, 100_000),
				NumberUtils.clamp(this.file.getInt("wars.scoring.village-life", 5), 0, 100_000),
				NumberUtils.clamp(this.file.getInt("wars.rewards.winner-bank", 0), 0, Integer.MAX_VALUE)
		);
	}

	public DiplomacySettings snapshot() {
		return this.settings;
	}

	private Duration duration(String path, Duration fallback, boolean allowZero) {
		String raw = this.file.getString(path, TimeUtils.defaultDuration(fallback));
		return TimeUtils.duration(raw, fallback, allowZero);
	}
}
