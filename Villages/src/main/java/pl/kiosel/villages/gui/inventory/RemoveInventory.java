package pl.kiosel.villages.gui.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import pl.kiosel.villages.config.GuiConfig;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.common.Item;
import pl.kiosel.villages.gui.VillageMenu;
import pl.kiosel.villages.village.Village;

public class RemoveInventory extends VillageMenu {

	@Override
	public Inventory getInventory(Village village, Player player) {
		Inventory inventory = Bukkit.createInventory(player, GUIS.REMOVE.getSize(), GUIS.REMOVE.getName());
		for(int x = 0; x < 27; x++) {
			inventory.setItem(x, blank(Item.Blank.WHITE));
		}
		inventory.setItem(12, create(Material.LIME_CONCRETE, GuiConfig.yes));
		inventory.setItem(13, create(Material.PAPER, GuiConfig.delete_village));
		inventory.setItem(14, create(Material.RED_CONCRETE, GuiConfig.no));
		return inventory;
	}
}
