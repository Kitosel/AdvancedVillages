package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiConfig;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

public final class VillageInventory extends VillageMenu {

	public VillageInventory(AdvancedVillages plugin, VillageGUIManager menus, Village village, Player player) {
		super(plugin, menus, village, player, GUIS.VILLAGE, null);

		setButton(4, Item.create(Material.NOTE_BLOCK, GuiConfig.guis_village_settings,
				GuiConfig.guis_village_settings_lore), event -> openFromMain(GUIS.SETTINGS));
		setButton(35, Item.blank(Item.Blank.EXIT), event -> {
			playSound(Sound.BLOCK_ANVIL_BREAK, 0.1f, 2.0f);
			event.gui.exit();
		});

		setButton(11, Item.create(Material.PLAYER_HEAD, GuiConfig.guis_village_members,
				GuiConfig.guis_village_members_lore), event -> openFromMain(GUIS.RESIDENT));

		if (village.getLevel().getLevel() > 1) {
			setButton(15, Item.create(Material.SUNFLOWER, GuiConfig.guis_village_bank,
					GuiConfig.guis_village_bank_lore), event -> openFromMain(GUIS.BANK));
		} else {
			setItem(15, Item.create(Material.BARRIER, "&cX " + GuiConfig.guis_village_bank,
					GuiConfig.guis_village_bank_lore));
		}

		setButton(21, Item.create(Material.NETHER_STAR, GuiConfig.guis_village_store,
				GuiConfig.guis_village_store_lore), event -> openFromMain(GUIS.STORE));
		if (village.getLevel().getLevel() > 3) {
			setButton(23, Item.create(Material.SPLASH_POTION, GuiConfig.guis_village_effects,
					GuiConfig.guis_village_effects_lore), event -> openFromMain(GUIS.EFFECTS));
		} else {
			setItem(23, Item.create(Material.BARRIER, "&cX " + GuiConfig.guis_village_effects,
					GuiConfig.guis_village_effects_lore));
		}
		setButton(31, Item.create(Material.DIAMOND, GuiConfig.guis_village_upgrade,
				GuiConfig.guis_village_upgrade_lore), event -> openFromMain(GUIS.UPGRADE));
	}
}
