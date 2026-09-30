package pl.kiosel.villages.config;

import lombok.Getter;

public enum VillageConfigFile {

	CONFIG("config.yml"),
	DATABASE("database.yml"),
	LEVELS("levels.yml"),
	//GUIS("guis.yml"),
	COMMANDS("command.yml"),

	BUILD_EDITOR("addons/build-editor.yml"),
	SPAWN("addons/spawn.yml"),
	COMBAT("addons/antilogout.yml"),
	TABLIST("addons/tablist.yml"),
	SCOREBOARD("addons/scoreboard.yml"),

	GUI_COMMON("guis/common.yml"),
	GUI_SETTINGS("guis/settings.yml"),
	GUI_VILLAGE("guis/village.yml"),
	GUI_OUTPOST("guis/outpost.yml", true),

	VILLAGE("village/village.yml"),
	ANIMATIONS("village/animation.yml"),
	SPECIALIZATION("village/specializations.yml", true),
	QUESTS("village/quests.yml"),
	LOGS("village/logs.yml"),
	DEVELOPMENT("village/development.yml", true),
	OUTPOSTS("village/outposts.yml", true);

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
