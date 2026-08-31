package pl.kiosel.villages.config;

import lombok.Getter;

public enum VillageConfigFile {

	CONFIG("config.yml"),
	DATABASE("database.yml"),
	LEVELS("levels.yml"),
	ROLES("roles.yml"),
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
	DIPLOMACY("addons/diplomacy.yml"),
	DEVELOPMENT("addons/development.yml"),
	UPKEEP("addons/upkeep.yml"),
	TABLIST("addons/tablist.yml");

	@Getter private final String path;

	VillageConfigFile(String path) {
		this.path = path;
	}
}
