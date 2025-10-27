package pl.kiosel.villages.commands.subcommands;

import org.bukkit.entity.Player;
import pl.kiosel.villages.Wioski;
import pl.kiosel.common.command.SubCommand;
import pl.kiosel.villages.config.CommandConfig;
import pl.kiosel.villages.config.Language;
import pl.kiosel.villages.enums.CommandLang;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Upgrade;
import pl.kiosel.villages.api.events.VillageUpgradeEvent;
import pl.kiosel.villages.village.UpgradeManager;
import pl.kiosel.villages.village.Village;
import pl.kiosel.villages.village.VillageManager;

public class AdminCommand extends SubCommand {

	@Override
	public String getName() { return "admin"; }

	@Override
	public String getDescription() { return "Admin command"; }

	@Override
	public String getUsage() { return "/village admin <reload|give|upgrade|delete>"; }

	@Override
	public String getPermission() { return "villages.command.admin"; }

	@Override
	public void run(Player sender, Wioski plugin, String[] args) {
		Language lang = plugin.getLang();
		CommandConfig commandConfig = plugin.getCommandLang();

		String reloadCmd = commandConfig.getCommand(CommandLang.ADMIN_RELOAD).toLowerCase();
		String giveCmd = commandConfig.getCommand(CommandLang.ADMIN_GIVE).toLowerCase();
		String giveVillageCmd = commandConfig.getCommand(CommandLang.ADMIN_GIVE_VILLAGE).toLowerCase();
		String giveDestroyerCmd = commandConfig.getCommand(CommandLang.ADMIN_GIVE_DESTROYER).toLowerCase();
		String upgradeCmd = commandConfig.getCommand(CommandLang.ADMIN_UPGRADE).toLowerCase();
		String deleteCmd = commandConfig.getCommand(CommandLang.ADMIN_DELETE).toLowerCase();

		if (args.length < 2) {
			sender.sendMessage(tl("&cUżycie: /village admin <reload|give|upgrade|delete>"));
			return;
		}

		String input = args[1].toLowerCase();

		if (input.equals(reloadCmd)) {
			plugin.reloadPlugin();
			sender.sendMessage(tl(lang.getMessage(Lang.COMMAND_RELOAD)));
			return;
		}

		if (input.equals(giveCmd)) {
			if (args.length == 3) {
				String giveType = args[2].toLowerCase();

				if (giveType.equals(giveVillageCmd)) {
					sender.getInventory().addItem(plugin.getApi().createVillageBlock());
					sender.sendMessage(tl("&aOtrzymałeś blok wioski!"));
					return;
				}

				if (giveType.equals(giveDestroyerCmd)) {
					sender.getInventory().addItem(plugin.getApi().createDestroyer());
					sender.sendMessage(tl("&aOtrzymałeś narzędzie niszczyciela!"));
					return;
				}
			}
			sender.sendMessage(tl("&cUżycie: /village admin " + giveCmd + " <" + giveVillageCmd + "|" + giveDestroyerCmd + ">"));
			return;
		}

		if (input.equals(upgradeCmd)) {
			if (args.length == 3) {
				String owner = args[2];
				Village village = VillageManager.getVillageByOfflineOwner(owner);
				if (village != null) {
					VillageUpgradeEvent villageUpgradeEvent = new VillageUpgradeEvent(
							village, sender,
							Upgrade.getByLevel(village.getLevel()),
							Upgrade.getByLevel(village.getLevel() + 1),
							UpgradeManager.getCostForLevel(village.getLevel()-1));
					plugin.getServer().getPluginManager().callEvent(villageUpgradeEvent);
					if (villageUpgradeEvent.isCancelled()) return;

					plugin.getUpgradeManager().upgradeVillage(village);
					sender.sendMessage(tl("&aWioska gracza &f" + owner + " &azostała ulepszona!"));
				} else {
					sender.sendMessage(tl("&cNie znaleziono wioski gracza &f" + owner));
				}
			} else {
				sender.sendMessage(tl("&cUżycie: /village admin " + upgradeCmd + " <owner>"));
			}
			return;
		}

		if (input.equals(deleteCmd)) {
			if (args.length == 3) {
				String owner = args[2];
				Village village = VillageManager.getVillageByOfflineOwner(owner);
				if (village != null) {
					plugin.getGui().openGui(village, sender, GUIS.REMOVE);
					sender.sendMessage(tl("&cWioska gracza &f" + owner + " &czostała usunięta!"));
				} else {
					sender.sendMessage(tl("&cNie znaleziono wioski gracza &f" + owner));
				}
			} else {
				sender.sendMessage(tl("&cUżycie: /village admin " + deleteCmd + " <owner>"));
			}
			return;
		}

		sender.sendMessage(tl("&cNieznana komenda admina: &f" + input));
	}
}