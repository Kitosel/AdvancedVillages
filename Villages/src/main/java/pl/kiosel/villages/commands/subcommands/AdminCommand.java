package pl.kiosel.villages.commands.subcommands;

import org.bukkit.entity.Player;
import panda.std.Option;
import pl.kiosel.core.configuration.editor.PluginConfigGui;
import pl.kiosel.core.locale.Locale;
import pl.kiosel.core.math.MathUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.config.CommandConfig;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.CommandLang;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Upgrade;
import pl.kiosel.villages.events.VillageUpgradeEvent;
import pl.kiosel.villages.manager.VillageUtilsManager;

import java.time.Duration;
import java.time.Instant;

import static pl.kiosel.core.utils.ColorUtils.tl;

public class AdminCommand extends AVSubCommand {

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
	public void run(Player player, User user, String[] args) {
		CommandConfig commandConfig = plugin.getCommandLang();
		Locale locale = plugin.getLocale();

		String adminCmd = commandConfig.getCommand(CommandLang.ADMIN).toLowerCase();
		String reloadCmd = commandConfig.getCommand(CommandLang.ADMIN_RELOAD).toLowerCase();
		String manageCmd = commandConfig.getCommand(CommandLang.ADMIN_MANAGE).toLowerCase();
		String settingsCmd = commandConfig.getCommand(CommandLang.ADMIN_SETTINGS).toLowerCase();
		String giveCmd = commandConfig.getCommand(CommandLang.ADMIN_GIVE).toLowerCase();
		String giveVillageCmd = commandConfig.getCommand(CommandLang.ADMIN_GIVE_VILLAGE).toLowerCase();
		String giveDestroyerCmd = commandConfig.getCommand(CommandLang.ADMIN_GIVE_DESTROYER).toLowerCase();

		String upgradeCmd = commandConfig.getCommand(CommandLang.ADMIN_UPGRADE).toLowerCase();
		String deleteCmd = commandConfig.getCommand(CommandLang.ADMIN_DELETE).toLowerCase();
		String protectionCmd = commandConfig.getCommand(CommandLang.ADMIN_PROTECTION).toLowerCase();
		String livesCmd = commandConfig.getCommand(CommandLang.ADMIN_LIVES).toLowerCase();
		String bankCmd = commandConfig.getCommand(CommandLang.ADMIN_BANK).toLowerCase();

		String addCmd = commandConfig.getCommand(CommandLang.ADMIN_ADD).toLowerCase();
		String removeCmd = commandConfig.getCommand(CommandLang.ADMIN_REMOVE).toLowerCase();

		if (args.length < 2) {
			player.sendMessage(tl("&c/village "+adminCmd+" <"+reloadCmd+"|"+giveCmd+"|"+manageCmd+"|"+settingsCmd+">"));
			return;
		}

		String input = args[1].toLowerCase();

		if (input.equals(reloadCmd)) {
			plugin.reloadConfig();
			locale.getMessage(Lang.COMMAND_RELOAD.getPath()).sendPrefixedMessage(player);
			return;
		}

		if (input.equals(settingsCmd)) {
			this.plugin.getGuiManager().showGUI(player, new PluginConfigGui(this.plugin));
			return;
		}

		if (input.equals(giveCmd)) {
			if (args.length == 3) {
				String giveType = args[2].toLowerCase();

				if (giveType.equals(giveVillageCmd)) {
					player.getInventory().addItem(plugin.getApi().createVillageBlock());
					locale.getMessage(Lang.COMMAND_ADMIN_VILLAGE_BLOCK.getPath()).sendPrefixedMessage(player);
					return;
				}

				if (giveType.equals(giveDestroyerCmd)) {
					player.getInventory().addItem(plugin.getApi().createDestroyer());
					locale.getMessage(Lang.COMMAND_ADMIN_DESTROYER.getPath()).sendPrefixedMessage(player);
					return;
				}
			}
			player.sendMessage(tl("&c/village "+adminCmd+" " + giveCmd + " <" + giveVillageCmd + "|" + giveDestroyerCmd + ">"));
			return;
		}

		if (input.equals(manageCmd)) {
			//      0      1     2      3     4   length
			//     -1      0     1      2     3   args
			// /villages admin manage owner type
			if (args.length <= 3) {
				player.sendMessage(tl("&c/village "+adminCmd+" " + manageCmd + " <owner> " + " <" + upgradeCmd + "|" + livesCmd + "|" + protectionCmd + "|" + removeCmd + ">"));
				return;
			}
			String owner = args[2];
			Option<User> ownerOption = plugin.getUserManager().findByName(owner);
			if (ownerOption.isEmpty()) {
				locale.getMessage(Lang.COMMAND_VILLAGE_NOT_FOUND.getPath()).processPlaceholder("owner", owner).sendPrefixedMessage(player);
				return;
			}
			User userOwner = ownerOption.get();
			Village village = userOwner.getPresentVillage();
			if (village == null) {
				locale.getMessage(Lang.COMMAND_VILLAGE_NOT_FOUND.getPath()).processPlaceholder("owner", owner).sendPrefixedMessage(player);
				return;
			}

			if (args.length == 4) {
				String manage = args[3].toLowerCase();
				if (manage.equals(deleteCmd)) {
					plugin.getVillageGui().openGui(village, player, GUIS.REMOVE);
					locale.getMessage(Lang.COMMAND_ADMIN_DELETE.getPath()).processPlaceholder("owner", owner).sendPrefixedMessage(player);
				}
				if (manage.equals(upgradeCmd)) {
					int currentLevel = village.getLevel().getLevel();
					pl.kiosel.villages.data.village.level.Level nextLevel = plugin.getLevelManager().getLevel(currentLevel + 1);
					if (nextLevel == null) {
						locale.getMessage(Lang.VILLAGE_MAX_LEVEL.getPath()).sendPrefixedMessage(player);
						return;
					}
					if (!plugin.getUpgradeManager().canPasteLevel(nextLevel.getLevel())) {
						locale.getMessage(Lang.BUILD_EDITOR_SCHEMATIC_MISSING.getPath())
								.processPlaceholder("schematic", "Turret" + nextLevel.getLevel() + ".schem")
								.sendPrefixedMessage(player);
						return;
					}
					VillageUpgradeEvent villageUpgradeEvent = new VillageUpgradeEvent(
							village, player,
							Upgrade.getByLevel(currentLevel),
							Upgrade.getByLevel(nextLevel.getLevel()),
							nextLevel.getCostEconomy());
					plugin.getServer().getPluginManager().callEvent(villageUpgradeEvent);
					if (villageUpgradeEvent.isCancelled()) return;

					plugin.getUpgradeManager().upgradeVillage(village);
					locale.getMessage(Lang.COMMAND_ADMIN_UPGRADE.getPath()).processPlaceholder("owner", owner).sendPrefixedMessage(player);
				}
				if (manage.equals(livesCmd)
						|| manage.equals(protectionCmd)
						|| manage.equals(bankCmd)) {
					player.sendMessage(tl("&c/village "+adminCmd+" " + manageCmd + " <owner> " + manage + " <" + addCmd + "|" + removeCmd + ">"));
				}
				return;
			}
			if (args.length == 5) {
				String manage = args[3].toLowerCase();
				String addrem = args[4].toLowerCase();
				if (manage.equals(protectionCmd)) {
					if (addrem.equals(removeCmd)) {
						village.setProtection(Instant.now());
						VillageUtilsManager.replaceWith(player, village, Lang.COMMAND_ADMIN_PROTECTION_REMOVE.getPath()).sendPrefixedMessage(player);
					} else if (addrem.equals(addCmd)) {
						player.sendMessage(tl("&c/village "+adminCmd+" " + manageCmd + " <owner> " + manage + " <" + addCmd + "|" + removeCmd + "> " + " <number> <time unit>"));
					}
				}
				if (manage.equals(bankCmd) || manage.equals(livesCmd)) {
					player.sendMessage(tl("&c/village "+adminCmd+" " + manageCmd + " <owner> " + manage + " <" + addCmd + "|" + removeCmd + "> " + " <number>"));
				}
				return;
			}
			if (args.length == 6) {
				String manage = args[3].toLowerCase();
				String addrem = args[4].toLowerCase();
				Integer number = MathUtils.parseInt(args[5]);
				if (number==null) {
					locale.getMessage(Lang.COMMAND_INVALID.getPath()).sendPrefixedMessage(player);
					return;
				}
				if (manage.equals(protectionCmd)) {
					if (addrem.equals(addCmd)) {
						player.sendMessage(tl("&c/village "+adminCmd+" " + manageCmd + " <owner> " + manage + " <" + addCmd + "|" + removeCmd + "> " + " <number> <time unit>"));
					}
				}
				if (manage.equals(bankCmd)) {
					String bank_add = locale.getMessage(Lang.COMMAND_ADMIN_BANK_ADD.getPath()).toText();
					String bank_remove = locale.getMessage(Lang.COMMAND_ADMIN_BANK_REMOVE.getPath()).toText();
					if (addrem.equals(addCmd)) {
						village.addBank(number);
						VillageUtilsManager.replaceWith(player, village, bank_add)
								.processPlaceholder("money", number)
								.sendPrefixedMessage(player);
					}
					if (addrem.equals(removeCmd)) {
						village.removeBank(number);
						VillageUtilsManager.replaceWith(player, village, bank_remove)
								.processPlaceholder("money", number)
								.sendPrefixedMessage(player);
					}
					return;
				}
				if (manage.equals(livesCmd)) {
					String live_add = locale.getMessage(Lang.COMMAND_ADMIN_LIVES_ADD.getPath()).toText();
					String live_remove = locale.getMessage(Lang.COMMAND_ADMIN_LIVES_REMOVE.getPath()).toText();
					if (addrem.equals(addCmd)) {
						village.setLives(village.getLives() + number);
						VillageUtilsManager.replaceWith(player, village, live_add)
								.processPlaceholder("lives", number)
								.sendPrefixedMessage(player);
					}
					if (addrem.equals(removeCmd)) {
						if (village.getLives() <= 0) {
							locale.getMessage(Lang.COMMAND_ADMIN_ALREADY_DYING.getPath()).sendPrefixedMessage(player);
							return;
						}
						village.setLives(village.getLives() - number);
						VillageUtilsManager.replaceWith(player, village, live_remove)
								.processPlaceholder("lives", number)
								.sendPrefixedMessage(player);
					}
					return;
				}
				return;
			}
			if (args.length == 7) {
				String manage = args[3].toLowerCase();
				String addrem = args[4].toLowerCase();
				Integer number = MathUtils.parseInt(args[5]);
				if (number==null) {
					locale.getMessage(Lang.COMMAND_INVALID.getPath()).sendPrefixedMessage(player);
					return;
				}
				String unit = args[6].toLowerCase();
				if (manage.equals(protectionCmd)) {
					if (addrem.equals(addCmd)) {
						Duration duration;
						switch (unit) {
							case "d":
							case "day":
							case "days":
								duration = Duration.ofDays(number);
								break;
							case "h":
							case "hor":
							case "hours":
								duration = Duration.ofHours(number);
								break;
							case "m":
							case "min":
							case "minutes":
								duration = Duration.ofMinutes(number);
								break;
							case "s":
							case "sec":
							case "seconds":
								duration = Duration.ofSeconds(number);
								break;
							default:
								locale.getMessage(Lang.COMMAND_INVALID.getPath()).sendPrefixedMessage(player);
								return;
						}
						village.setProtection(Instant.now().plus(duration));
						VillageUtilsManager.replaceWith(player, village, locale.getMessage(Lang.COMMAND_ADMIN_PROTECTION_ADD.getPath()).toText())
								.processPlaceholder("time", number + " " + unit)
								.processPlaceholder("full_time", Instant.now().plus(duration).toString())
								.sendPrefixedMessage(player);
					}
				}
				return;
			}
		}
		locale.getMessage(Lang.COMMAND_UNKNOWN.getPath())
				.processPlaceholder("input", input)
				.sendPrefixedMessage(player);
	}
}
