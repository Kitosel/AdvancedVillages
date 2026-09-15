package pl.kiosel.villages.gui.village;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.VillagePermission;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageRole;
import pl.kiosel.villages.gui.GUIS;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

import java.util.List;

public final class ResidentGUI extends VillageMenu {

	public ResidentGUI(AdvancedVillages plugin, VillageGUIManager menus, Village village,
					   Player player, Gui parent) {
		super(plugin, menus, village, player, GUIS.RESIDENT, parent);
		addBackButton();
		playSoundOnClick(true);

		int defaultStart = village.getMembers().size() > 8 ? 9 : 19;
		String startPath = village.getMembers().size() > 8 ? "start-slot" : "compact-start-slot";
		int cell = plugin.getGuiSettings().integer("guis.village.members." + startPath,
				defaultStart, 0, menuConfig.getSize() - 1);
		GuiItemConfig ownerItem = plugin.getGuiSettings().item(GUIS.RESIDENT,
				"guis.village.members.owner", cell, Material.PLAYER_HEAD,
				"&6%PLAYER% &cOWNER", java.util.List.of(
						"&7Role: %role%", " ", "&7Last online: %PLAYER_LAST_ONLINE%", " "));
		GuiItemConfig residentItem = plugin.getGuiSettings().item(GUIS.RESIDENT,
				"guis.village.members.resident", cell, Material.PLAYER_HEAD,
				"%PLAYER%", java.util.List.of(
						"&7Role: %role%", " ", "&7Last online: %PLAYER_LAST_ONLINE%", " "));
		for (User member : village.getMembers()) {
			OfflinePlayer memberPlayer = Bukkit.getOfflinePlayer(member.getUUID());
			String name = member.getName();
			boolean owner = village.isOwner(member);
			String roleName = plugin.getRoleManager().getRole(member).getName();
			String specializationName = member.getSpecialization()
					.map(plugin.getSpecializationManager()::getDisplayName)
					.orElse("-");
			GuiItemConfig configured = owner ? ownerItem : residentItem;
			if (!configured.isEnabled()) continue;
			setButton(cell++, Item.createHead(
					memberPlayer,
					configured.getName().replace("%PLAYER%", name).replace("%player%", name),
					replacePlayer(configured.getLore(), memberPlayer).stream()
							.map(line -> line.replace("%ROLE%", roleName).replace("%role%", roleName))
							.map(line -> line.replace("%SPECIALIZATION%", specializationName)
									.replace("%specialization%", specializationName))
							.toList()
			), event -> openMember(member));
		}

		if (plugin.getRoleManager().canEditPermissions(village, viewer)) {
			List<VillageRole> roles = plugin.getRoleManager().getRoles();
			if (roles.isEmpty()) return;
			int column = getCenteredRoleColumn(roles.size());
			GuiItemConfig roleItem = plugin.getGuiSettings().item(GUIS.RESIDENT,
					"guis.village.members.role-permissions", 0, Material.NAME_TAG,
					"&6%role%", List.of("&7Click to edit this role's permissions."));
			if (!roleItem.isEnabled()) return;
			for (VillageRole role : roles) {
				String roleName = roleItem.getName().replace("%role%", role.getName());
				List<String> lore = roleItem.getLore().stream()
						.map(line -> line.replace("%role%", role.getName())).toList();
				setButton(4, column++, roleItem.createItem(roleName, lore), event ->
						plugin.getGuiManager().showGUI(event.getPlayer(), new PermissionGUI(plugin, menus, village, viewer, this, role)));
			}
		}
	}

	private int getCenteredRoleColumn(int roles) {
		return Math.max(1, (11 - roles) / 2);
	}

	private void openMember(User member) {
		if (!hasPermission(VillagePermission.OWNER)) return;
		if (village.isOwner(member) || member.getUUID().equals(viewer.getUniqueId())) {
			getVillageMessages().get(Lang.CANT_EDIT).sendPrefixed(viewer);
			return;
		}
		plugin.getGuiManager().showGUI(viewer,
				new MemberSettingsGUI(plugin, menus, village, viewer, this, member.getUUID()));
	}
}
