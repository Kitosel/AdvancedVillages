package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.core.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiConfig;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.level.Level;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Permission;
import pl.kiosel.villages.enums.Upgrade;
import pl.kiosel.villages.events.VillageUpgradeEvent;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

import java.util.ArrayList;
import java.util.List;

public final class UpgradeInventory extends VillageMenu {

	private static final int INFO_ROW_WIDTH = 9;
	private static final int SPACED_INFO_ROW_START = 18;
	private static final int COMPACT_INFO_ROW_START = 27;
	private static final int MAX_SPACED_ITEMS = 4;

	public UpgradeInventory(AdvancedVillages plugin, VillageGUIManager menus, Village village,
	                        Player player, Gui parent) {
		super(plugin, menus, village, player, GUIS.UPGRADE, parent);
		addBackButton(4);

		int currentLevel = village.getLevel().getLevel();
		int highestLevel = plugin.getLevelManager().getHighestLevel().getLevel();
		int transitionCount = Math.min(INFO_ROW_WIDTH, Math.max(0, highestLevel - 1));
		int[] infoSlots = createCenteredInfoSlots(transitionCount);
		for (int index = 0; index < transitionCount; index++) {
			int fromLevel = index + 1;
			List<String> lore = replaceWithLevelInfo(GuiConfig.guis_village_upgrade_info_lore, fromLevel);
			boolean completed = currentLevel > fromLevel;
			ItemStack info = Item.create(
					Material.PAPER,
					completed ? GuiConfig.guis_village_upgrade_info_button_upgraded
							: GuiConfig.guis_village_upgrade_info_button,
					lore,
					completed
			);
			setItem(infoSlots[index], info);
		}

		Level next = plugin.getLevelManager().getLevel(currentLevel + 1);
		if (next == null) {
			setItem(13, Item.create(Material.BARRIER, "&cMaksymalny poziom",
					List.of("&7Wioska osiągnęła najwyższy", "&7poziom ustawiony w levels.yml.")));
		} else {
			setButton(13, Item.create(
					Upgrade.getMaterialByLevel(next.getLevel()),
					GuiConfig.guis_village_upgrade_button,
					formatUpgradeLore(GuiConfig.guis_village_upgrade_button_lore, currentLevel, next)
			), event -> upgrade(next));
		}
	}

	private void upgrade(Level nextLevel) {
		if (!hasPermission(Permission.UPGRADE)) {
			return;
		}
		if (!plugin.getUpgradeManager().canPasteLevel(nextLevel.getLevel())) {
			plugin.getLocale().getMessage(Lang.BUILD_EDITOR_SCHEMATIC_MISSING.getPath())
					.processPlaceholder("schematic", "Turret" + nextLevel.getLevel() + ".schem")
					.sendPrefixedMessage(viewer);
			exit();
			return;
		}

		int currentLevel = village.getLevel().getLevel();
		VillageUpgradeEvent upgradeEvent = new VillageUpgradeEvent(
				village,
				viewer,
				Upgrade.getByLevel(currentLevel),
				Upgrade.getByLevel(nextLevel.getLevel()),
				nextLevel.getCostEconomy()
		);
		plugin.getServer().getPluginManager().callEvent(upgradeEvent);
		if (upgradeEvent.isCancelled() || !plugin.getUpgradeManager().canUpgrade(viewer, nextLevel)) {
			return;
		}

		plugin.getLocale().getMessage(Lang.VILLAGE_UPGRADE.getPath()).sendPrefixedMessage(viewer);
		plugin.getUpgradeManager().upgradeVillage(village);
		playSound(Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 1.0f);
		exit();
	}

	private int[] createCenteredInfoSlots(int itemCount) {
		int count = Math.min(INFO_ROW_WIDTH, Math.max(0, itemCount));
		int[] slots = new int[count];
		if (count == 0) {
			return slots;
		}

		boolean spaced = count <= MAX_SPACED_ITEMS;
		int spacing = spaced ? 2 : 1;
		int rowStart = spaced ? SPACED_INFO_ROW_START : COMPACT_INFO_ROW_START;
		int occupiedWidth = 1 + (count - 1) * spacing;
		int firstSlot = rowStart + (INFO_ROW_WIDTH - occupiedWidth) / 2;
		for (int index = 0; index < count; index++) {
			slots[index] = firstSlot + index * spacing;
		}
		return slots;
	}

	private List<String> formatUpgradeLore(List<String> lines, int currentLevel, Level next) {
		List<String> result = new ArrayList<>();
		for (String line : lines) {
			result.add(line
					.replace("%village_level%", Integer.toString(currentLevel))
					.replace("%village_next_level%", Integer.toString(next.getLevel()))
					.replace("%village_cost%", Integer.toString(next.getCostEconomy())));
		}
		return result;
	}
}
