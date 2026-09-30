package pl.kiosel.villages.config.gui;

import lombok.Getter;

@Getter
public enum GUIS {

	//VILLAGE GUIS
	VILLAGE("village", "gui-village", "Village", 5, 4),
	BANK("bank", "gui-bank", "Village - &6&lBank", 5, 4, 2),
	STORE("store", "gui-store", "Village - &aStore", 4, 4, 2),
	RESIDENT("resident", "gui-resident", "Village - &eMembers", 5, 4),
	UPGRADE("upgrade", "gui-upgrade", "Village - &bUpgrade", 5, 4),
	SETTINGS("settings", "gui-settings", "Village - &cSettings", 5, 4),
	EFFECTS("effects", "gui-effects", "Village - &dEffects", 5, 4, 3),
	PERMISSION("permission", "gui-permission", "Village - &dRole Permission", 4, 4),
	MEMBER_SETTINGS("member-settings", "gui-member-settings", "Village - &7Member Settings", 3, 8),
	LOGS("logs", "gui-logs", "Village - &6Activity logs", 5, 4),
	UPKEEP("upkeep", "gui-upkeep", "Village - &eUpkeep", 5, 4),
	DIPLOMACY("diplomacy", "gui-diplomacy", "Village - &bDiplomacy", 5, 4),
	QUESTS("quests", "gui-quests", "Village - &eQuests", 5, 4, 2),
	DEVELOPMENT("development", "gui-development", "Village - &dDevelopment", 5, 4, 2),
	SPECIALIZATIONS("specializations", "gui-specializations", "Village - &aSpecializations", 4, 4),
	TAG("tag", "gui-tag", "Name a village", 1, 4),
	DELETE_MEMBER("delete-member", "gui-remove-member", "&cRemove member", 3, 4),
	REMOVE("remove", "gui-remove", "Village - &c&lRemove", 3, 4),
	//OUTPOST GUIS
	OUTPOST("outpost", "gui-outpost", "Outpost - &d%tag%", 3, 4),
	OUTPOST_NAME("outpost-name", "gui-outpost-name", "Name a Outpost", 1, 4),
	OUTPOST_UPGRADE("outpost-upgrade", "gui-outpost-upgrade", "Outpost - &bUpgrade", 3, 4),
	OUTPOST_SETTINGS("outpost-settings", "gui-outpost-settings", "Outpost - &cSettings", 3, 8),
	//CRAFTING
	CRAFTING("crafting", "gui-crafting", "&8Crafting", 1, 1),
	CRAFTING_RECIPE("crafting-recipe", "gui-crafting-recipe", "&8Crafting recipe", 1, 1),
	CRAFTING_EDITOR("crafting-editor", "gui-crafting-editor", "&8Edit recipe: %recipe%", 1, 1),
	//utils
	TUTORIAL("tutorial", "gui-tutorial", "&6AdvancedVillages setup", 3, 4),
	;

	private final String id;
	private final String titlePath;
	private final String defaultTitle;
	private final int defaultRows;
	private final int defaultBackSlot;
	private final int defaultRequiredLevel;

	GUIS(String id, String titlePath, String defaultTitle, int defaultRows, int defaultBackSlot) {
		this(id, titlePath, defaultTitle, defaultRows, defaultBackSlot, 0);
	}

	GUIS(String id, String titlePath, String defaultTitle, int defaultRows, int defaultBackSlot, int defaultRequiredLevel) {
		this.id = id;
		this.titlePath = titlePath;
		this.defaultTitle = defaultTitle;
		this.defaultRows = defaultRows;
		this.defaultBackSlot = defaultBackSlot;
		this.defaultRequiredLevel = defaultRequiredLevel;
	}
}
