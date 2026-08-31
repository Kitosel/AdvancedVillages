package pl.kiosel.villages.addons.buildeditor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.Upgrade;
import pl.kiosel.villages.gui.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static pl.kiosel.rosacore.utils.ColorUtils.tl;

public final class EditorMenu extends Gui {

	public EditorMenu(AdvancedVillages plugin, Player player, VillageBuildEditorManager manager) {
		setRows(4);
		setTitle(tl(manager.getConfig().getString("menu.title", "&8Village building editor")));
		setDefaultItem(Item.create(Material.BLACK_STAINED_GLASS_PANE, " "));

		for (Integer level : plugin.getLevelManager().getLevels().keySet()) {
			if (level < 1 || level > VillageBuildEditorManager.MAX_LEVEL) {
				continue;
			}
			int row = level <= 5 ? 2 : 3;
			int column = level <= 5 ? level + 2 : (level - 5) + 2;
			boolean schematicExists = manager.getSchematicFile(level).isFile();
			setButton(row, column, createLevelItem(Upgrade.getMaterialByLevel(level), level, schematicExists, manager), event -> {
				player.closeInventory();
				if (event.getClickType().isRightClick()) {
					manager.startSession(player, level);
				} else {
					manager.openLevelSettings(player, level);
				}
			});
		}

		int nextLevel = manager.getNextAvailableLevel();
		if (nextLevel <= VillageBuildEditorManager.MAX_LEVEL) {
			boolean pendingSetup = manager.getSchematicFile(nextLevel).isFile();
			setButton(4, 5, createNewLevelItem(nextLevel, pendingSetup, manager), event -> {
				player.closeInventory();
				manager.startNewLevel(player, nextLevel);
			});
		}

		setButton(4, 9, Item.create(Material.SPECTRAL_ARROW,
				plugin.getGuiSettings().text("guis.common.exit.name", "&cExit")),
				event -> player.closeInventory());
	}

	private ItemStack createLevelItem(Material material, int level, boolean exists, VillageBuildEditorManager manager) {
		String name = manager.getConfig().getString("menu.level-name", "&aLevel &f%level%")
				.replace("%level%", String.valueOf(level));
		List<String> configuredLore = manager.getConfig().getStringList("menu.level-lore");
		if (configuredLore.isEmpty()) {
			configuredLore = Arrays.asList(
					"&eLeft click &7- level settings",
					"&eRight click &7- edit build",
					"",
					"&7Schematic: %status%"
			);
		}
		List<String> lore = replace(configuredLore, level, exists ? "&aAvailable" : "&cFile not found");
		return Item.create(material, name, lore);
	}

	private ItemStack createNewLevelItem(int level, boolean pendingSetup, VillageBuildEditorManager manager) {
		String key = pendingSetup ? "menu.resume-name" : "menu.create-name";
		String fallback = pendingSetup ? "&eFinish level &f%level%" : "&aCreate level &f%level%";
		String name = manager.getConfig().getString(key, fallback).replace("%level%", String.valueOf(level));
		List<String> lore = manager.getConfig().getStringList(pendingSetup ? "menu.resume-lore" : "menu.create-lore");
		if (lore.isEmpty()) {
			lore = pendingSetup
					? List.of("&7Schematic is saved.", "&7Click to set requirements.")
					: List.of("&7It creates an empty space", "&7with a stone base.");
		}
		return Item.create(pendingSetup ? Material.WRITABLE_BOOK : Material.LIME_DYE, name, replace(lore, level, ""));
	}

	private List<String> replace(List<String> lines, int level, String status) {
		List<String> result = new ArrayList<>();
		for (String line : lines) {
			result.add(tl(line.replace("%level%", String.valueOf(level)).replace("%status%", status)));
		}
		return result;
	}
}
