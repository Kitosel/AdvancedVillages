package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.data.user.VillagePermission;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.gui.GUIS;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

import java.util.List;

public class PermissionGUI extends VillageMenu {

	protected PermissionGUI(AdvancedVillages plugin, VillageGUIManager menus,
							Village village, Player viewer, Gui parent) {
		super(plugin, menus, village, viewer, GUIS.PERMISSION, parent);
		addBackButton();
		int slot = plugin.getGuiSettings().integer("guis.diplomacy.start-slot", 9, 9,
				Math.max(9, menuConfig.getSize() - 1));
		for (VillagePermission permission : VillagePermission.values()) {
			setButton(slot++,
					item("permission", slot, Material.GRAY_DYE, permission.name(), List.of("permission")).createItem(),
					event -> event.getPlayer().sendMessage("1"));
		}
	}

	private GuiItemConfig item(String path, int slot, Material material, String name, List<String> lore) {
		return item(GUIS.PERMISSION, path, slot, material, name, lore);
	}
}
