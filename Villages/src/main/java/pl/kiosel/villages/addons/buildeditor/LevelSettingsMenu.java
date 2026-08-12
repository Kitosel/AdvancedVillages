package pl.kiosel.villages.addons.buildeditor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.core.gui.Gui;
import pl.kiosel.core.gui.GuiUtils;
import pl.kiosel.dependencies.com.cryptomorin.xseries.XMaterial;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.data.village.level.Level;
import pl.kiosel.villages.gui.Item;

import java.util.List;
import java.util.stream.Collectors;

import static pl.kiosel.core.utils.ColorUtils.tl;

final class LevelSettingsMenu extends Gui {

	LevelSettingsMenu(AdvancedVillages plugin, Player player, VillageBuildEditorManager manager, int levelNumber) {
		Level level = plugin.getLevelManager().getLevel(levelNumber);
		setRows(3);
		setTitle(tl(manager.getConfig().getString("settings-menu.title", "&8Level &6%level% &8settings")
				.replace("%level%", String.valueOf(levelNumber))));
		setDefaultItem(GuiUtils.getBorderItem(XMaterial.BLACK_STAINED_GLASS_PANE));

		setButton(1, 1, button(Material.CHEST,
				manager, "settings-menu.items", "&eRequired Items",
				formatMaterials(level)), event -> begin(player, manager, levelNumber, LevelEditorField.ITEMS));
		setButton(1, 3, button(Material.EXPERIENCE_BOTTLE,
				manager, "settings-menu.experience", "&aExperience cost",
				Integer.toString(level.getCostExperience())), event -> begin(player, manager, levelNumber, LevelEditorField.EXPERIENCE));
		setButton(1, 5, button(Material.GOLD_INGOT,
				manager, "settings-menu.economy", "&6Economy cost",
				Integer.toString(level.getCostEconomy())), event -> begin(player, manager, levelNumber, LevelEditorField.ECONOMY));
		setButton(1, 7, button(Material.FILLED_MAP,
				manager, "settings-menu.size", "&bRegion size",
				Integer.toString(level.getSize())), event -> begin(player, manager, levelNumber, LevelEditorField.SIZE));

		setButton(2, 8, GuiUtils.createButtonItem(XMaterial.SPECTRAL_ARROW,
				plugin.getMessages().text(Lang.BACK)), event -> manager.openLevelMenu(player));
	}

	private void begin(Player player, VillageBuildEditorManager manager, int level, LevelEditorField field) {
		player.closeInventory();
		manager.beginFieldEdit(player, level, field);
	}

	private ItemStack button(Material material, VillageBuildEditorManager manager, String path, String fallback, String value) {
		String name = manager.getConfig().getString(path + ".name", fallback);
		List<String> lore = manager.getConfig().getStringList(path + ".lore");
		if (lore.isEmpty()) {
			lore = List.of("&7Currently: &f%value%", "", "&eClick to change in the chat");
		}
		return Item.create(material, name, lore.stream()
				.map(line -> tl(line.replace("%value%", value)))
				.collect(Collectors.toList()));
	}

	private String formatMaterials(Level level) {
		if (level.getMaterials().isEmpty()) {
			return "brak";
		}
		return level.getMaterials().entrySet().stream()
				.map(entry -> entry.getKey().name() + ":" + entry.getValue())
				.collect(Collectors.joining(", "));
	}
}
