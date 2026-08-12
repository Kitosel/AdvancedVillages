package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.kiosel.core.gui.Gui;
import pl.kiosel.dependencies.com.cryptomorin.xseries.XSound;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiItemConfig;
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
		for (int slot = 0; slot < menuConfig.getSize(); slot++) {
			setItem(slot, Item.blank(Item.Blank.WHITE));
		}
		GuiItemConfig confirm = item("confirm", 12, Material.LIME_CONCRETE,
				getMessages().get(Lang.YES).toText());
		if (confirm.isEnabled()) {
			setButton(confirm.getSlot(), confirm.createItem(), event -> confirm(event.gui));
		}
		GuiItemConfig info = item("info", 13, Material.PAPER,
				plugin.getGuiSettings().text("gui-delete-village", "&7Do you want to delete village?"));
		if (info.isEnabled()) setItem(info.getSlot(), info.createItem());
		GuiItemConfig cancel = item("cancel", 14, Material.RED_CONCRETE,
				getMessages().get(Lang.NO).toText());
		if (cancel.isEnabled()) {
			setButton(cancel.getSlot(), cancel.createItem(), event -> {
				playSound(XSound.BLOCK_ANVIL_HIT, 2.0f, 0.0f);
				event.manager.showGUI(event.player, parent);
			});
		}
	}

	private GuiItemConfig item(String id, int slot, Material material, String name) {
		return plugin.getGuiSettings().item(GUIS.REMOVE, "guis.remove." + id,
				slot, material, name, java.util.Collections.emptyList());
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
		getMessages().get(Lang.VILLAGE_REMOVE).sendPrefixedMessage(viewer);
		playSound(XSound.BLOCK_NOTE_BLOCK_FLUTE, 2.0f, 0.0f);
	}
}
