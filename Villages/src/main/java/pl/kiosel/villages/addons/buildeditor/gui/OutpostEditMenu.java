package pl.kiosel.villages.addons.buildeditor.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.buildeditor.VillageBuildEditorManager;
import pl.kiosel.villages.gui.Item;

import java.util.ArrayList;
import java.util.List;

public class OutpostEditMenu extends Gui {

	public OutpostEditMenu(AdvancedVillages plugin, Player player, VillageBuildEditorManager manager, Gui parent) {
		setRows(4);
		setParent(parent);
		setTitle(manager.guiTitle());
		setDefaultItem(Item.blank(Item.Blank.BLACK));

		if (plugin.getOutpostLevelManager() != null) {
			for (Integer level : plugin.getOutpostLevelManager().getLevels().keySet()) {
				if (level < 1 || level > 5) continue;
				int column = level + 2;
				boolean schematic = manager.getOutpostSchematicFile(level).isFile();

				setButton(2, column, levelItem(level, schematic), event -> {
					exit();
					if (event.getClickType().isRightClick()) {
						manager.startOutpostSession(player, level);
					} else {
						manager.openOutpostLevelSettings(player, level);
					}
				});
			}
		}

		int nextLevel = manager.getNextAvailableOutpostLevel();
		if (nextLevel <= 5) {
			boolean pending = manager.getOutpostSchematicFile(nextLevel).isFile();
			setButton(3, 5, Item.create(pending ? Material.WRITABLE_BOOK : Material.LIME_DYE,
					pending ? "&eFinish outpost level &f" + nextLevel : "&aCreate outpost level &f" + nextLevel,
					pending ? List.of("&7Click to set requirements.")
							: List.of("&7Empty 7x7x6 building area.", "&7The core is placed automatically.")), event -> {
				exit();
				manager.startNewOutpostLevel(player, nextLevel);
			});
		}

		setButton(4, 9, Item.blank(Item.Blank.BACK),
				event -> event.getManager().openGUI(player, parent));
	}

	private ItemStack levelItem(int level, boolean schematic) {
		Material[] materials = {
				Material.WHITE_WOOL, Material.RED_WOOL, Material.GREEN_WOOL, Material.BLUE_WOOL, Material.PURPLE_WOOL
		};
		String status = schematic ? "&aSchematic" : level <= 3 ? "&eInternal" : "&cMissing";
		List<String> lore = new ArrayList<>();
		lore.add("&eLeft click &7- level settings");
		lore.add("&eRight click &7- edit 7x7x6 build");
		lore.add("");
		lore.add("&7Structure: " + status);
		return Item.create(materials[level - 1], "&aOutpost level &f" + level, lore);
	}
}
