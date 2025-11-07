package pl.kiosel.villages.gui.village;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import pl.kiosel.villages.config.GuiConfig;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.gui.VillageMenu;
import pl.kiosel.villages.data.village.Village;

public class VillageInventory extends VillageMenu {

	@Override
	public Inventory getInventory(Village village, Player player) {
		Inventory inventory = Bukkit.createInventory(player, GUIS.VILLAGE.getSize(), GUIS.VILLAGE.getName());
		for(int x = 0; x < 9; ++x) { inventory.setItem(x, blank(Item.Blank.WHITE)); }
		inventory.setItem(4, create(Material.NOTE_BLOCK, GuiConfig.guis_village_settings, GuiConfig.guis_village_settings_lore));
		inventory.setItem(35, blank(Item.Blank.EXIT));

		inventory.setItem(11, create(Material.PLAYER_HEAD, GuiConfig.guis_village_members, GuiConfig.guis_village_members_lore));
//		inventory.setItem(13, create(Material.CHEST, GuiConfig.guis_village_storage, GuiConfig.guis_village_storage_lore));
		if (village.getLevel().getLevel() > 1) {
			inventory.setItem(15, create(Material.SUNFLOWER, GuiConfig.guis_village_bank, GuiConfig.guis_village_bank_lore));
		} else {
			inventory.setItem(15, create(Material.BARRIER, "&cX " + GuiConfig.guis_village_bank, GuiConfig.guis_village_bank_lore));
		}
		inventory.setItem(21, create(Material.NETHER_STAR, GuiConfig.guis_village_store, GuiConfig.guis_village_store_lore));
		if (village.getLevel().getLevel() > 3) {
			inventory.setItem(23, create(Material.SPLASH_POTION, GuiConfig.guis_village_effects, GuiConfig.guis_village_effects_lore));
		} else {
			inventory.setItem(23, create(Material.BARRIER, "&cX " + GuiConfig.guis_village_effects, GuiConfig.guis_village_effects_lore));
		}
		inventory.setItem(31, create(Material.DIAMOND, GuiConfig.guis_village_upgrade, GuiConfig.guis_village_upgrade_lore));
		return inventory;
	}
}
