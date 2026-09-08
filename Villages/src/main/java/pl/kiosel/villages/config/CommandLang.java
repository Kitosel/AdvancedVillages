package pl.kiosel.villages.config;

import lombok.Getter;

public enum CommandLang {

	ADMIN("admin", "admin"),
	ADMIN_RELOAD("admin_reload", "reload"),
	ADMIN_SETTINGS("admin_settings", "settings"),
	ADMIN_GIVE("admin_give", "give"),
	ADMIN_MANAGE("admin_manage", "manage"),
	ADMIN_DEBUG("admin_debug", "debug"),
	ADMIN_INTEGRATION("admin_integration", "integration"),

	ADMIN_GIVE_VILLAGE("admin_give_villageblock", "village"),
	ADMIN_GIVE_DESTROYER("admin_give_destroyer", "destroyer"),
	ADMIN_GIVE_DESTROYER_HEARTH("admin_give_destroyer_hearth", "destroyer-hearth"),
	ADMIN_GIVE_VILLAGE_HEARTH("admin_give_village_hearth", "village-hearth"),
	ADMIN_GIVE_VILLAGE_HEARTH_PART("admin_give_hearth_part", "village-hearth-part"),

	ADMIN_ADD("admin_add", "add"),
	ADMIN_REMOVE("admin_remove", "remove"),

	ADMIN_UPGRADE("admin_upgrade", "upgrade"),
	ADMIN_DELETE("admin_delete", "delete"),
	ADMIN_PROTECTION("admin_protection", "protection"),
	ADMIN_LIVES("admin_lives", "lives"),
	ADMIN_BANK("admin_bank", "bank"),

	ADMIN_INTEGRATION_TABLIST("admin_integration_tablist", "tablist"),
	ADMIN_INTEGRATION_INSTALL("admin_integration_install", "install"),
	ADMIN_INTEGRATION_RESTORE("admin_integration_restore", "restore"),
	ADMIN_INTEGRATION_STATUS("admin_integration_status", "status"),

	REQUEST("request", "request"),
	REQUEST_ACCEPT("request_accept", "accept"),
	REQUEST_DENY("request_deny", "deny"),

	CHAT("chat", "chat"),
	INVITE("invite", "invite"),
	LEAVE("leave", "leave"),
	TELEPORT("teleport", "teleport"),
	EDIT("edit", "edit"),
	EDIT_SAVE("edit_save", "save"),
	EDIT_CANCEL("edit_cancel", "cancel"),

	ALLIANCE("alliance", "alliance"),
	ALLIANCE_INVITE("alliance_invite", "invite"),
	ALLIANCE_ACCEPT("alliance_accept", "accept"),
	ALLIANCE_DENY("alliance_deny", "deny"),
	ALLIANCE_LEAVE("alliance_leave", "leave"),
	ALLIANCE_LIST("alliance_list", "list"),

	WAR("war", "war"),
	WAR_DECLARE("war_declare", "declare"),
	WAR_SURRENDER("war_surrender", "surrender"),
	WAR_INFO("war_info", "info"),
	HELP("help", "help"),

	SPAWN("spawn","spawn"),
	SET("set", "set");

	@Getter private final String path;
	@Getter private final String def;

	CommandLang(String path, String def) {
		this.path = path;
		this.def = def;
	}

}
