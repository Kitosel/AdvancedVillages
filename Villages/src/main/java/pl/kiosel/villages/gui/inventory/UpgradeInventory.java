package pl.kiosel.villages.gui.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.villages.config.GuiConfig;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.common.Item;
import pl.kiosel.villages.gui.VillageMenu;
import pl.kiosel.villages.village.Village;

import java.util.List;

public class UpgradeInventory extends VillageMenu {

	@Override
	public Inventory getInventory(Village village, Player player) {
		Inventory inventory = Bukkit.createInventory(player, GUIS.UPGRADE.getSize(), GUIS.UPGRADE.getName());
		for(int x = 0; x < 9; ++x) { inventory.setItem(x, blank(Item.Blank.WHITE)); }
		inventory.setItem(4, blank(Item.Blank.BACK));

		List<String> upgrade_list = replaceWith(village, GuiConfig.guis_village_upgrade_button_lore);

		List<String> info_list = replaceWithLevelInfo(GuiConfig.guis_village_upgrade_info_lore, 1);
		List<String> info_list2 = replaceWithLevelInfo(GuiConfig.guis_village_upgrade_info_lore, 2);
		List<String> info_list3 = replaceWithLevelInfo(GuiConfig.guis_village_upgrade_info_lore, 3);
		List<String> info_list4 = replaceWithLevelInfo(GuiConfig.guis_village_upgrade_info_lore, 4);

		ItemStack upgrade = create(getMaterialUpgrade(village.getLevel()), GuiConfig.guis_village_upgrade_button, upgrade_list);
		ItemStack info1 = create(Material.PAPER, GuiConfig.guis_village_upgrade_info_button, info_list);
		ItemStack info2 = create(Material.PAPER, GuiConfig.guis_village_upgrade_info_button, info_list2);
		ItemStack info3 = create(Material.PAPER, GuiConfig.guis_village_upgrade_info_button, info_list3);
		ItemStack info4 = create(Material.PAPER, GuiConfig.guis_village_upgrade_info_button, info_list4);

		ItemStack info1en = create(Material.PAPER, GuiConfig.guis_village_upgrade_info_button_upgraded, info_list, true);
		ItemStack info2en = create(Material.PAPER, GuiConfig.guis_village_upgrade_info_button_upgraded, info_list2, true);
		ItemStack info3en = create(Material.PAPER, GuiConfig.guis_village_upgrade_info_button_upgraded, info_list3, true);
		ItemStack info4en = create(Material.PAPER, GuiConfig.guis_village_upgrade_info_button_upgraded, info_list4, true);

		switch (village.getLevel()) {
			case 1:
				inventory.setItem(28, info1);
				inventory.setItem(30, info2);
				inventory.setItem(32, info3);
				inventory.setItem(34, info4);
				break;
			case 2:
				inventory.setItem(28, info1en);
				inventory.setItem(30, info2);
				inventory.setItem(32, info3);
				inventory.setItem(34, info4);
				break;
			case 3:
				inventory.setItem(28, info1en);
				inventory.setItem(30, info2en);
				inventory.setItem(32, info3);
				inventory.setItem(34, info4);
				break;
			case 4:
				inventory.setItem(28, info1en);
				inventory.setItem(30, info2en);
				inventory.setItem(32, info3en);
				inventory.setItem(34, info4);
				break;
			case 5:
				inventory.setItem(19, info1en);
				inventory.setItem(21, info2en);
				inventory.setItem(23, info3en);
				inventory.setItem(25, info4en);
				break;
		}

		if (village.getLevel() != 5) {
			inventory.setItem(13, upgrade);
		}
		return inventory;
	}
}
