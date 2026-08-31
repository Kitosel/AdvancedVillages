package pl.kiosel.villages.gui.village;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.kiosel.rosacore.compatibility.ZSound;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.village.Permission;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.gui.GUIS;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

import java.util.List;

public final class StoreInventory extends VillageMenu {

	public StoreInventory(AdvancedVillages plugin, VillageGUIManager menus, Village village,
	                      Player player, Gui parent) {
		super(plugin, menus, village, player, GUIS.STORE, parent);
		addBackButton();

		addProduct("hearth", 19, 240, plugin.getApi().createHearthPart());
		addProduct("destroyer", 21, 120, plugin.getApi().createDestroyerHearth());
	}

	private void addProduct(String id, int slot, int fallbackPrice, ItemStack purchasedItem) {
		int price = plugin.getGuiSettings().integer("guis.village.store." + id + ".price", fallbackPrice, 0, Integer.MAX_VALUE);
		ItemMeta meta = purchasedItem.getItemMeta();
		String name = meta != null && meta.hasDisplayName() ? meta.getDisplayName() : purchasedItem.getType().name();
		GuiItemConfig display = plugin.getGuiSettings().item(GUIS.STORE,
				"guis.village.store." + id, slot, purchasedItem.getType(), name,
				List.of("&7Buy for &6%price%$"));
		if (!display.isEnabled()) return;
		List<String> lore = display.getLore().stream()
				.map(line -> line.replace("%price%", Integer.toString(price))).toList();
		setButton(display.getSlot(), display.createItem(display.getName(), lore),
				event -> buyItem(price, purchasedItem.clone()));
	}

	private void buyItem(int price, ItemStack item) {
		if (!hasPermission(Permission.STORE)) {
			return;
		}
		if (!plugin.getEconomy().hasBalance(viewer, price)) {
			double missing = price - plugin.getEconomy().getBalance(viewer);
			getVillageMessages().get(Lang.NO_MONEY)
					.with("money", missing).sendPrefixed(viewer);
			return;
		}
		if (viewer.getInventory().firstEmpty() == -1) {
			playSound(ZSound.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.0f);
			getVillageMessages().get(Lang.FULL_EQ).sendPrefixed(viewer);
			return;
		}
		plugin.getEconomy().withdrawBalance(viewer, price);
		playSound(ZSound.BLOCK_NOTE_BLOCK_PLING, 2.0f, 2.0f);
		getVillageMessages().get(Lang.MONEY_REMOVE)
				.with("money", price).sendPrefixed(viewer);
		viewer.getInventory().addItem(item);
		exit();
	}
}
