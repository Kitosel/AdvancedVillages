package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.compatibility.ZSound;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.data.user.VillagePermission;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageRole;
import pl.kiosel.villages.data.village.features.logs.VillageLogType;
import pl.kiosel.villages.gui.GUIS;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class PermissionGUI extends VillageMenu {

	protected PermissionGUI(AdvancedVillages plugin, VillageGUIManager menus,
							Village village, Player viewer, Gui parent, VillageRole role) {
		super(plugin, menus, village, viewer, GUIS.PERMISSION, parent);
		addBackButton();
		int maximumStart = Math.max(9, menuConfig.getSize() - VillagePermission.editableValues().size());
		int slot = plugin.getGuiSettings().integer("guis.permission.start-slot", 9, 9, maximumStart);
		for (VillagePermission permission : VillagePermission.editableValues()) {
			boolean enabled = plugin.getRoleManager().getPermissions(village, role).contains(permission);
			GuiItemConfig permissionItem = enabled
					? item("guis.permission.active", slot, Material.LIME_DYE, "&a%permission%",
					List.of("&7Status: &aEnabled", "", "&eClick to disable"))
					: item("guis.permission.inactive", slot, Material.GRAY_DYE, "&7%permission%",
					List.of("&7Status: &cDisabled", "", "&eClick to enable"));
			String permissionName = permissionName(permission);
			setButton(slot++, permissionItem.createItem(
					permissionItem.getName().replace("%permission%", permissionName),
					permissionItem.getLore().stream()
							.map(line -> line.replace("%permission%", permissionName))
							.toList()), event -> toggle(role, permission));
		}

		GuiItemConfig header = item("guis.permission.header", 31, Material.ITEM_FRAME,
				"&7Editing role: &r%role%", List.of("&7Click a permission to change it."));
		GuiItemConfig reset = item("guis.permission.reset", 32, Material.BARRIER,
				"&7Reset permission", List.of("&7Click to reset permission to &cdefault&7."));
		if (header.isEnabled()) {
			setItem(header.getSlot(), header.createItem(
					header.getName().replace("%role%", role.getName()),
					header.getLore().stream().map(line -> line.replace("%role%", role.getName())).toList()));
		}
		if (reset.isEnabled()) {
			setButton(reset.getSlot(), reset.createItem(), event -> {
				playSound(ZSound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1f);
				reset(role);
			});
		}
	}

	private void reset(VillageRole role) {
		if (!hasPermission(VillagePermission.OWNER)) return;
		if (!plugin.getRoleManager().resetPermissions(village, viewer, role)) return;

		plugin.getLogManager().record(village, VillageLogType.MEMBER_PERMISSION, viewer,
				"member", role.getId(),
				"permission", "all",
				"value", "default");
		playToggleSound();
		plugin.getGuiManager().showGUI(viewer,
				new PermissionGUI(plugin, menus, village, viewer, getParent(), role));
	}

	private void toggle(VillageRole role, VillagePermission permission) {
		if (!hasPermission(VillagePermission.OWNER)) return;
		Optional<Boolean> enabled = plugin.getRoleManager().togglePermission(village, viewer, role, permission);
		if (enabled.isEmpty()) return;
		plugin.getLogManager().record(village, VillageLogType.MEMBER_PERMISSION, viewer,
				"member", role.getId(),
				"permission", permission.name(),
				"value", enabled.get() ? "&aON" : "&cOFF");
		playToggleSound();
		plugin.getGuiManager().showGUI(viewer,
				new PermissionGUI(plugin, menus, village, viewer, getParent(), role));
	}

	private String permissionName(VillagePermission permission) {
		String id = permission.name().toLowerCase(Locale.ROOT).replace('_', '-');
		String fallback = permission.name().toLowerCase(Locale.ROOT).replace('_', ' ');
		return plugin.getGuiSettings().text("guis.permission.permission-names." + id, fallback);
	}

	private GuiItemConfig item(String path, int slot, Material material, String name, List<String> lore) {
		return item(GUIS.PERMISSION, path, slot, material, name, lore);
	}
}
