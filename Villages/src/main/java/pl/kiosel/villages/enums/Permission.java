package pl.kiosel.villages.enums;

import lombok.Getter;

public enum Permission {

	OWNER("owner"),
	SETTINGS("setting"),
	INVITE("invite"),
	STORE("store"),
	EFFECTS_BUY("effectbuy"),
	EFFECTS_TOGGLE("effecttog"),
	UPGRADE("upgrade"),
	STORAGE("storage"),
	BANK_ADD("bankadd"),
	BANK_REMOVE("bankrem");

	@Getter private final String perm;

	Permission(String perm) {
		this.perm = perm;
	}

}