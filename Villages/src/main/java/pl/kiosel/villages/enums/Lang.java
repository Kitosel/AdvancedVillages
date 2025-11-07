package pl.kiosel.villages.enums;

import lombok.Getter;

public enum Lang {

	COMMAND_RELOAD("command.reload"),
	COMMAND_NO_PERMISSION("command.no-permission"),
	COMMAND_VILLAGE_NOT_FOUND("command.village-not-found"),
	COMMAND_ADMIN_VILLAGE_BLOCK("command.admin.village-block"),
	COMMAND_ADMIN_DESTROYER("command.admin.destroyer"),
	COMMAND_ADMIN_UPGRADE("command.admin.upgrade"),
	COMMAND_ADMIN_DELETE("command.admin.delete"),

	FULL_EQ("you-have-full-eq"),
	MONEY_REMOVE("general.player.money-remove"),
	MONEY_ADD("general.player.money-add"),
	NO_MONEY("general.player.money-no"),
	NO_XP("general.player.no-xp"),
	NO_ITEMS("general.player.no-items"),
    PLAYER_OFFLINE("general.player.offline"),
	PLAYER_ONLINE("general.player.online"),
    HAS_VILLAGE("general.player.has-village"),

	TAKE_XP("general.player.take-xp"),
	TAKE_ITEMS("general.player.take-items"),

	TELEPORT("teleport.teleporting"),
	TELEPORTED("teleport.teleported"),
	TELEPORT_COOLDOWN("teleport.cooldown"),
	TELEPORT_TITLE("teleport.title"),
	TELEPORT_SUBTITLE("teleport.subtitle"),
	TELEPORT_MOVE("teleport.move"),
	TELEPORT_SET("teleport.set"),
	TELEPORT_SET_CANCEL("teleport.set-cancel"),
	TELEPORT_SET_HOVER("teleport.set-hover"),
	TELEPORT_SET_TITLE("teleport.set-title"),
	TELEPORT_SET_SUBTITLE("teleport.set-subtitle"),

	DISABLED_WORLD("village.disabled-world"),
	VILLAGE_NO_PERMISSION("village.no-permission"),
	VILLAGE_NEARBY("village.nearby"),
	VILLAGE_SPAWN("village.spawn-nearby"),
	VILLAGE_REMOVE("village.remove"),
	VILLAGE_UPGRADE("village.upgrade"),
	VILLAGE_CHAT_FORMAT("village.chat-format"),
	VILLAGE_NO_REQ_UPGRADE("village.upgrade-no-requirements"),

	VILLAGE_IN("village.in-village"),
    VILLAGE_NO("village.no-village"),
    LEAVE_OWNER("village.leave-owner"),
    LEAVE_VILLAGE("village.leave-village"),
	CANT_EDIT("village.cant-edit"),
	EFFECT_NOT_BUY("effect-not-bought"),

	BANK_ADD("village.bank-add"),
	BANK_REMOVE("village.bank-remove"),
	BANK_NO_MONEY("village.bank-no-money"),

	ENTER_VILLAGE_AREA_TITLE("village.enter-village-area-title"),
	ENTER_VILLAGE_AREA_SUBTITLE("village.enter-village-area-subtitle"),
	LEAVE_VILLAGE_AREA_TITLE("village.leave-village-area-title"),
	LEAVE_VILLAGE_AREA_SUBTITLE("village.leave-village-area-subtitle"),

	ANVIL_NAME("tag.anvil-name-your-village"),
	TAG_VILLAGE("tag.your-village-tag"),
	TAG_NEW_VILLAGE("tag.new-village-tag"),
	TAG_NO_SET_VILLAGE("tag.closed-name-village-tag"),
	TAG_ALREADY_SET_VILLAGE("tag.already-name-village-tag"),
	TAG_TRY_AGAIN("tag.try-again"),
	TAG_SPACES("tag.has-spaces"),
	TAG_TOO_LONG("tag.too-long"),
	TAG_INVALID_CHARS("tag.invalid-chars"),

    HAS_INVITE("invite.has-invite"),
    NO_INVITE("invite.no-invite"),
    PLAYER_INVITE("invite.player-invite"),
    PLAYER_TARGET("invite.player-target"),
	INVITE_CONFIRM("invite.confirm"),
	INVITE_CANCEL("invite.cancel"),
	INVITE_CONFIRM_HOVER("invite.confirm-hover"),
	INVITE_CANCEL_HOVER("invite.cancel-hover"),
    INVITE_CONFIRMED("invite.confirmed"),
    INVITE_CANCELED("invite.canceled"),
    TARGET_JOIN_MEMBER("invite.target-join-member"),

	VILLAGE_BLOCK_NAME("items.village-block"),
	VILLAGE_DESTROYER_NAME("items.destroyer"),
	VILLAGE_HEARTH_BLOCK_NAME("items.hearth"),
	VILLAGE_PART_HEARTH_BLOCK_NAME("items.part-of-hearth"),
	VILLAGE_DESTROYER_HEARTH_NAME("items.destroyer-hearth");

    @Getter private final String path;

    Lang(String path) {
        this.path = path;
    }
}