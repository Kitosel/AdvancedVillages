package pl.kiosel.villages.enums;

import lombok.Getter;

public enum CommandLang {

	ADMIN("admin", "admin"),
	ADMIN_RELOAD("admin_reload", "reload"),
	ADMIN_GIVE("admin_give", "give"),
	ADMIN_GIVE_VILLAGE("admin_give_villageblock", "village"),
	ADMIN_GIVE_DESTROYER("admin_give_destroyer", "destroyer"),
	ADMIN_UPGRADE("admin_upgrade", "upgrade"),
	ADMIN_DELETE("admin_delete", "delete"),

	REQUEST("request", "request"),
	REQUEST_ACCEPT("request_accept", "accept"),
	REQUEST_DENY("request_deny", "deny"),

	CHAT("chat", "chat"),
	INVITE("invite", "invite"),
	LEAVE("leave", "leave"),
	TELEPORT("teleport", "teleport"),
	HELP("help", "help");

	@Getter private final String path;
	@Getter private final String def;

	CommandLang(String path, String def) {
		this.path = path;
		this.def = def;
	}

}