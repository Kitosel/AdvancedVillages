package pl.kiosel.villages.commands.subcommands;

import org.bukkit.entity.Player;
import pl.kiosel.core.commands.SubCommand;
import pl.kiosel.core.locale.Locale;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.CommandConfig;
import pl.kiosel.villages.enums.CommandLang;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Upgrade;
import pl.kiosel.villages.api.events.VillageUpgradeEvent;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageManager;

import static pl.kiosel.core.utils.ColorUtils.tl;

public class AdminCommand extends SubCommand {

	@Override
	public String getName() { return "admin"; }

	@Override
	public String getDescription() { return "Admin command"; }

	@Override
	public String getUsage() { return "/village admin <reload|give|upgrade|delete>"; }

	@Override
	public String getPermission() { return "villages.command.admin"; }

	private final AdvancedVillages plugin;

	public AdminCommand(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run(Player sender, String[] args) {
		CommandConfig commandConfig = plugin.getCommandLang();
		Locale locale = plugin.getLocale();

		String adminCmd = commandConfig.getCommand(CommandLang.ADMIN).toLowerCase();
		String reloadCmd = commandConfig.getCommand(CommandLang.ADMIN_RELOAD).toLowerCase();
		String giveCmd = commandConfig.getCommand(CommandLang.ADMIN_GIVE).toLowerCase();
		String giveVillageCmd = commandConfig.getCommand(CommandLang.ADMIN_GIVE_VILLAGE).toLowerCase();
		String giveDestroyerCmd = commandConfig.getCommand(CommandLang.ADMIN_GIVE_DESTROYER).toLowerCase();
		String upgradeCmd = commandConfig.getCommand(CommandLang.ADMIN_UPGRADE).toLowerCase();
		String deleteCmd = commandConfig.getCommand(CommandLang.ADMIN_DELETE).toLowerCase();

		if (args.length < 2) {
			sender.sendMessage(tl("&c/village "+adminCmd+" <"+reloadCmd+"|"+giveCmd+"|"+upgradeCmd+"|"+deleteCmd+">"));
			return;
		}

		String input = args[1].toLowerCase();

		if (input.equals(reloadCmd)) {
			plugin.reloadConfig();
			locale.getMessage(Lang.COMMAND_RELOAD.getPath()).sendPrefixedMessage(sender);
			return;
		}

		if (input.equals(giveCmd)) {
			if (args.length == 3) {
				String giveType = args[2].toLowerCase();

				if (giveType.equals(giveVillageCmd)) {
					sender.getInventory().addItem(plugin.getApi().createVillageBlock());
					locale.getMessage(Lang.COMMAND_ADMIN_VILLAGE_BLOCK.getPath()).sendPrefixedMessage(sender);
					return;
				}

				if (giveType.equals(giveDestroyerCmd)) {
					sender.getInventory().addItem(plugin.getApi().createDestroyer());
					locale.getMessage(Lang.COMMAND_ADMIN_DESTROYER.getPath()).sendPrefixedMessage(sender);
					return;
				}
			}
			sender.sendMessage(tl("&c/village "+adminCmd+" " + giveCmd + " <" + giveVillageCmd + "|" + giveDestroyerCmd + ">"));
			return;
		}

		if (input.equals(upgradeCmd)) {
			if (args.length == 3) {
				String owner = args[2];
				Village village = VillageManager.getVillageByOfflineOwner(owner);
				if (village != null) {
					VillageUpgradeEvent villageUpgradeEvent = new VillageUpgradeEvent(
							village, sender,
							Upgrade.getByLevel(village.getLevel().getLevel()),
							Upgrade.getByLevel(village.getLevel().getLevel() + 1),
							village.getLevel().getCostEconomy());
					plugin.getServer().getPluginManager().callEvent(villageUpgradeEvent);
					if (villageUpgradeEvent.isCancelled()) return;

					plugin.getUpgradeManager().upgradeVillage(village);
					locale.getMessage(Lang.COMMAND_ADMIN_UPGRADE.getPath())
							.processPlaceholder("owner", owner).sendPrefixedMessage(sender);
				} else {
					locale.getMessage(Lang.COMMAND_VILLAGE_NOT_FOUND.getPath())
							.processPlaceholder("owner", owner).sendPrefixedMessage(sender);
				}
			} else {
				sender.sendMessage(tl("&c/village "+adminCmd+" "+upgradeCmd+" <owner>"));
			}
			return;
		}

		if (input.equals(deleteCmd)) {
			if (args.length == 3) {
				String owner = args[2];
				Village village = VillageManager.getVillageByOfflineOwner(owner);
				if (village != null) {
					plugin.getVillageGui().openGui(village, sender, GUIS.REMOVE);
					locale.getMessage(Lang.COMMAND_ADMIN_DELETE.getPath())
							.processPlaceholder("owner", owner).sendPrefixedMessage(sender);
				} else {
					locale.getMessage(Lang.COMMAND_VILLAGE_NOT_FOUND.getPath())
							.processPlaceholder("owner", owner).sendPrefixedMessage(sender);
				}
			} else {
				sender.sendMessage(tl("&c/village "+adminCmd+" "+deleteCmd+" <owner>"));
			}
			return;
		}

		sender.sendMessage(tl("&cUnknown command: &f" + input));
	}
}