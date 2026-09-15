package pl.kiosel.villages.data.user;

import java.util.Arrays;
import java.util.List;

public enum VillagePermission {

	OWNER,
	SETTINGS,
	INVITE,
	STORE,
	EFFECTS_BUY,
	EFFECTS_TOGGLE,
	UPGRADE,
	STORAGE,
	BANK_ADD,
	BANK_REMOVE,
	QUEST_TOGGLE,
	ALLIANCE_MANAGE,
	WAR_MANAGE,
	DEVELOPMENT_BUY,
	UPKEEP,
	UNSET;

	private static final List<VillagePermission> EDITABLE = Arrays.stream(values())
			.filter(permission -> permission != OWNER && permission != UNSET)
			.toList();

	public static List<VillagePermission> editableValues() {
		return EDITABLE;
	}

}
