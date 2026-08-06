package pl.kiosel.villages.gui;

import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.gui.village.*;
import pl.kiosel.villages.data.village.Village;

import java.util.UUID;

public class VillageGUIManager {

    private final AdvancedVillages plugin;
	private final BankInventory bankInventory;
	private final EffectsInventory effectsInventory;
	private final RemoveInventory removeInventory;
	private final ResidentInventory residentInventory;
	private final SettingsInventory settingsInventory;
	private final StoreInventory storeInventory;
	private final UpgradeInventory upgradeInventory;
	private final VillageInventory villageInventory;
	private final TagInventory tagInventory;
	private final MemberSettingsInventory memberSettingsInventory;
	private final StorageInventory storageInventory;
	private final MemberRemoveInventory memberRemoveInventory;

    public VillageGUIManager(AdvancedVillages plugin) {
        this.plugin = plugin;
		this.bankInventory = new BankInventory();
		this.effectsInventory = new EffectsInventory();
		this.removeInventory = new RemoveInventory();
		this.residentInventory = new ResidentInventory();
		this.settingsInventory = new SettingsInventory();
		this.storeInventory = new StoreInventory(plugin);
		this.upgradeInventory = new UpgradeInventory();
		this.storageInventory = new StorageInventory();
		this.villageInventory = new VillageInventory();
		this.memberSettingsInventory = new MemberSettingsInventory(plugin);
		this.tagInventory = new TagInventory();
		this.memberRemoveInventory = new MemberRemoveInventory();
    }

    public void openGui(Village village, Player player, GUIS GUIS) {
        switch (GUIS) {
            case VILLAGE:
                player.openInventory(villageInventory.getInventory(village, player));
                break;
            case SETTINGS:
                player.openInventory(settingsInventory.getInventory(village, player));
                break;
            case BANK:
                player.openInventory(bankInventory.getInventory(village, player));
                break;
			case REMOVE:
				player.openInventory(removeInventory.getInventory(village, player));
				break;
			case UPGRADE:
				player.openInventory(upgradeInventory.getInventory(village, player));
				break;
			case EFFECTS:
				player.openInventory(effectsInventory.getInventory(village, player));
				break;
			case RESIDENT:
				player.openInventory(residentInventory.getInventory(village, player));
				break;
			case STORE:
				player.openInventory(storeInventory.getInventory(village, player));
				break;
			case STORAGE:
				player.openInventory(storageInventory.getInventory(village, player));
				break;
			case TAG:
				tagInventory.openInventory(plugin, village).open(player);
				break;
        }
    }

	public void openMemberSettings(Player player, UUID member) {
		player.openInventory(memberSettingsInventory.getInventory(player, member));
	}

	public void openMemberRemove(Player player, UUID member) {
		player.openInventory(memberRemoveInventory.getInventory(player, member));
	}
}