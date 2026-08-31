package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.logs.VillageLogType;
import pl.kiosel.villages.addons.quests.QuestType;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.village.Permission;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.gui.GUIS;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

import java.util.List;

public final class BankInventory extends VillageMenu {

	public BankInventory(AdvancedVillages plugin, VillageGUIManager menus, Village village,
	                     Player player, Gui parent) {
		super(plugin, menus, village, player, GUIS.BANK, parent);
		addBackButton();

		GuiItemConfig balance = item("account_balance", 13, Material.PAPER, 1,
				"&6Bank&7: %village_balance%", List.of());
		if (balance.isEnabled()) {
			setItem(balance.getSlot(), balance.createItem(
					balance.getName().replace("%village_balance%", Long.toString(village.getBank())),
					balance.getLore()));
		}

		if (plugin.getUpkeepManager().isEnabled()) {
			GuiItemConfig upkeep = item("upkeep", 18, Material.CLOCK, 1,
					"&6Village upkeep", List.of(
							"&7Next cost: &6%cost%$",
							"&7Payment in: &f%time%",
							"&7Missed payments: &c%missed%"));
			if (upkeep.isEnabled()) {
				String cost = Integer.toString(plugin.getUpkeepManager().calculateCost(village));
				String time = plugin.getVillageMessages().formatDuration(plugin.getUpkeepManager().getRemaining(village));
				String missed = Integer.toString(plugin.getUpkeepManager().getMissedPayments(village));
				setItem(upkeep.getSlot(), upkeep.createItem(
						upkeep.getName().replace("%cost%", cost).replace("%time%", time).replace("%missed%", missed),
						upkeep.getLore().stream().map(line -> line
								.replace("%cost%", cost).replace("%time%", time).replace("%missed%", missed)).toList()));
			}
		}

		depositButton("deposit-all", 11, Material.GREEN_DYE, 1, "&a&l+ALL", -1);
		withdrawButton("withdraw-all", 15, Material.RED_DYE, 1, "&c&l-ALL", -1);

		depositButton("deposit-1", 20, Material.EMERALD, 1, "&a&l+1", 1);
		depositButton("deposit-5", 21, Material.EMERALD, 5, "&a&l+5", 5);
		depositButton("deposit-10", 22, Material.EMERALD, 10, "&a&l+10", 10);
		depositButton("deposit-100", 23, Material.EMERALD_BLOCK, 1, "&a&l+100", 100);
		depositButton("deposit-1000", 24, Material.EMERALD_BLOCK, 10, "&a&l+1000", 1000);

		withdrawButton("withdraw-1", 29, Material.REDSTONE, 1, "&c&l-1", 1);
		withdrawButton("withdraw-5", 30, Material.REDSTONE, 5, "&c&l-5", 5);
		withdrawButton("withdraw-10", 31, Material.REDSTONE, 10, "&c&l-10", 10);
		withdrawButton("withdraw-100", 32, Material.REDSTONE_BLOCK, 1, "&c&l-100", 100);
		withdrawButton("withdraw-1000", 33, Material.REDSTONE_BLOCK, 10, "&c&l-1000", 1000);
	}

	private GuiItemConfig item(String id, int slot, Material material, int amount,
	                           String fallbackName, List<String> fallbackLore) {
		return plugin.getGuiSettings().item(GUIS.BANK, "guis.village.bank." + id,
				slot, material, amount, fallbackName, fallbackLore, false);
	}

	private void depositButton(String id, int slot, Material material, int stackAmount,
	                           String lore, int economyAmount) {
		String name = plugin.getGuiSettings().text("guis.village.bank.add.name", "&aAdd to bank");
		GuiItemConfig button = item(id, slot, material, stackAmount, name, List.of(lore));
		if (!button.isEnabled()) return;
		setButton(button.getSlot(), button.createItem(), event -> addBank(economyAmount < 0
				? (int) plugin.getEconomy().getBalance(event.getPlayer())
				: economyAmount));
	}

	private void withdrawButton(String id, int slot, Material material, int stackAmount,
	                            String lore, int economyAmount) {
		String name = plugin.getGuiSettings().text("guis.village.bank.remove.name", "&cRemove from bank");
		GuiItemConfig button = item(id, slot, material, stackAmount, name, List.of(lore));
		if (!button.isEnabled()) return;
		setButton(button.getSlot(), button.createItem(), event -> removeBank(economyAmount < 0
				? village.getBank()
				: economyAmount));
	}

	private void addBank(int amount) {
		if (!hasPermission(Permission.BANK_ADD)) {
			return;
		}
		if (!plugin.getEconomy().hasBalance(viewer, amount)) {
			double missing = amount - plugin.getEconomy().getBalance(viewer);
			getVillageMessages().get(Lang.NO_MONEY)
					.with("money", missing).sendPrefixed(viewer);
			return;
		}
		village.addBank(amount);
		plugin.getEconomy().withdraw(viewer, amount);
		plugin.getQuestManager().record(village, QuestType.BANK_DEPOSIT, null, amount);
		plugin.getLogManager().record(village, VillageLogType.BANK_DEPOSIT, viewer,
				"amount", amount);
		getVillageMessages().get(Lang.BANK_ADD)
				.with("money", amount).sendPrefixed(viewer);
		getVillageMessages().get(Lang.MONEY_REMOVE)
				.with("money", amount).sendPrefixed(viewer);
		reopen(GUIS.BANK);
	}

	private void removeBank(int amount) {
		if (!hasPermission(Permission.BANK_REMOVE)) {
			return;
		}
		if (village.getBank() < amount) {
			getVillageMessages().get(Lang.BANK_NO_MONEY).sendPrefixed(viewer);
			return;
		}
		village.removeBank(amount);
		plugin.getEconomy().deposit(viewer, amount);
		plugin.getLogManager().record(village, VillageLogType.BANK_WITHDRAW, viewer,
				"amount", amount);
		getVillageMessages().get(Lang.BANK_REMOVE)
				.with("money", amount).sendPrefixed(viewer);
		getVillageMessages().get(Lang.MONEY_ADD)
				.with("money", amount).sendPrefixed(viewer);
		reopen(GUIS.BANK);
	}
}
