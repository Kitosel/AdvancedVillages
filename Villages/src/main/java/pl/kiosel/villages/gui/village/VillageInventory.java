package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.compatibility.ZSound;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.gui.GUIS;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

import java.util.List;

public final class VillageInventory extends VillageMenu {

	public VillageInventory(AdvancedVillages plugin, VillageGUIManager menus, Village village, Player player) {
		super(plugin, menus, village, player, GUIS.VILLAGE, null);

		GuiItemConfig settings = item("guis.village.settings", 4, Material.NOTE_BLOCK,
				"&lSettings", List.of("&7Manage Village"));
		if (settings.isEnabled()) {
			setButton(settings.getSlot(), settings.createItem(), event -> openFromMain(GUIS.SETTINGS));
		}
		GuiItemConfig exit = item("guis.village.exit", 35, Material.ARROW,
				"&cExit", List.of());
		if (exit.isEnabled()) setButton(exit.getSlot(), exit.createItem(), event -> {
			playSound(ZSound.BLOCK_ANVIL_BREAK, 0.1f, 2.0f);
			event.getGui().exit();
		});

		GuiItemConfig members = item("guis.village.members", 11, Material.PLAYER_HEAD,
				"&cMembers", List.of("&7Manage members"));
		if (members.isEnabled()) {
			setButton(members.getSlot(), members.createItem(), event -> openFromMain(GUIS.RESIDENT));
		}

		GuiItemConfig quests = item("guis.village.quests", 13, Material.WRITABLE_BOOK,
				"&eVillage quests", List.of("&7Shared daily and weekly quests"));
		if (quests.isEnabled() && plugin.getQuestManager().isEnabled()) {
			setButton(quests.getSlot(), quests.createItem(), event -> openFromMain(GUIS.QUESTS));
		}

		GuiItemConfig diplomacy = item("guis.village.diplomacy", 29, Material.IRON_SWORD,
				"&bDiplomacy", List.of("&7Alliances and wars"));
		if (diplomacy.isEnabled() && plugin.getDiplomacyManager().isEnabled()) {
			setButton(diplomacy.getSlot(), diplomacy.createItem(), event -> openFromMain(GUIS.DIPLOMACY));
		}

		GuiItemConfig development = item("guis.village.development", 33, Material.AMETHYST_SHARD,
				"&dDevelopment tree", List.of("&7Choose permanent village bonuses"));
		if (development.isEnabled() && plugin.getDevelopmentManager().isEnabled()) {
			setButton(development.getSlot(), development.createItem(),
					event -> openFromMain(GUIS.DEVELOPMENT));
		}

		GuiItemConfig bank = item("guis.village.bank", 15, Material.SUNFLOWER,
				"&6Bank", List.of("&7Village bank"));
		if (bank.isEnabled()) {
			if (village.getLevel().getLevel() > 1) {
				setButton(bank.getSlot(), bank.createItem(), event -> openFromMain(GUIS.BANK));
			} else {
				setItem(bank.getSlot(), Item.create(Material.BARRIER, "&cX " + bank.getName(), bank.getLore()));
			}
		}

		GuiItemConfig store = item("guis.village.store", 21, Material.NETHER_STAR,
				"&3Store", List.of("&7Village store"));
		if (store.isEnabled()) {
			setButton(store.getSlot(), store.createItem(), event -> openFromMain(GUIS.STORE));
		}

		GuiItemConfig effects = item("guis.village.effects", 23, Material.SPLASH_POTION,
				"&dEffects", List.of("&7Add effects for", "&7Members of village"));
		if (effects.isEnabled()) {
			if (village.getLevel().getLevel() > 3) {
				setButton(effects.getSlot(), effects.createItem(), event -> openFromMain(GUIS.EFFECTS));
			} else {
				setItem(effects.getSlot(), Item.create(Material.BARRIER, "&cX " + effects.getName(), effects.getLore()));
			}
		}

		GuiItemConfig upgrade = item("guis.village.upgrade", 31, Material.DIAMOND,
				"&bUpgrade", List.of("&7Upgrade village"));
		if (upgrade.isEnabled()) {
			setButton(upgrade.getSlot(), upgrade.createItem(), event -> openFromMain(GUIS.UPGRADE));
		}
	}

	private GuiItemConfig item(String path, int slot, Material material, String name, List<String> lore) {
		return item(GUIS.VILLAGE, path, slot, material, name, lore);
	}
}
