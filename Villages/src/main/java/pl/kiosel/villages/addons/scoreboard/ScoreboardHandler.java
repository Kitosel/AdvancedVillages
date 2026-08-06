package pl.kiosel.villages.addons.scoreboard;

import pl.kiosel.core.configuration.Config;
import pl.kiosel.villages.AdvancedVillages;

import java.util.List;

public class ScoreboardHandler {

	private final Config scoreboardConfig;

	public ScoreboardHandler(AdvancedVillages plugin) {
		this.scoreboardConfig = plugin.getScoreboardFile();
	}

	public boolean scoreboardAnimationEnabled() {
		return this.scoreboardConfig.getBoolean("animation.enabled", false);
	}

	public String scoreboardTitle() {
		return this.scoreboardConfig.getString("title", "&6&lVillages");
	}

	public String scoreboardNoVillage() {
		return this.scoreboardConfig.getString("no-village", "&cNone");
	}

	public int getAnimationSpeed() {
		return this.scoreboardConfig.getInt("animation.speed", 2);
	}

	public List<String> getScore() {
		return this.scoreboardConfig.getStringList("score");
	}

	public List<String> getTitles() {
		return this.scoreboardConfig.getStringList("animation.titles");
	}
}