package pl.kiosel.villages.gui.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import pl.kiosel.common.Item;
import pl.kiosel.villages.Wioski;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.enums.Permission;
import pl.kiosel.villages.village.PermissionManager;
import pl.kiosel.villages.village.Village;

import java.util.List;
import java.util.UUID;

public class MemberSettingsInventory extends Item {

	private final Wioski plugin;
	private final PermissionManager permissionManager;

	public MemberSettingsInventory(Wioski plugin) {
		this.plugin = plugin;
		this.permissionManager = plugin.getPermissionManager();
	}

	public Inventory getInventory(Village village, Player player, UUID member) {
		Inventory inventory = Bukkit.createInventory(player, GUIS.MEMBER_SETTINGS.getSize(), GUIS.MEMBER_SETTINGS.getName());
		for(int x = 0; x < 9; ++x) { inventory.setItem(x, blank(Blank.WHITE)); }
		OfflinePlayer memberPlayer = Bukkit.getOfflinePlayer(member);
		inventory.setItem(4, createHead(memberPlayer, memberPlayer.getName(), List.of("&7Settings of: &6" + memberPlayer.getName())));
		inventory.setItem(8, blank(Blank.BACK));

		inventory.setItem(9, create(Material.NOTE_BLOCK, "SETTINGS", List.of(""), permissionManager.hasPermission(member, Permission.SETTINGS)));
		inventory.setItem(10, create(Material.TOTEM_OF_UNDYING, "INVITE", List.of(""), permissionManager.hasPermission(member, Permission.INVITE)));
		inventory.setItem(11, create(Material.ENDER_EYE, "STORE", List.of(""), permissionManager.hasPermission(member, Permission.STORE)));
		inventory.setItem(12, create(Material.POTION, "EFFECTS_BUY", List.of(""), permissionManager.hasPermission(member, Permission.EFFECTS_BUY)));
		inventory.setItem(13, create(Material.SPLASH_POTION, "EFFECTS_TOGGLE", List.of(""), permissionManager.hasPermission(member, Permission.EFFECTS_TOGGLE)));
		inventory.setItem(14, create(Material.DIAMOND, "UPGRADE", List.of(""), permissionManager.hasPermission(member, Permission.UPGRADE)));
		inventory.setItem(15, create(Material.GREEN_CONCRETE_POWDER, "BANK_ADD", List.of(""), permissionManager.hasPermission(member, Permission.BANK_ADD)));
		inventory.setItem(16, create(Material.RED_CONCRETE_POWDER, "BANK_REMOVE", List.of(""), permissionManager.hasPermission(member, Permission.BANK_REMOVE)));
		inventory.setItem(17, create(Material.BARRIER, "Remove from village", List.of("")));

		return inventory;
	}
}