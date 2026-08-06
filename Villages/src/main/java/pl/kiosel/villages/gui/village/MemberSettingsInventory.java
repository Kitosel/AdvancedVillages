package pl.kiosel.villages.gui.village;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import pl.kiosel.core.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Permission;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

import java.util.List;
import java.util.UUID;

public final class MemberSettingsInventory extends VillageMenu {

	private final UUID memberId;

	public MemberSettingsInventory(AdvancedVillages plugin, VillageGUIManager menus, Village village,
	                               Player player, Gui parent, UUID memberId) {
		super(plugin, menus, village, player, GUIS.MEMBER_SETTINGS, parent);
		this.memberId = memberId;

		OfflinePlayer memberPlayer = Bukkit.getOfflinePlayer(memberId);
		String memberName = memberPlayer.getName() == null ? memberId.toString() : memberPlayer.getName();
		setItem(4, Item.createHead(memberPlayer, memberName, List.of("&7Settings of: &6" + memberName)));
		addBackButton(8);

		User member = plugin.getUserManager().findByUuid(memberId).orNull();
		if (member == null) {
			setItem(13, Item.create(Material.BARRIER, "&cPlayer not found"));
			return;
		}

		addPermissionButton(9, Material.NOTE_BLOCK, "SETTINGS", member, Permission.SETTINGS);
		addPermissionButton(10, Material.TOTEM_OF_UNDYING, "INVITE", member, Permission.INVITE);
		addPermissionButton(11, Material.ENDER_EYE, "STORE", member, Permission.STORE);
		addPermissionButton(12, Material.POTION, "EFFECTS_BUY", member, Permission.EFFECTS_BUY);
		addPermissionButton(13, Material.SPLASH_POTION, "EFFECTS_TOGGLE", member, Permission.EFFECTS_TOGGLE);
		addPermissionButton(14, Material.DIAMOND, "UPGRADE", member, Permission.UPGRADE);
		addPermissionButton(15, Material.GREEN_CONCRETE_POWDER, "BANK_ADD", member, Permission.BANK_ADD);
		addPermissionButton(16, Material.RED_CONCRETE_POWDER, "BANK_REMOVE", member, Permission.BANK_REMOVE);
		setButton(17, Item.create(Material.BARRIER, "Remove from village", List.of("")),
				event -> event.manager.showGUI(event.player,
						new MemberRemoveInventory(plugin, menus, village, viewer, this, memberId)));
	}

	private void addPermissionButton(int slot, Material material, String name, User member, Permission permission) {
		setButton(slot, Item.create(material, name, List.of(""),
				plugin.getPermissionManager().hasPermission(member, permission)), event -> toggle(member, permission));
	}

	private void toggle(User member, Permission permission) {
		if (!hasPermission(Permission.OWNER)) return;
		if (plugin.getPermissionManager().hasPermission(member, permission)) {
			plugin.getPermissionManager().removePermission(member, permission);
		} else {
			plugin.getPermissionManager().addPermission(member, permission);
		}
		plugin.getGuiManager().showGUI(viewer,
				new MemberSettingsInventory(plugin, menus, village, viewer, getParent(), memberId));
	}
}
