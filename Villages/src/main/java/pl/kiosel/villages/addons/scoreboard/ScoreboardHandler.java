package pl.kiosel.villages.addons.scoreboard;

import org.bukkit.plugin.Plugin;
import pl.kiosel.core.configuration.Config;
import pl.kiosel.villages.settings.Settings;

import java.util.Arrays;
import java.util.List;

public class ScoreboardHandler {

	private final Config scoreboardConfig;

	public ScoreboardHandler(Plugin plugin) {
		this.scoreboardConfig = new Config(plugin, "scoreboard.yml");
		loadBlacklistFile();
	}

	public boolean scoreboardAnimationEnabled() {
		return this.scoreboardConfig.getBoolean("scoreboard.animation.enabled", false);
	}

	public String scoreboardTitle() {
		return this.scoreboardConfig.getString("scoreboard.title", "&6&lVillages");
	}

	public String scoreboardNoVillage() {
		return this.scoreboardConfig.getString("scoreboard.no-village", "&cNone");
	}

	public int getAnimationSpeed() {
		return this.scoreboardConfig.getInt("scoreboard.animation.speed", 10);
	}

	public List<String> getScore() {
		return this.scoreboardConfig.getStringList("scoreboard.score");
	}

	public List<String> getTitles() {
		return this.scoreboardConfig.getStringList("scoreboard.animation.titles");
	}

	private void loadBlacklistFile() {
		this.scoreboardConfig.addDefault("scoreboard.no-village", "&cNone");
		this.scoreboardConfig.addDefault("scoreboard.title", "&6&lVillages");
		this.scoreboardConfig.addDefault("scoreboard.score", Arrays.asList(
				"&7------------",
				"&7Nick: &e%player_name%",
				"&7Money: &e%player_money%",
				"&7Village:",
				" &7Owner: &e%village_owner%",
				" &7Life: &e%village_life%",
				" &7Level: &e%village_level%",
				" &7Tag: &e%village_tag%",
				"&7--------&7----"
		));
		this.scoreboardConfig.addDefault("scoreboard.animation.enabled", true);
		this.scoreboardConfig.addDefault("scoreboard.animation.speed", 10);
		this.scoreboardConfig.addDefault("scoreboard.animation.titles", Arrays.asList(
				"&6&lVillages", "&b&lVillages", "&f&lWioski", "&b&lV&f&li&b&ll&b&ll&f&la&b&lg&f&le&f&ls"
		));
		this.scoreboardConfig.load();

		this.scoreboardConfig.saveChanges();
	}

	public void reload() {
		loadBlacklistFile();
	}
}