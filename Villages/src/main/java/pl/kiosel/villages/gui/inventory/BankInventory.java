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

import java.util.List;

public class BankInventory extends VillageMenu {

	@Override
	public Inventory getInventory(Village village, Player player) {
		Inventory inventory = Bukkit.createInventory(player, GUIS.BANK.getSize(), GUIS.BANK.getName());
		for(int x = 0; x < 9; ++x) { inventory.setItem(x, blank(Item.Blank.WHITE)); }
		inventory.setItem(4, blank(Item.Blank.BACK));

		if (village == null) return inventory;

		inventory.setItem(13, create(Material.PAPER, 1, GuiConfig.guis_village_bank_balance.replace("%village_balance%", village.getBank()+"")));
		inventory.setItem(21, create(Material.GREEN_CONCRETE, 5, GuiConfig.guis_village_bank_add, List.of("&a&l+5")));
		inventory.setItem(22, create(Material.GREEN_CONCRETE, 10, GuiConfig.guis_village_bank_add, List.of("&a&l+10")));
		inventory.setItem(23, create(Material.GREEN_CONCRETE, 60, GuiConfig.guis_village_bank_add, List.of("&a&l+100")));

		inventory.setItem(30,create(Material.RED_CONCRETE, 5, GuiConfig.guis_village_bank_remove, List.of("&c&l-5")));
		inventory.setItem(31, create(Material.RED_CONCRETE, 10, GuiConfig.guis_village_bank_remove, List.of("&c&l-10")));
		inventory.setItem(32, create(Material.RED_CONCRETE, 60, GuiConfig.guis_village_bank_remove, List.of("&c&l-100")));
		inventory.setItem(35, blank(Item.Blank.BACK));
		return inventory;
	}
}