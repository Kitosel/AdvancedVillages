package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.kiosel.core.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiConfig;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Permission;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

import java.util.List;

public final class BankInventory extends VillageMenu {

	public BankInventory(AdvancedVillages plugin, VillageGUIManager menus, Village village,
	                     Player player, Gui parent) {
		super(plugin, menus, village, player, GUIS.BANK, parent);
		addBackButton(4);
		addBackButton(35);

		setItem(13, Item.create(Material.PAPER, 1,
				GuiConfig.guis_village_bank_balance.replace("%village_balance%", Long.toString(village.getBank()))));
		setButton(21, Item.create(Material.GREEN_CONCRETE, 5, GuiConfig.guis_village_bank_add,
				List.of("&a&l+5")), event -> addBank(5));
		setButton(22, Item.create(Material.GREEN_CONCRETE, 10, GuiConfig.guis_village_bank_add,
				List.of("&a&l+10")), event -> addBank(10));
		setButton(23, Item.create(Material.GREEN_CONCRETE, 60, GuiConfig.guis_village_bank_add,
				List.of("&a&l+100")), event -> addBank(100));

		setButton(30, Item.create(Material.RED_CONCRETE, 5, GuiConfig.guis_village_bank_remove,
				List.of("&c&l-5")), event -> removeBank(5));
		setButton(31, Item.create(Material.RED_CONCRETE, 10, GuiConfig.guis_village_bank_remove,
				List.of("&c&l-10")), event -> removeBank(10));
		setButton(32, Item.create(Material.RED_CONCRETE, 60, GuiConfig.guis_village_bank_remove,
				List.of("&c&l-100")), event -> removeBank(100));
	}

	private void addBank(int amount) {
		if (!hasPermission(Permission.BANK_ADD)) {
			return;
		}
		if (!plugin.getEconomy().hasBalance(viewer, amount)) {
			double missing = amount - plugin.getEconomy().getBalance(viewer);
			plugin.getLocale().getMessage(Lang.NO_MONEY.getPath())
					.processPlaceholder("money", missing).sendPrefixedMessage(viewer);
			return;
		}
		village.addBank(amount);
		plugin.getEconomy().withdrawBalance(viewer, amount);
		plugin.getLocale().getMessage(Lang.BANK_ADD.getPath())
				.processPlaceholder("money", amount).sendPrefixedMessage(viewer);
		plugin.getLocale().getMessage(Lang.MONEY_REMOVE.getPath())
				.processPlaceholder("money", amount).sendPrefixedMessage(viewer);
		reopen(GUIS.BANK);
	}

	private void removeBank(int amount) {
		if (!hasPermission(Permission.BANK_REMOVE)) {
			return;
		}
		if (village.getBank() < amount) {
			plugin.getLocale().getMessage(Lang.BANK_NO_MONEY.getPath()).sendPrefixedMessage(viewer);
			return;
		}
		village.removeBank(amount);
		plugin.getEconomy().deposit(viewer, amount);
		plugin.getLocale().getMessage(Lang.BANK_REMOVE.getPath())
				.processPlaceholder("money", amount).sendPrefixedMessage(viewer);
		plugin.getLocale().getMessage(Lang.MONEY_ADD.getPath())
				.processPlaceholder("money", amount).sendPrefixedMessage(viewer);
		reopen(GUIS.BANK);
	}
}
