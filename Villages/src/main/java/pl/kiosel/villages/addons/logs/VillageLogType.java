package pl.kiosel.villages.addons.logs;

import lombok.Getter;
import org.bukkit.Material;

import java.util.Locale;

/** Categories stored in the persistent village activity history. */
public enum VillageLogType {

	VILLAGE_CREATED(Material.BEACON),
	MEMBER_JOIN(Material.LIME_DYE),
	MEMBER_LEAVE(Material.ORANGE_DYE),
	MEMBER_KICK(Material.RED_DYE),
	MEMBER_PERMISSION(Material.NAME_TAG),
	BANK_DEPOSIT(Material.GOLD_INGOT),
	BANK_WITHDRAW(Material.GOLD_NUGGET),
	QUEST_COMPLETED(Material.WRITABLE_BOOK),
	SETTING_CHANGED(Material.COMPARATOR),
	VILLAGE_UPGRADE(Material.DIAMOND),
	VILLAGE_ATTACK(Material.IRON_SWORD);

	@Getter private final Material icon;

	VillageLogType(Material icon) {
		this.icon = icon;
	}

	public String getConfigKey() {
		return this.name().toLowerCase(Locale.ROOT).replace('_', '-');
	}
}
