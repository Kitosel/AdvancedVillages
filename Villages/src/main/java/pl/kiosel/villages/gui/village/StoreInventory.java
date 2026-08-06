package pl.kiosel.villages.gui.village;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.core.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Permission;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

import java.util.List;

public final class StoreInventory extends VillageMenu {

	public StoreInventory(AdvancedVillages plugin, VillageGUIManager menus, Village village,
	                      Player player, Gui parent) {
		super(plugin, menus, village, player, GUIS.STORE, parent);
		addBackButton(4);

		ItemStack villageHearth = Item.addLoreToItemStack(
				plugin.getApi().createVillageHearth(), List.of("Buy for 240$"));
		ItemStack destroyerHearth = Item.addLoreToItemStack(
				plugin.getApi().createDestroyerHearth(), List.of("Buy for 120$"));
		setButton(19, villageHearth, event -> buyItem(240, plugin.getApi().createVillageHearth()));
		setButton(21, destroyerHearth, event -> buyItem(120, plugin.getApi().createDestroyerHearth()));
	}

	private void buyItem(int price, ItemStack item) {
		if (!hasPermission(Permission.STORE)) {
			return;
		}
		if (!plugin.getEconomy().hasBalance(viewer, price)) {
			double missing = price - plugin.getEconomy().getBalance(viewer);
			plugin.getLocale().getMessage(Lang.NO_MONEY.getPath())
					.processPlaceholder("money", missing).sendPrefixedMessage(viewer);
			return;
		}
		if (viewer.getInventory().firstEmpty() == -1) {
			playSound(Sound.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.0f);
			plugin.getLocale().getMessage(Lang.FULL_EQ.getPath()).sendPrefixedMessage(viewer);
			return;
		}
		plugin.getEconomy().withdrawBalance(viewer, price);
		playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 2.0f, 2.0f);
		plugin.getLocale().getMessage(Lang.MONEY_REMOVE.getPath())
				.processPlaceholder("money", price).sendPrefixedMessage(viewer);
		viewer.getInventory().addItem(item);
		exit();
	}
}
