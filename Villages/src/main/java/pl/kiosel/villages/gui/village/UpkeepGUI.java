package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.rosacore.utils.NumberUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.VillagePermission;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.features.rent.VillageUpkeepManager;
import pl.kiosel.villages.gui.GUIS;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

import java.util.List;

public final class UpkeepGUI extends VillageMenu {

	public UpkeepGUI(AdvancedVillages plugin, VillageGUIManager menus, Village village, Player viewer, Gui parent) {
		super(plugin, menus, village, viewer, GUIS.UPKEEP, parent);
		addBackButton();

		VillageUpkeepManager upkeepManager = plugin.getUpkeepManager();

		GuiItemConfig upkeep = item("info", 13, Material.IRON_NUGGET,
				"&6Village upkeep", List.of(
						"&7Next cost: &6%cost%$",
						"&7Payment in: &f%time%",
						"&7Missed payments: &c%missed%"));

		String cost = Integer.toString(upkeepManager.calculateCost(village));
		String time = plugin.getVillageMessages().formatDuration(upkeepManager.getRemaining(village));
		String missed = Integer.toString(upkeepManager.getMissedPayments(village));
		String name = upkeep.getName().replace("%cost%", cost).replace("%time%", time).replace("%missed%", missed);
		List<String> lore = upkeep.getLore().stream().map(line -> line
				.replace("%cost%", cost).replace("%time%", time).replace("%missed%", missed)).toList();
		if (upkeep.isEnabled()) setItem(upkeep.getSlot(), upkeep.createItem(name, lore));

		GuiItemConfig pay = item("pay", 21, Material.SUNFLOWER, "&6Pay upkeep",
				List.of("&7Cost: &6%cost%$", "&7Village bank: &f%bank%$", "&7Your balance: &f%balance%$", "",
						"&7Right click: &ePay with bank", "&7Left click: &dPay with balance"));
		if (pay.isEnabled()) {
			String balance = NumberUtils.formatNumber(plugin.getEconomy().getBalance(viewer), 2);
			List<String> payLore = pay.getLore().stream()
					.map(line -> line.replace("%cost%", cost)
							.replace("%bank%", Integer.toString(village.getBank()))
							.replace("%balance%", balance))
					.toList();
			setButton(pay.getSlot(), pay.createItem(pay.getName().replace("%cost%", cost), payLore),
					event -> pay(upkeepManager, event.getClickType().isRightClick()));
		}

		boolean automatic = upkeepManager.isAutomaticPayment(village);
		String status = plugin.getGuiSettings().text(automatic
				? "guis.upkeep.status.enabled" : "guis.upkeep.status.disabled",
				automatic ? "&aEnabled" : "&cDisabled");
		GuiItemConfig automaticPayment = item("automatic-payment", 23, Material.ALLIUM,
				"&dAutomatic payment", List.of("&7Current: %status%", "", "&eClick to change"));
		if (automaticPayment.isEnabled()) {
			List<String> automaticLore = automaticPayment.getLore().stream()
					.map(line -> line.replace("%status%", status)).toList();
			setButton(automaticPayment.getSlot(), automaticPayment.createItem(
					automaticPayment.getName().replace("%status%", status), automaticLore,
					automaticPayment.isGlow() || automatic), event -> toggleAutomaticPayment(upkeepManager));
		}
	}

	private void pay(VillageUpkeepManager upkeepManager, boolean bank) {
		if (!hasPermission(VillagePermission.UPKEEP)) return;
		if (!upkeepManager.canPayNow(village)) {
			getVillageMessages().get(Lang.UPKEEP_NOT_DUE)
					.with("time", getVillageMessages().formatDuration(
							upkeepManager.getManualPaymentAvailableIn(village)))
					.sendPrefixed(viewer);
			return;
		}
		if (bank) {
			if (!upkeepManager.payNowBank(village, viewer)) {
				getVillageMessages().get(Lang.BANK_NO_MONEY).sendPrefixed(viewer);
				return;
			}
		} else {
			if (!upkeepManager.payNowBalance(village, viewer)) {
				getVillageMessages().get(Lang.NO_MONEY).sendPrefixed(viewer);
				return;
			}
		}
		playToggleSound();
		refresh();
	}

	private void toggleAutomaticPayment(VillageUpkeepManager upkeepManager) {
		if (!hasPermission(VillagePermission.UPKEEP)) return;
		upkeepManager.setAutomaticPayment(village, !upkeepManager.isAutomaticPayment(village));
		playToggleSound();
		refresh();
	}

	private void refresh() {
		plugin.getGuiManager().showGUI(viewer,
				new UpkeepGUI(plugin, menus, village, viewer, getParent()));
	}

	private GuiItemConfig item(String id, int slot, Material material, String name, List<String> lore) {
		return plugin.getGuiSettings().item(GUIS.UPKEEP, "guis.upkeep." + id,
				slot, material, name, lore);
	}
}
