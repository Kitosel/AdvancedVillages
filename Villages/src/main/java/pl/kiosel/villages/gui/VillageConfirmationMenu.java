package pl.kiosel.villages.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.gui.ConfirmationGui;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.config.GuiMenuConfig;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.config.VillageMessages;
import pl.kiosel.villages.data.village.Permission;
import pl.kiosel.villages.data.village.Village;

import java.util.List;

public abstract class VillageConfirmationMenu extends ConfirmationGui {

	protected final AdvancedVillages plugin;
	protected final Village village;
	protected final Player viewer;
	private final GUIS type;

	protected VillageConfirmationMenu(AdvancedVillages plugin, Village village, Player viewer, GUIS type, Gui parent) {
		this.plugin = plugin;
		this.village = village;
		this.viewer = viewer;
		this.type = type;

		GuiMenuConfig menu = plugin.getGuiSettings().menu(type);
		setParent(parent);
		title(menu.getTitle());
		rows(menu.getRows());
		emptyItem(Item.blank(Item.Blank.WHITE));
		fillSlots(true);
		closeOnClick(false);
	}

	protected GuiItemConfig item(String id, int slot, Material material, String name, List<String> lore) {
		return plugin.getGuiSettings().item(this.type, "guis." + this.type.getId() + "." + id,
				slot, material, name, lore);
	}

	protected VillageMessages messages() {
		return plugin.getVillageMessages();
	}

	protected boolean hasPermission(Permission permission) {
		if (plugin.getRoleManager().hasPermission(viewer.getUniqueId(), permission)) return true;
		messages().get(Lang.VILLAGE_NO_PERMISSION).sendPrefixed(viewer);
		return false;
	}
}
