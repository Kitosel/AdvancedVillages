package pl.kiosel.villages.addons.buildeditor.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.buildeditor.VillageBuildEditorManager;
import pl.kiosel.villages.gui.Item;

public final class EditorMenu extends Gui {

	public EditorMenu(AdvancedVillages plugin, Player player, VillageBuildEditorManager manager) {
		setRows(3);
		setTitle(manager.guiTitle());
		setDefaultItem(Item.blank(Item.Blank.BLACK));

		setButton(2, 4, Item.create(Material.NOTE_BLOCK, "Village Builds"), event ->
				event.getManager().openGUI(player, new VillageEditMenu(plugin, player, manager, this)));
		if (plugin.isDev()) {
			setButton(2, 6, Item.create(Material.LODESTONE, "Outpost Builds"), event ->
					event.getManager().openGUI(player, new OutpostEditMenu(plugin, player, manager, this)));
		}

		setButton(3, 9, Item.blank(Item.Blank.EXIT), event -> player.closeInventory());
	}
}
