package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.kiosel.dependencies.com.cryptomorin.xseries.XSound;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

public final class VillageInventory extends VillageMenu {

	public VillageInventory(AdvancedVillages plugin, VillageGUIManager menus, Village village, Player player) {
		super(plugin, menus, village, player, GUIS.VILLAGE, null);

		GuiItemConfig settings = item("guis.village.settings", 4, Material.NOTE_BLOCK,
				"&lSettings", java.util.List.of("&7Manage Village"));
		if (settings.isEnabled()) {
			setButton(settings.getSlot(), settings.createItem(), event -> openFromMain(GUIS.SETTINGS));
		}
		GuiItemConfig exit = item("guis.village.exit", 35, Material.ARROW,
				messages.text(pl.kiosel.villages.enums.Lang.EXIT), java.util.Collections.emptyList());
		if (exit.isEnabled()) setButton(exit.getSlot(), exit.createItem(), event -> {
			playSound(XSound.BLOCK_ANVIL_BREAK, 0.1f, 2.0f);
			event.gui.exit();
		});

		GuiItemConfig members = item("guis.village.members", 11, Material.PLAYER_HEAD,
				"&cMembers", java.util.List.of("&7Manage members"));
		if (members.isEnabled()) {
			setButton(members.getSlot(), members.createItem(), event -> openFromMain(GUIS.RESIDENT));
		}

		GuiItemConfig quests = item("guis.village.quests", 13, Material.WRITABLE_BOOK,
				messages.text(pl.kiosel.villages.enums.Lang.QUESTS_GUI_MENU_NAME),
				java.util.List.of(messages.text(pl.kiosel.villages.enums.Lang.QUESTS_GUI_MENU_LORE).split("\n")));
		if (quests.isEnabled() && plugin.getQuestManager().isEnabled()) {
			setButton(quests.getSlot(), quests.createItem(), event -> openFromMain(GUIS.QUESTS));
		}

		GuiItemConfig logs = item("guis.village.logs", 27, Material.BOOK,
				messages.text(pl.kiosel.villages.enums.Lang.LOGS_GUI_MENU_NAME),
				java.util.List.of(messages.text(pl.kiosel.villages.enums.Lang.LOGS_GUI_MENU_LORE).split("\n")));
		if (logs.isEnabled() && plugin.getLogManager().canView(player, village)) {
			setButton(logs.getSlot(), logs.createItem(), event -> openFromMain(GUIS.LOGS));
		}

		GuiItemConfig bank = item("guis.village.bank", 15, Material.SUNFLOWER,
				"&6Bank", java.util.List.of("&7Village bank"));
		if (bank.isEnabled()) {
			if (village.getLevel().getLevel() > 1) {
				setButton(bank.getSlot(), bank.createItem(), event -> openFromMain(GUIS.BANK));
			} else {
				setItem(bank.getSlot(), Item.create(Material.BARRIER, "&cX " + bank.getName(), bank.getLore()));
			}
		}

		GuiItemConfig store = item("guis.village.store", 21, Material.NETHER_STAR,
				"&3Store", java.util.List.of("&7Village store"));
		if (store.isEnabled()) {
			setButton(store.getSlot(), store.createItem(), event -> openFromMain(GUIS.STORE));
		}

		GuiItemConfig effects = item("guis.village.effects", 23, Material.SPLASH_POTION,
				"&dEffects", java.util.List.of("&7Add effects for", "&7Members of village"));
		if (effects.isEnabled()) {
			if (village.getLevel().getLevel() > 3) {
				setButton(effects.getSlot(), effects.createItem(), event -> openFromMain(GUIS.EFFECTS));
			} else {
				setItem(effects.getSlot(), Item.create(Material.BARRIER, "&cX " + effects.getName(), effects.getLore()));
			}
		}

		GuiItemConfig upgrade = item("guis.village.upgrade", 31, Material.DIAMOND,
				"&bUpgrade", java.util.List.of("&7Upgrade village"));
		if (upgrade.isEnabled()) {
			setButton(upgrade.getSlot(), upgrade.createItem(), event -> openFromMain(GUIS.UPGRADE));
		}
	}

	private GuiItemConfig item(String path, int slot, Material material, String name, java.util.List<String> lore) {
		return plugin.getGuiSettings().item(GUIS.VILLAGE, path, slot, material, name, lore);
	}
}
