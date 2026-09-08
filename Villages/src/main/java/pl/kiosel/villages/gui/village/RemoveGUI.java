package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.compatibility.RosaSound;
import pl.kiosel.rosacore.compatibility.ZSound;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.api.events.VillageRemoveEvent;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.gui.GUIS;
import pl.kiosel.villages.gui.VillageConfirmationMenu;

import java.util.Collections;

public final class RemoveGUI extends VillageConfirmationMenu {

	public RemoveGUI(AdvancedVillages plugin, Village village, Player player, Gui parent) {
		super(plugin, village, player, GUIS.REMOVE, parent);
		GuiItemConfig confirm = item("confirm", 12, Material.LIME_CONCRETE, "&aYes");
		GuiItemConfig info = item("info", 13, Material.PAPER,
				plugin.getGuiSettings().text("gui-delete-village", "&7Do you want to delete village?"));
		GuiItemConfig cancel = item("cancel", 14, Material.RED_CONCRETE, "&cNo");

		setConfirmSlot(confirm.getSlot()).confirmItem(confirm.createItem()).showConfirm(confirm.isEnabled());
		setInformationSlot(info.getSlot()).informationItem(info.createItem()).showInformation(info.isEnabled());
		setCancelSlot(cancel.getSlot()).cancelItem(cancel.createItem()).showCancel(cancel.isEnabled());
		playSoundOnClick(true);
		setConfirmSound(new RosaSound.SoundHolder(ZSound.BLOCK_NOTE_BLOCK_FLUTE, 2.0f, 0.0f));
		setCancelSound(new RosaSound.SoundHolder(ZSound.BLOCK_ANVIL_HIT, 2.0f, 0.0f));
		setCloseSound(null);
		onConfirm(event -> confirm(event.getGui()));
		onCancel(event -> event.getManager().showGUI(event.getPlayer(), parent));
		setItems();
	}

	private GuiItemConfig item(String id, int slot, Material material, String name) {
		return item(id, slot, material, name, Collections.emptyList());
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
		messages().get(Lang.VILLAGE_REMOVE).sendPrefixed(viewer);
	}
}
