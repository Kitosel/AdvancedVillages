package pl.kiosel.villages.enums;

import lombok.Getter;

@Getter
public enum GUIS {

	VILLAGE("village", "gui-village", "Village", 4, 4),
	BANK("bank", "gui-bank", "Village - &6&lBank", 4, 4),
	STORE("store", "gui-store", "Village - &aStore", 4, 4),
	RESIDENT("resident", "gui-resident", "Village - &eMembers", 4, 4),
	REMOVE("remove", "gui-remove", "Village - &c&lRemove", 3, 4),
	UPGRADE("upgrade", "gui-upgrade", "Village - &bUpgrade", 4, 4),
	SETTINGS("settings", "gui-settings", "Village - &cSettings", 4, 4),
	EFFECTS("effects", "gui-effects", "Village - &dEffects", 4, 4),
	QUESTS("quests", "gui-quests", "Village - &eQuests", 4, 4),
	LOGS("logs", "gui-logs", "Village - &6Activity logs", 4, 4),
	TAG("tag", "gui-tag", "TAG", 1, 4),
	DELETE_MEMBER("delete-member", "gui-remove-member", "&cRemove member", 3, 4),
	MEMBER_SETTINGS("member-settings", "gui-member-settings", "Village - &7Member Settings", 3, 8);

	private final String id;
	private final String titlePath;
	private final String defaultTitle;
	private final int defaultRows;
	private final int defaultBackSlot;

	GUIS(String id, String titlePath, String defaultTitle, int defaultRows, int defaultBackSlot) {
		this.id = id;
		this.titlePath = titlePath;
		this.defaultTitle = defaultTitle;
		this.defaultRows = defaultRows;
		this.defaultBackSlot = defaultBackSlot;
	}
}
