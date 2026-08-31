package pl.kiosel.villages.gui.village;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.logs.VillageLogType;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.role.VillageRole;
import pl.kiosel.villages.gui.GUIS;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

		User member = plugin.getUserManager().findByUuid(memberId).orElse(null);
		if (member == null) {
			GuiItemConfig notFound = item("not-found", 13, Material.BARRIER,
					"&cPlayer not found", List.of());
			if (notFound.isEnabled()) setItem(notFound.getSlot(), notFound.createItem());
			return;
		}

		VillageRole role = plugin.getRoleManager().getRole(member);
		GuiItemConfig roleButton = item("role", 13, Material.NAME_TAG, "&eRole: %role%", List.of(
				"&7Left click: next role", "&7Right click: previous role", " ", "%permissions%"));
		if (roleButton.isEnabled()) {
			List<String> lore = renderRoleLore(roleButton.getLore(), role);
			String name = roleButton.getName().replace("%role%", role.getName());
			setButton(roleButton.getSlot(), roleButton.createItem(name, lore, roleButton.isGlow()),
					ClickType.LEFT, event -> changeRole(member, 1));
			setButton(roleButton.getSlot(), roleButton.createItem(name, lore, roleButton.isGlow()),
					ClickType.RIGHT, event -> changeRole(member, -1));
		}
		GuiItemConfig remove = item("remove", 22, Material.BARRIER, "Remove from village", List.of(" "));
		if (remove.isEnabled()) {
			setButton(remove.getSlot(), remove.createItem(), event -> event.getManager().showGUI(event.getPlayer(),
					new MemberRemoveInventory(plugin, village, viewer, this, memberId)));
		}
	}

	private GuiItemConfig item(String id, int slot, Material material, String name, List<String> lore) {
		return plugin.getGuiSettings().item(GUIS.MEMBER_SETTINGS,
				"guis.member-settings." + id, slot, material, name, lore);
	}

	private List<String> replaceMember(List<String> lore, String memberName) {
		return lore.stream().map(line -> line.replace("%player%", memberName)).toList();
	}

	private List<String> renderRoleLore(List<String> template, VillageRole role) {
		List<String> rendered = new ArrayList<>();
		String permissionLine = plugin.getGuiSettings().text(
				"guis.member-settings.role.permission-line", "&8 • &f%permission%");
		String noPermissions = plugin.getGuiSettings().text(
				"guis.member-settings.role.no-permissions", "&8 • &7No permissions");
		for (String line : template) {
			if (!line.contains("%permissions%")) {
				rendered.add(line.replace("%role%", role.getName()));
				continue;
			}
			if (role.getPermissions().isEmpty()) {
				rendered.add(line.replace("%permissions%", noPermissions));
				continue;
			}
			role.getPermissions().stream().sorted().forEach(permission -> {
				String id = permission.name().toLowerCase(Locale.ROOT).replace('_', '-');
				String fallback = permission.name().toLowerCase(Locale.ROOT).replace('_', ' ');
				String display = plugin.getGuiSettings().text(
						"guis.member-settings.permission-names." + id, fallback);
				rendered.add(line.replace("%permissions%", permissionLine.replace("%permission%", display)));
			});
		}
		return rendered;
	}

	private void changeRole(User member, int direction) {
		if (!hasPermission(pl.kiosel.villages.data.village.Permission.OWNER)) return;
		VillageRole role = plugin.getRoleManager().nextRole(member, direction);
		if (!plugin.getRoleManager().changeRole(village, viewer, member, role)) return;
		plugin.getLogManager().record(village, VillageLogType.MEMBER_ROLE, viewer,
				"member", member.getName(),
				"role", role.getId());
		plugin.getVillageMessages().get(Lang.VILLAGE_ROLE_CHANGED)
				.with("player", member.getName())
				.with("role", role.getName())
				.sendPrefixed(viewer);
		playToggleSound();
		plugin.getGuiManager().showGUI(viewer,
				new MemberSettingsInventory(plugin, menus, village, viewer, getParent(), memberId));
	}
}
