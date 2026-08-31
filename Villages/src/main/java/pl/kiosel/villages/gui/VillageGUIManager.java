package pl.kiosel.villages.gui;

import org.bukkit.entity.Player;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.gui.village.*;

public class VillageGUIManager {

	private final AdvancedVillages plugin;

	public VillageGUIManager(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public void openGui(Village village, Player player, GUIS type) {
		if (village == null || player == null) {
			return;
		}

		VillageInventory main = new VillageInventory(plugin, this, village, player);
		Gui gui;
		switch (type) {
			case SETTINGS:
				gui = new SettingsInventory(plugin, this, village, player, main);
				break;
			case BANK:
				gui = new BankInventory(plugin, this, village, player, main);
				break;
			case REMOVE:
				gui = new RemoveInventory(plugin, village, player, main);
				break;
			case UPGRADE:
				gui = new UpgradeInventory(plugin, this, village, player, main);
				break;
			case EFFECTS:
				gui = new EffectsInventory(plugin, this, village, player, main);
				break;
			case QUESTS:
				gui = new QuestInventory(plugin, this, village, player, main);
				break;
			case LOGS:
				gui = new VillageLogInventory(plugin, this, village, player, main);
				break;
			case DIPLOMACY:
				gui = new DiplomacyInventory(plugin, this, village, player, main);
				break;
			case DEVELOPMENT:
				gui = new DevelopmentInventory(plugin, this, village, player, main);
				break;
			case RESIDENT:
				gui = new ResidentInventory(plugin, this, village, player, main);
				break;
			case STORE:
				gui = new StoreInventory(plugin, this, village, player, main);
				break;
			case TAG:
				SettingsInventory settings = new SettingsInventory(plugin, this, village, player, main);
				gui = new TagInventory(plugin, village, player, settings);
				break;
			default:
				gui = main;
		}
		plugin.getGuiManager().openGUI(player, gui);
	}
}
