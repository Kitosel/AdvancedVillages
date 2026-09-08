package pl.kiosel.villages.config;

import lombok.Getter;

public enum VillageConfigFile {

	CONFIG("config.yml"),
	DATABASE("database.yml"),
	LEVELS("levels.yml"),
	GUIS("guis.yml"),
	COMMANDS("command.yml"),

	BUILD_EDITOR("addons/build-editor.yml"),
	SPAWN("addons/spawn.yml"),
	COMBAT("addons/antilogout.yml"),
	TABLIST("addons/tablist.yml"),
	SCOREBOARD("addons/scoreboard.yml"),

	VILLAGE("village/village.yml"),
	ANIMATIONS("village/animation.yml"),
	SPECIALIZATION("village/specializations.yml", true),
	QUESTS("village/quests.yml", true),
	LOGS("village/logs.yml", true),
	DEVELOPMENT("village/development.yml", true);

	@Getter private final String path;
	@Getter private final boolean developerOnly;

	VillageConfigFile(String path) {
		this(path, false);
	}

	VillageConfigFile(String path, boolean developerOnly) {
		this.path = path;
		this.developerOnly = developerOnly;
	}
}
