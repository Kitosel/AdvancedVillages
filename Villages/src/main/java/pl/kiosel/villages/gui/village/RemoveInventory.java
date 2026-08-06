package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import pl.kiosel.core.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiConfig;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.events.VillageRemoveEvent;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

public final class RemoveInventory extends VillageMenu {

	public RemoveInventory(AdvancedVillages plugin, VillageGUIManager menus, Village village,
	                       Player player, Gui parent) {
		super(plugin, menus, village, player, GUIS.REMOVE, parent);
		for (int slot = 0; slot < GUIS.REMOVE.getSize(); slot++) {
			setItem(slot, Item.blank(Item.Blank.WHITE));
		}
		setButton(12, Item.create(Material.LIME_CONCRETE, GuiConfig.yes), event -> confirm(event.gui));
		setItem(13, Item.create(Material.PAPER, GuiConfig.delete_village));
		setButton(14, Item.create(Material.RED_CONCRETE, GuiConfig.no),
				event -> {
					playSound(Sound.BLOCK_ANVIL_HIT, 2.0f, 0.0f);
					event.manager.showGUI(event.player, parent);
				});
	}

	private void confirm(Gui gui) {
		VillageRemoveEvent removeEvent = new VillageRemoveEvent(village, viewer);
		plugin.getServer().getPluginManager().callEvent(removeEvent);
		if (removeEvent.isCancelled()) {
			return;
		}
		gui.exit();
		plugin.getEconomy().deposit(viewer, village.getBank());
		plugin.getVillageRemoveManager().removeVillage(village, true);
		plugin.getLocale().getMessage(Lang.VILLAGE_REMOVE.getPath()).sendPrefixedMessage(viewer);
		playSound(Sound.BLOCK_NOTE_BLOCK_FLUTE, 2.0f, 0.0f);
	}
}
