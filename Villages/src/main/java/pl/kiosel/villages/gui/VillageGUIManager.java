package pl.kiosel.villages.gui;

import org.bukkit.entity.Player;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.gui.GUIS;
import pl.kiosel.villages.data.outpost.Outpost;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.gui.outpost.OutpostGUI;
import pl.kiosel.villages.gui.outpost.OutpostSettingsGUI;
import pl.kiosel.villages.gui.outpost.OutpostUpgradeGUI;
import pl.kiosel.villages.gui.village.*;

public class VillageGUIManager {

	private final AdvancedVillages plugin;

	public VillageGUIManager(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public void openOutpostGui(Outpost outpost, Player player, GUIS type) {
		if (outpost == null || player == null) {
			return;
		}
		OutpostGUI main = new OutpostGUI(plugin, this, outpost, player);
		Gui gui;
		switch (type) {
			case OUTPOST_SETTINGS:
				gui = new OutpostSettingsGUI(plugin, this, outpost, player, main);
				break;
			case OUTPOST_UPGRADE:
				gui = new OutpostUpgradeGUI(plugin, this, outpost, player, main);
				break;
			default:
				gui = main;
		}
		plugin.getGuiManager().openGUI(player, gui);
	}

	public void openVillageGui(Village village, Player player, GUIS type) {
		if (village == null || player == null) {
			return;
		}

		VillageGUI main = new VillageGUI(plugin, this, village, player);
		Gui gui;
		switch (type) {
			case SETTINGS:
				gui = new SettingsGUI(plugin, this, village, player, main);
				break;
			case BANK:
				gui = new BankGUI(plugin, this, village, player, main);
				break;
			case REMOVE:
				gui = new RemoveGUI(plugin, village, player, main);
				break;
			case UPGRADE:
				gui = new UpgradeGUI(plugin, this, village, player, main);
				break;
			case EFFECTS:
				gui = new EffectsGUI(plugin, this, village, player, main);
				break;
			case UPKEEP:
				gui = new UpkeepGUI(plugin, this, village, player, main);
				break;
			case QUESTS:
				gui = new QuestGUI(plugin, this, village, player, main);
				break;
			case DIPLOMACY:
				gui = new DiplomacyGUI(plugin, this, village, player, main);
				break;
			case DEVELOPMENT:
				gui = new DevelopmentGUI(plugin, this, village, player, main);
				break;
			case SPECIALIZATIONS:
				gui = new SpecializationGUI(plugin, this, village, player, main);
				break;
			case RESIDENT:
				gui = new ResidentGUI(plugin, this, village, player, main);
				break;
			case STORE:
				gui = new StoreGUI(plugin, this, village, player, main);
				break;
			case TAG:
				SettingsGUI settings = new SettingsGUI(plugin, this, village, player, main);
				gui = new TagGUI(plugin, village, player, settings);
				break;
			default:
				gui = main;
		}
		plugin.getGuiManager().openGUI(player, gui);
	}
}
