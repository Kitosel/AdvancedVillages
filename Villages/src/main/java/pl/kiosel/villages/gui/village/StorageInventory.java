package pl.kiosel.villages.gui.village;

import org.bukkit.entity.Player;
import pl.kiosel.core.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

public final class StorageInventory extends VillageMenu {

	public StorageInventory(AdvancedVillages plugin, VillageGUIManager menus, Village village,
	                        Player player, Gui parent) {
		super(plugin, menus, village, player, GUIS.STORAGE, parent);
		addBackButton(4);
		setItem(0, Item.blank(Item.Blank.PREVIUS_PAGE));
		setItem(8, Item.blank(Item.Blank.NEXT_PAGE));
	}
}
