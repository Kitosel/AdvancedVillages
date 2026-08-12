package pl.kiosel.villages.gui.village;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import pl.kiosel.core.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.logs.VillageLogType;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.GUIS;
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
		GuiItemConfig header = item("header", 4, Material.PLAYER_HEAD, "%player%",
				List.of("&7Settings of: &6%player%"));
		if (header.isEnabled()) {
			setItem(header.getSlot(), Item.createHead(memberPlayer,
					header.getName().replace("%player%", memberName),
					replaceMember(header.getLore(), memberName)));
		}
		addBackButton();

		User member = plugin.getUserManager().findByUuid(memberId).orNull();
		if (member == null) {
			GuiItemConfig notFound = item("not-found", 13, Material.BARRIER,
					"&cPlayer not found", List.of());
			if (notFound.isEnabled()) setItem(notFound.getSlot(), notFound.createItem());
			return;
		}

		addPermissionButton("settings", 9, Material.NOTE_BLOCK, "SETTINGS", member, Permission.SETTINGS);
		addPermissionButton("invite", 10, Material.TOTEM_OF_UNDYING, "INVITE", member, Permission.INVITE);
		addPermissionButton("store", 11, Material.ENDER_EYE, "STORE", member, Permission.STORE);
		addPermissionButton("effects-buy", 12, Material.POTION, "EFFECTS BUY", member, Permission.EFFECTS_BUY);
		addPermissionButton("effects-toggle", 13, Material.SPLASH_POTION, "EFFECTS TOGGLE", member, Permission.EFFECTS_TOGGLE);
		addPermissionButton("upgrade", 14, Material.DIAMOND, "UPGRADE", member, Permission.UPGRADE);
		addPermissionButton("bank-add", 15, Material.GREEN_CONCRETE_POWDER, "BANK ADD", member, Permission.BANK_ADD);
		addPermissionButton("bank-remove", 16, Material.RED_CONCRETE_POWDER, "BANK REMOVE", member, Permission.BANK_REMOVE);
		addPermissionButton("quest", 17, Material.BOOK, "QUEST", member, Permission.QUEST_TOGGLE);
		GuiItemConfig remove = item("remove", 22, Material.BARRIER, "Remove from village", List.of(""));
		if (remove.isEnabled()) {
			setButton(remove.getSlot(), remove.createItem(), event -> event.manager.showGUI(event.player,
					new MemberRemoveInventory(plugin, menus, village, viewer, this, memberId)));
		}
	}

	private GuiItemConfig item(String id, int slot, Material material, String name, List<String> lore) {
		return plugin.getGuiSettings().item(GUIS.MEMBER_SETTINGS,
				"guis.member-settings." + id, slot, material, name, lore);
	}

	private List<String> replaceMember(List<String> lore, String memberName) {
		return lore.stream().map(line -> line.replace("%player%", memberName)).toList();
	}

	private void addPermissionButton(String id, int slot, Material material, String name,
	                                 User member, Permission permission) {
		GuiItemConfig button = item("permissions." + id, slot, material, name, List.of(""));
		if (!button.isEnabled()) return;
		boolean hasPermission = plugin.getPermissionManager().hasPermission(member, permission);
		setButton(button.getSlot(), button.createItem(button.getName(), button.getLore(),
				button.isGlow() || hasPermission), event -> toggle(member, permission));
	}

	private void toggle(User member, Permission permission) {
		if (!hasPermission(Permission.OWNER)) return;
		boolean enabled;
		if (plugin.getPermissionManager().hasPermission(member, permission)) {
			plugin.getPermissionManager().removePermission(member, permission);
			enabled = false;
		} else {
			plugin.getPermissionManager().addPermission(member, permission);
			enabled = true;
		}
		plugin.getLogManager().record(village, VillageLogType.MEMBER_PERMISSION, viewer,
				"member", member.getName(),
				"permission", permission.name(),
				"value", enabled);
		plugin.getGuiManager().showGUI(viewer,
				new MemberSettingsInventory(plugin, menus, village, viewer, getParent(), memberId));
	}
}
