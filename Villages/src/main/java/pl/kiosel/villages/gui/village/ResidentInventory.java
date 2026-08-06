package pl.kiosel.villages.gui.village;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import pl.kiosel.core.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiConfig;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Permission;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

public final class ResidentInventory extends VillageMenu {

	public ResidentInventory(AdvancedVillages plugin, VillageGUIManager menus, Village village,
	                         Player player, Gui parent) {
		super(plugin, menus, village, player, GUIS.RESIDENT, parent, true);
		addBackButton(4);

		int cell = village.getMembers().size() > 8 ? 9 : 19;
		for (User member : village.getMembers()) {
			OfflinePlayer memberPlayer = Bukkit.getOfflinePlayer(member.getUUID());
			String name = memberPlayer.getName() == null
					? memberPlayer.getUniqueId().toString()
					: memberPlayer.getName();
			boolean owner = village.isOwner(member);
			setButton(cell++, Item.createHead(
					memberPlayer,
					(owner ? GuiConfig.guis_village_members_owner : GuiConfig.guis_village_members_member)
							.replace("%PLAYER%", name),
					replacePlayer(owner ? GuiConfig.guis_village_members_owner_lore
							: GuiConfig.guis_village_members_member_lore, memberPlayer)
			), event -> openMember(member));
		}
	}

	private void openMember(User member) {
		if (!hasPermission(Permission.OWNER)) return;
		if (village.isOwner(member) || member.getUUID().equals(viewer.getUniqueId())) {
			plugin.getLocale().getMessage(Lang.CANT_EDIT.getPath()).sendPrefixedMessage(viewer);
			return;
		}
		plugin.getGuiManager().showGUI(viewer,
				new MemberSettingsInventory(plugin, menus, village, viewer, this, member.getUUID()));
	}
}
