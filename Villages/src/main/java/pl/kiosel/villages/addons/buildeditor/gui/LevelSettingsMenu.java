package pl.kiosel.villages.addons.buildeditor.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.buildeditor.LevelEditorField;
import pl.kiosel.villages.addons.buildeditor.BuildEditorType;
import pl.kiosel.villages.addons.buildeditor.VillageBuildEditorManager;
import pl.kiosel.villages.data.village.level.Level;
import pl.kiosel.villages.gui.Item;

import java.util.List;
import java.util.stream.Collectors;

import static pl.kiosel.rosacore.utils.ColorUtils.tl;

public final class LevelSettingsMenu extends Gui {

	public LevelSettingsMenu(AdvancedVillages plugin, Player player, VillageBuildEditorManager manager, int levelNumber) {
		this(plugin, player, manager, levelNumber, BuildEditorType.VILLAGE);
	}

	public LevelSettingsMenu(AdvancedVillages plugin, Player player, VillageBuildEditorManager manager,
	                         int levelNumber, BuildEditorType type) {
		Level level = type == BuildEditorType.OUTPOST
				? plugin.getOutpostLevelManager().getLevel(levelNumber)
				: plugin.getLevelManager().getLevel(levelNumber);
		setRows(3);
		setTitle(manager.getConfig().getString("settings-menu.title", "&8Level &6%level% &8settings")
				.replace("%level%", String.valueOf(levelNumber)));
		setDefaultItem(Item.blank(Item.Blank.BLACK));

		setButton(1, 1, button(Material.CHEST,
				manager, "settings-menu.items", "&eRequired Items",
				formatMaterials(level)), event -> begin(player, manager, levelNumber, LevelEditorField.ITEMS, type));
		setButton(1, 3, button(Material.EXPERIENCE_BOTTLE,
				manager, "settings-menu.experience", "&aExperience cost",
				Integer.toString(level.getCostExperience())), event -> begin(player, manager, levelNumber, LevelEditorField.EXPERIENCE, type));
		setButton(1, 5, button(Material.GOLD_INGOT,
				manager, "settings-menu.economy", "&6Economy cost",
				Integer.toString(level.getCostEconomy())), event -> begin(player, manager, levelNumber, LevelEditorField.ECONOMY, type));
		setButton(1, 7, button(Material.FILLED_MAP,
				manager, "settings-menu.size", "&bRegion size",
				Integer.toString(level.getSize())), event -> begin(player, manager, levelNumber, LevelEditorField.SIZE, type));

		setButton(2, 8, Item.blank(Item.Blank.BACK),event -> manager.openLevelMenu(player));
	}

	private void begin(Player player, VillageBuildEditorManager manager, int level,
	                   LevelEditorField field, BuildEditorType type) {
		player.closeInventory();
		if (type == BuildEditorType.OUTPOST) manager.beginOutpostFieldEdit(player, level, field);
		else manager.beginFieldEdit(player, level, field);
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
