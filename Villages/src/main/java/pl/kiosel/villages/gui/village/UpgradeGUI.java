package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.rosacore.compatibility.ZSound;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.api.events.VillageUpgradeEvent;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.config.Settings;
import pl.kiosel.villages.config.gui.GUIS;
import pl.kiosel.villages.config.gui.GuiItemConfig;
import pl.kiosel.villages.data.user.VillagePermission;
import pl.kiosel.villages.data.village.Upgrade;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.features.logs.VillageLogType;
import pl.kiosel.villages.data.village.level.Level;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;
import pl.kiosel.villages.manager.VillageUtils;

import java.util.List;

public final class UpgradeGUI extends VillageMenu {

	private static final int INFO_ROW_WIDTH = 9;
	private static final int INFO_ROW_START = 27;
	private static final int MAX_SPACED_ITEMS = 4;

	public UpgradeGUI(AdvancedVillages plugin, VillageGUIManager menus, Village village,
					  Player player, Gui parent) {
		super(plugin, menus, village, player, GUIS.UPGRADE, parent);
		addBackButton();

		int currentLevel = village.getLevel().getLevel();
		int highestLevel = plugin.getLevelManager().getHighestLevel().getLevel();
		int transitionCount = Math.min(INFO_ROW_WIDTH, Math.max(0, highestLevel - 1));
		int[] infoSlots = createCenteredInfoSlots(transitionCount);

		GuiItemConfig infoConfig = plugin.getGuiSettings().item(guiType,
				"upgrade.info", 27, Material.PAPER,
				"&cTo upgrade:", List.of(
						"&7Level &c%village_level% &f-> &e%village_next_level%",
						"&7Size: &c%village_size% &7-> &e%village_next_size%",
						"&eCost: &a%village_cost%"));

		for (int index = 0; index < transitionCount; index++) {
			if (!infoConfig.isEnabled() || infoSlots[index] >= menuConfig.getSize()) continue;

			int fromLevel = index + 1;
			List<String> lore = replaceWithLevelInfo(infoConfig.getLore(), fromLevel);

			boolean completed = currentLevel > fromLevel;
			String completedName = plugin.getGuiSettings().text(guiType,
					"upgrade.info.name-upgraded", "&bUpgraded");

			ItemStack info = infoConfig.createItem(
					completed ? completedName : infoConfig.getName(), lore,
					infoConfig.isGlow() || completed);

			setItem(infoSlots[index], info);
		}

		Level next = plugin.getLevelManager().getLevel(currentLevel + 1);
		if (next == null) {
			GuiItemConfig maxLevel = plugin.getGuiSettings().item(guiType,
					"upgrade.max-level", 22, Material.BARRIER,
					"&cMaximum level", List.of("&7The village has reached the ",
							"&7highest level set in levels.yml."));

			if (maxLevel.isEnabled()) {
				setItem(maxLevel.getSlot(), maxLevel.createItem(maxLevel.getName(),
						VillageUtils.replaceWithList(village, maxLevel.getLore())));
			}
		} else {
			GuiItemConfig upgrade = plugin.getGuiSettings().item(guiType,
					"upgrade.upgrade-button", 22,
					Upgrade.getMaterialByLevel(next.getLevel()), "&aUpgrade village",
					List.of("&7Actual level: &e%village_level%", "&7Cost: &a%village_cost%",
							"&cClick to &6Upgrade"));
			if (upgrade.isEnabled()) {
				setButton(upgrade.getSlot(), upgrade.createItem(upgrade.getName(),
						VillageUtils.replaceWithList(village, upgrade.getLore())),
						event -> upgrade(next));
			}
		}
	}

	private void upgrade(Level nextLevel) {
		if (!hasPermission(VillagePermission.UPGRADE)) {
			return;
		}
		if (!plugin.getUpgradeManager().canPasteLevel(nextLevel.getLevel())) {
			getVillageMessages().get(Lang.BUILD_EDITOR_SCHEMATIC_MISSING)
					.with("schematic", "Turret" + nextLevel.getLevel() + ".schem")
					.sendPrefixed(viewer);
			exit();
			return;
		}
		if (!village.isTag() && !Settings.VILLAGE_UPGRADE_NO_TAG.getBoolean()) {
			getVillageMessages().get(Lang.VILLAGE_MUST_HAVE_TAG).sendPrefixed(viewer);
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

		getVillageMessages().get(Lang.VILLAGE_UPGRADE).sendPrefixed(viewer);
		if (!plugin.getUpgradeManager().upgradeVillage(village)) {
			return;
		}
		plugin.getLogManager().record(village, VillageLogType.VILLAGE_UPGRADE, viewer,
				"level", nextLevel.getLevel());
		playSound(ZSound.ENTITY_PLAYER_LEVELUP, 0.2f, 1.0f);
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
		int occupiedWidth = 1 + (count - 1) * spacing;
		int firstSlot = INFO_ROW_START + (INFO_ROW_WIDTH - occupiedWidth) / 2;
		for (int index = 0; index < count; index++) {
			slots[index] = firstSlot + index * spacing;
		}
		return slots;
	}
}
