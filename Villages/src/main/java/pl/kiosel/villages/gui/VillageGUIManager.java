package pl.kiosel.villages.gui;

import org.bukkit.entity.Player;
import pl.kiosel.core.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.gui.village.*;

import java.util.UUID;

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
			case VILLAGE:
				gui = main;
				break;
			case SETTINGS:
				gui = new SettingsInventory(plugin, this, village, player, main);
				break;
			case BANK:
				gui = new BankInventory(plugin, this, village, player, main);
				break;
			case REMOVE:
				gui = new RemoveInventory(plugin, this, village, player, main);
				break;
			case UPGRADE:
				gui = new UpgradeInventory(plugin, this, village, player, main);
				break;
			case EFFECTS:
				gui = new EffectsInventory(plugin, this, village, player, main);
				break;
			case RESIDENT:
				gui = new ResidentInventory(plugin, this, village, player, main);
				break;
			case STORE:
				gui = new StoreInventory(plugin, this, village, player, main);
				break;
			case STORAGE:
				gui = new StorageInventory(plugin, this, village, player, main);
				break;
			case TAG:
				SettingsInventory settings = new SettingsInventory(plugin, this, village, player, main);
				gui = new TagInventory(plugin, village, player, settings);
				break;
			default:
				gui = main;
		}
		plugin.getGuiManager().showGUI(player, gui);
	}

	public void openMemberSettings(Player player, UUID member) {
		Village village = getVillage(player);
		if (village == null) {
			return;
		}
		VillageInventory main = new VillageInventory(plugin, this, village, player);
		ResidentInventory residents = new ResidentInventory(plugin, this, village, player, main);
		plugin.getGuiManager().showGUI(player,
				new MemberSettingsInventory(plugin, this, village, player, residents, member));
	}

	public void openMemberRemove(Player player, UUID member) {
		Village village = getVillage(player);
		if (village == null) {
			return;
		}
		VillageInventory main = new VillageInventory(plugin, this, village, player);
		ResidentInventory residents = new ResidentInventory(plugin, this, village, player, main);
		MemberSettingsInventory settings = new MemberSettingsInventory(
				plugin, this, village, player, residents, member);
		plugin.getGuiManager().showGUI(player,
				new MemberRemoveInventory(plugin, this, village, player, settings, member));
	}

	private Village getVillage(Player player) {
		User user = plugin.getUserManager().findByUuid(player.getUniqueId()).orNull();
		return user == null ? null : user.getPresentVillage();
	}
}
