package pl.kiosel.villages.enums;

import lombok.Getter;

public enum Lang {

    COMMAND_RELOAD("command-reload", "%PREFIX% &7Configs reloaded"),
	COMMAND_NO_PERM("command-no-permission", "%PREFIX% &7You don't have permission to use this"),

    DISABLED_WORLD("disabled-world", "%PREFIX% &cYou can't place village in this world"),
    PLAYER_OFFLINE("player-offline", "%PREFIX% &cPlayer is offline"),
    HAS_VILLAGE("has-village", "%PREFIX% &cThis player has village"),
	PLAYER_NOW_ONLINE("player-now-online", "&aNOW ONLINE"),

	VILLAGE_LIC_BANK("has-village", "-11ee"),

	TELEPORT("teleport", "%PREFIX% &6Teleporting to village"),
	TELEPORT_COOLDOWN("teleport-cooldown", "%PREFIX% &7You have to wait %seconds% before next teleport"),
	TELEPORT_TITLE("teleport-title", "&aTeleporting..."),
	TELEPORT_SUBTITLE("teleport-subtitle", "&eTeleporting in %seconds% seconds..."),
	TELEPORT_MOVE("teleport-move", "%PREFIX% &cTeleport canceled because you move"),
	TELEPORT_SET("teleport-set", "&cSet teleport to village"),
	TELEPORT_SET_CANCEL("teleport-set-cancel", "%PREFIX% &cTeleport set canceled"),
	TELEPORT_SET_HOVER("teleport-set-hover", "&cClick to set teleport"),
	TELEPORT_SET_TITLE("teleport-set-title", "&7( &cTeleport &7)"),
	TELEPORT_SET_SUBTITLE("teleport-set-subtitle", "&6Teleport set"),
	TELEPORTED("teleported", "%PREFIX% &7You teleported to your village"),

	VILLAGE_NO_PERMISSION("village-no-permission", "%PREFIX% &cYou don't have permission to change that"),
	VILLAGE_REMOVE("village-remove", "%PREFIX% &cYou removed village"),
	VILLAGE_UPGRADE("village-upgrade", "%PREFIX% &6You upgraded village"),
	VILLAGE_NEARBY("village-nearby", "%PREFIX% &cYou can't create village because there is another village nearby (minimum %distance% blocks)"),

	MONEY_REMOVE_FOR_UPGRADE("money-remove-for-upgrade", "%PREFIX% &7Removed &a%money% &7from your account for upgrade"),
	MONEY_REMOVE("money-remove", "%PREFIX% &7Removed &a%money% from your account"),
	MONEY_ADD("money-add", "%PREFIX% &7Added &a%money% to your account"),
	NO_MONEY("money-no", "%PREFIX% &cYou don't have enough money!"),

	VILLAGE_IN("in-village", "%PREFIX% &cYou are already in village"),
    VILLAGE_NO("no-village", "%PREFIX% &cYou don't have village"),
    LEAVE_OWNER("leave-owner", "%PREFIX% &cYou can't leave because you are owner"),
    LEAVE_VILLAGE("leave-village", "%PREFIX% &aYou leaved village"),

	VILLAGE_BLOCK_NAME("block-name","&6&lVillages"),
	VILLAGE_DESTROYER_NAME("destroyer-name","&c&lDestroyer"),
	VILLAGE_HEARTH_BLOCK_NAME("hearth-name","&c&lHearth of the Village"),
	VILLAGE_DESTROYER_HEARTH_NAME("hearth-name","&c&lHearth of the Destroyer"),

	BANK_ADD("village-bank-add", "%PREFIX% &7Added to bank: &a%money%"),
	BANK_REMOVE("village-bank-remove", "%PREFIX% &7Removed from bank: &c%money%"),
	BANK_NO_MONEY("village-bank-no-money", "%PREFIX% &cBank don't have enough money for withdraw"),

	ANVIL_NAME("anvil-name-your-village", "Name your village"),
	SCOREBOARD_NONE("scoreboard-no-village", "&cNone"),

	TAG_VILLAGE("your-village-tag", "%PREFIX% Your village tag is %TAG%"),
	TAG_NEW_VILLAGE("new-village-tag", "%PREFIX% Your new tag for village is %TAG%"),
	TAG_NO_SET_VILLAGE("closed-name-village-tag", "%PREFIX% &cYou closed inventory. No tag has been set"),
	TAG_ALREADY_SET_VILLAGE("already-name-village-tag", "%PREFIX% &cThis tag is already in use"),
	TAG_TRY_AGAIN("tag-try-again", "This tag is already in use"),
	TAG_SPACES("tag-has-spaces", "Tag has spaces"),
	TAG_TOO_LONG("tag-too-long", "Tag too long"),
	TAG_INVALID_CHARS("tag-invalid-chars", "Tag has invalid chars"),

    HAS_INVITE("has-invite", "%PREFIX% &cPlayer has an invitation pending"),
    NO_INVITE("no-invite", "%PREFIX% &cYou don't have an invite to village"),

    PLAYER_INVITE("player-invite", "%PREFIX% &7You invited the player &b%TARGET% &7to your village"),
    PLAYER_TARGET("player-target", "%PREFIX% &7%PLAYER% invited you to village"),

	INVITE_CONFIRM("invite-confirm", "&7Invite - &aConfirm"),
	INVITE_CANCEL("invite-cancel", "&7Invite - &cCancel"),
	INVITE_CONFIRM_HOVER("invite-confirm-hover", "&6Click to join"),
	INVITE_CANCEL_HOVER("invite-cancel-hover", "&6Click to cancel"),
    INVITE_CONFIRMED("invite-confirmed", "%PREFIX% &7You joined to &6%OWNER% &7village"),
    INVITE_CANCELED("invite-canceled", "%PREFIX% &7You canceled invite"),
    TARGET_JOIN_MEMBER("target-join-member", "%PREFIX% &6%PLAYER% &7joined to your village"),

	ENTER_VILLAGE_AREA_TITLE("enter-village-area-title", "&6You entered village"),
	ENTER_VILLAGE_AREA_SUBTITLE("enter-village-area-subtitle", "&7%village_tag%"),
	LEAVE_VILLAGE_AREA_TITLE("leave-village-area-title", "&6You leave village"),
	LEAVE_VILLAGE_AREA_SUBTITLE("leave-village-area-subtitle", "&7%village_tag%");

    @Getter private final String path;
    @Getter private final String def;

    Lang(String path, String def) {
        this.path = path;
        this.def = def;
    }
}