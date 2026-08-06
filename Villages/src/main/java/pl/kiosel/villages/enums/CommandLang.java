package pl.kiosel.villages.enums;

import lombok.Getter;

public enum CommandLang {

	ADMIN("admin", "admin"),
	ADMIN_RELOAD("admin_reload", "reload"),
	ADMIN_GIVE("admin_give", "give"),
	ADMIN_GIVE_VILLAGE("admin_give_villageblock", "village"),
	ADMIN_GIVE_DESTROYER("admin_give_destroyer", "destroyer"),
	ADMIN_MANAGE("admin_manage", "manage"),
	ADMIN_SETTINGS("admin_settings", "settings"),
	ADMIN_ADD("admin_add", "add"),
	ADMIN_REMOVE("admin_remove", "remove"),

	ADMIN_UPGRADE("admin_upgrade", "upgrade"),
	ADMIN_DELETE("admin_delete", "delete"),
	ADMIN_PROTECTION("admin_protection", "protection"),
	ADMIN_LIVES("admin_lives", "lives"),
	ADMIN_BANK("admin_bank", "bank"),

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
