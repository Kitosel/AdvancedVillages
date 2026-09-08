package pl.kiosel.villages.data.village.features.diplomacy;

import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.rosacore.utils.NumberUtils;
import pl.kiosel.rosacore.utils.TimeUtils;
import pl.kiosel.villages.AdvancedVillages;

import java.time.Duration;

public final class DiplomacyConfiguration {

	private final AdvancedVillages plugin;
	private final RosaConfig file;
	private volatile DiplomacySettings settings;

	public DiplomacyConfiguration(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.file = plugin.getVillageFile();
		this.reload();
	}

	public synchronized void reload() {
		this.settings = new DiplomacySettings(
				this.file.getBoolean("diplomacy.enabled", true),
				this.file.getBoolean("diplomacy.alliances.enabled", true),
				NumberUtils.clamp(this.file.getInt("diplomacy.alliances.maximum-per-village", 3), 0, 100),
				this.duration("diplomacy.alliances.request-expiration", Duration.ofMinutes(10), false),
				this.file.getBoolean("diplomacy.alliances.prevent-friendly-fire", true),
				this.file.getBoolean("diplomacy.alliances.prevent-village-attacks", true),
				this.file.getBoolean("diplomacy.wars.enabled", true),
				this.file.getBoolean("diplomacy.wars.require-war-to-attack", true),
				this.duration("diplomacy.wars.preparation-time", Duration.ofMinutes(10), true),
				this.duration("diplomacy.wars.duration", Duration.ofHours(24), false),
				this.duration("diplomacy.wars.cooldown", Duration.ofHours(12), true),
				NumberUtils.clamp(this.file.getInt("diplomacy.wars.maximum-per-village", 1), 0, 20),
				NumberUtils.clamp(this.file.getInt("diplomacy.wars.minimum-members", 1), 1, 100),
				NumberUtils.clamp(this.file.getInt("diplomacy.wars.minimum-online-members", 1), 0, 100),
				NumberUtils.clamp(this.file.getInt("diplomacy.wars.declaration-cost", 0), 0, Integer.MAX_VALUE),
				NumberUtils.clamp(this.file.getInt("diplomacy.wars.scoring.player-kill", 1), 0, 100_000),
				NumberUtils.clamp(this.file.getInt("diplomacy.wars.scoring.village-life", 5), 0, 100_000),
				NumberUtils.clamp(this.file.getInt("diplomacy.wars.rewards.winner-bank", 0), 0, Integer.MAX_VALUE)
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
