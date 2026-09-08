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

public final class VillageGUI extends VillageMenu {

	public VillageGUI(AdvancedVillages plugin, VillageGUIManager menus, Village village, Player player) {
		super(plugin, menus, village, player, GUIS.VILLAGE, null);

		GuiItemConfig settings =
				item("guis.village.settings", 4, Material.NOTE_BLOCK, "&lSettings", List.of("&7Manage Village"));
		GuiItemConfig exit =
				item("guis.village.exit", 35, Material.ARROW, "&cExit", List.of());
		GuiItemConfig members =
				item("guis.village.members", 11, Material.PLAYER_HEAD, "&cMembers", List.of("&7Manage members"));
		GuiItemConfig bank =
				item("guis.village.bank", 15, Material.SUNFLOWER, "&6Bank", List.of("&7Village bank"));
		GuiItemConfig store =
				item("guis.village.store", 21, Material.NETHER_STAR, "&3Store", List.of("&7Village store"));
		GuiItemConfig effects =
				item("guis.village.effects", 23, Material.SPLASH_POTION,"&dEffects", List.of("&7Add effects for", "&7Members of village"));
		GuiItemConfig upgrade =
				item("guis.village.upgrade", 31, Material.DIAMOND,"&bUpgrade", List.of("&7Upgrade village"));

		/* IN DEVELOPMENT */
		GuiItemConfig quests =
				item("guis.village.quests", 13, Material.WRITABLE_BOOK, "&eVillage quests", List.of("&7Shared daily and weekly quests"));
		GuiItemConfig specialist =
				item("guis.village.specializ", 27, Material.KNOWLEDGE_BOOK,
						"&aSpecializations", List.of("&7Choose your personal village bonus"));
		GuiItemConfig diplomacy =
				item("guis.village.diplomacy", 29, Material.IRON_SWORD,
						"&bDiplomacy", List.of("&7Alliances and wars"));
		GuiItemConfig development =
				item("guis.village.development", 33, Material.AMETHYST_SHARD,
						"&dDevelopment tree", List.of("&7Choose permanent village bonuses"));

		if (settings.isEnabled()) {
			setButton(settings.getSlot(), settings.createItem(), event -> openFromMain(GUIS.SETTINGS));
		}
		if (exit.isEnabled()) setButton(exit.getSlot(), exit.createItem(), event -> {
			playSound(ZSound.BLOCK_ANVIL_BREAK, 0.1f, 2.0f);
			event.getGui().exit();
		});

		if (members.isEnabled()) {
			setButton(members.getSlot(), members.createItem(), event -> openFromMain(GUIS.RESIDENT));
		}

		if (bank.isEnabled()) {
			if (village.getLevel().getLevel() > 1) {
				setButton(bank.getSlot(), bank.createItem(), event -> openFromMain(GUIS.BANK));
			} else {
				setItem(bank.getSlot(), Item.create(Material.BARRIER, "&cX " + bank.getName(), bank.getLore()));
			}
		}

		if (store.isEnabled()) {
			setButton(store.getSlot(), store.createItem(), event -> openFromMain(GUIS.STORE));
		}

		if (effects.isEnabled()) {
			if (village.getLevel().getLevel() > 3) {
				setButton(effects.getSlot(), effects.createItem(), event -> openFromMain(GUIS.EFFECTS));
			} else {
				setItem(effects.getSlot(), Item.create(Material.BARRIER, "&cX " + effects.getName(), effects.getLore()));
			}
		}

		if (upgrade.isEnabled()) {
			setButton(upgrade.getSlot(), upgrade.createItem(), event -> openFromMain(GUIS.UPGRADE));
		}

		/* IN DEVELOPMENT */
		if (plugin.isDev()) {
			if (quests.isEnabled() && plugin.getQuestManager().isEnabled()) {
				setButton(quests.getSlot(), quests.createItem(), event -> openFromMain(GUIS.QUESTS));
			}

			if (specialist.isEnabled() && plugin.getSpecializationManager().isEnabled()) {
				setButton(specialist.getSlot(), specialist.createItem(), event -> openFromMain(GUIS.SPECIALIZATIONS));
			}

			if (diplomacy.isEnabled() && plugin.getDiplomacyManager().isEnabled()) {
				setButton(diplomacy.getSlot(), diplomacy.createItem(), event -> openFromMain(GUIS.DIPLOMACY));
			}

			if (development.isEnabled() && plugin.getDevelopmentManager().isEnabled()) {
				setButton(development.getSlot(), development.createItem(), event -> openFromMain(GUIS.DEVELOPMENT));
			}
		}
	}

	private GuiItemConfig item(String path, int slot, Material material, String name, List<String> lore) {
		return item(GUIS.VILLAGE, path, slot, material, name, lore);
	}
}
