package pl.kiosel.villages.config;

import lombok.Getter;

public enum VillageConfigFile {

	LEVELS("levels.yml"),
	GUIS("guis.yml"),
	COMMANDS("command.yml"),
	BUILD_EDITOR("addons/build-editor.yml"),
	ANIMATIONS("addons/animation.yml"),
	SPAWN("addons/spawn.yml"),
	COMBAT("addons/antilogout.yml"),
	SCOREBOARD("addons/scoreboard.yml"),
	QUESTS("addons/quests.yml"),
	LOGS("addons/logs.yml"),
	RANKING("addons/ranking.yml"),
	TABLIST("addons/tablist.yml");

	@Getter private final String path;

	VillageConfigFile(String path) {
		this.path = path;
	}
}
