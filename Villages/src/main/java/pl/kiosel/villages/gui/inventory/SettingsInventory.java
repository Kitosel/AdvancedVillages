package pl.kiosel.villages.gui.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import pl.kiosel.villages.config.GuiConfig;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.common.Item;
import pl.kiosel.villages.gui.VillageMenu;
import pl.kiosel.common.utils.Utils;
import pl.kiosel.villages.village.Village;

public class SettingsInventory extends VillageMenu {

	@Override
	public Inventory getInventory(Village village, Player player) {
		Inventory inventory = Bukkit.createInventory(player, GUIS.SETTINGS.getSize(), GUIS.SETTINGS.getName());
		for(int x = 0; x < 9; ++x) { inventory.setItem(x, blank(Item.Blank.WHITE)); }
		inventory.setItem(4, blank(Item.Blank.BACK));

		if (village == null) return inventory;

		inventory.setItem(19, create(Material.FEATHER, 1, GuiConfig.guis_village_setting_teleport, replaceWith(village, GuiConfig.guis_village_setting_teleport_lore), false));
		inventory.setItem(21, create(Material.MOJANG_BANNER_PATTERN, 1, GuiConfig.guis_village_setting_tag, replaceWith(village, GuiConfig.guis_village_setting_tag_lore), false));
		inventory.setItem(23, create(Material.TNT, 1,
				GuiConfig.guis_village_setting_tnt, replaceWith(village, GuiConfig.guis_village_setting_tnt_lore), village.getVillageSettings().isTnt()));
		inventory.setItem(25, create(Material.NETHERITE_SWORD, 1,
				GuiConfig.guis_village_setting_pvp, replaceWith(village, GuiConfig.guis_village_setting_pvp_lore), village.getVillageSettings().isPvp()));
		inventory.setItem(31, create(Material.ORANGE_BED, 1, GuiConfig.guis_village_setting_delete, GuiConfig.guis_village_setting_delete_lore, false));

		return inventory;
	}
}
