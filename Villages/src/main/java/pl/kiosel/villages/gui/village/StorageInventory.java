package pl.kiosel.villages.gui.village;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.gui.VillageMenu;
import pl.kiosel.villages.data.village.Village;

public class StorageInventory extends VillageMenu {

	@Override
	public Inventory getInventory(Village village, Player player) {
		Inventory inventory = Bukkit.createInventory(player, GUIS.STORAGE.getSize(), GUIS.STORAGE.getName());
		for(int x = 0; x < 9; ++x) { inventory.setItem(x, blank(Blank.WHITE)); }
		inventory.setItem(4, blank(Blank.BACK));
		inventory.setItem(0, blank(Blank.PREVIUS_PAGE));
		inventory.setItem(8, blank(Blank.NEXT_PAGE));

		if (village == null) return inventory;
		return inventory;
	}
}