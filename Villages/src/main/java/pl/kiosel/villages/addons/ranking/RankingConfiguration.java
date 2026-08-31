package pl.kiosel.villages.addons.ranking;

import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.rosacore.utils.NumberUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.rank.RankSystem;

public final class RankingConfiguration {

	private final AdvancedVillages plugin;
	private final RosaConfig file;
	private volatile RankingSettings settings;

	public RankingConfiguration(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.file = plugin.getRankingFile();
		this.reload();
	}

	public synchronized void reload() {
		String rawAlgorithm = this.file.getString("points.algorithm", "ELO");
		RankSystem.Type algorithm = RankSystem.Type.parse(rawAlgorithm);
		if (!algorithm.name().equalsIgnoreCase(rawAlgorithm)) {
			this.plugin.getRosaLogger().warning("Unknown ranking algorithm '" + rawAlgorithm + "'; using ELO");
		}

		int minimum = Math.max(0, this.file.getInt("points.minimum", 0));
		int starting = Math.max(minimum, this.file.getInt("points.starting", 1000));
		RankSystem rankSystem = new RankSystem(
				algorithm,
				NumberUtils.clamp(this.file.getInt("points.elo-k-factor", 32), 1, 1000),
				NumberUtils.clamp(this.file.getInt("points.static-winner-gain", 15), 0, 100_000),
				NumberUtils.clamp(this.file.getInt("points.static-loser-loss", 10), 0, 100_000),
				NumberUtils.clamp(this.file.getDouble("points.percent-transfer", 1.0D), 0.0D, 100.0D)
		);

		this.settings = new RankingSettings(
				this.file.getBoolean("enabled", true),
				starting,
				minimum,
				NumberUtils.clamp(this.file.getInt("assists.points", 3), 0, 100_000),
				NumberUtils.clamp(this.file.getInt("assists.window-seconds", 15), 1, 300),
				NumberUtils.clamp(this.file.getInt("anti-farm.same-opponent-cooldown-seconds", 300), 0, 86_400),
				NumberUtils.clamp(this.file.getInt("top-refresh-seconds", 30), 5, 3600),
				this.file.getBoolean("count-same-village-kills", false),
				rankSystem
		);
	}

	public RankingSettings snapshot() {
		return this.settings;
	}
}
