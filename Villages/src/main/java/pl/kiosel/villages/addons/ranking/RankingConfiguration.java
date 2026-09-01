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
		this.file = plugin.getVillageFile();
		this.reload();
	}

	public synchronized void reload() {
		String rawAlgorithm = this.file.getString("ranking.points.algorithm", "ELO");
		RankSystem.Type algorithm = RankSystem.Type.parse(rawAlgorithm);
		if (!algorithm.name().equalsIgnoreCase(rawAlgorithm)) {
			this.plugin.getRosaLogger().warning("Unknown ranking algorithm '" + rawAlgorithm + "'; using ELO");
		}

		int minimum = Math.max(0, this.file.getInt("ranking.points.minimum", 0));
		int starting = Math.max(minimum, this.file.getInt("ranking.points.starting", 1000));
		RankSystem rankSystem = new RankSystem(
				algorithm,
				NumberUtils.clamp(this.file.getInt("ranking.points.elo-k-factor", 32), 1, 1000),
				NumberUtils.clamp(this.file.getInt("ranking.points.static-winner-gain", 15), 0, 100_000),
				NumberUtils.clamp(this.file.getInt("ranking.points.static-loser-loss", 10), 0, 100_000),
				NumberUtils.clamp(this.file.getDouble("ranking.points.percent-transfer", 1.0D), 0.0D, 100.0D)
		);

		this.settings = new RankingSettings(
				this.file.getBoolean("ranking.enabled", true),
				starting,
				minimum,
				NumberUtils.clamp(this.file.getInt("ranking.assists.points", 3), 0, 100_000),
				NumberUtils.clamp(this.file.getInt("ranking.assists.window-seconds", 15), 1, 300),
				NumberUtils.clamp(this.file.getInt("ranking.anti-farm.same-opponent-cooldown-seconds", 300), 0, 86_400),
				NumberUtils.clamp(this.file.getInt("ranking.top-refresh-seconds", 30), 5, 3600),
				this.file.getBoolean("ranking.count-same-village-kills", false),
				rankSystem
		);
	}

	public RankingSettings snapshot() {
		return this.settings;
	}
}
