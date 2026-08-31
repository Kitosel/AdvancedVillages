package pl.kiosel.villages.commands.subcommands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.rosacore.location.LocationUtils;
import pl.kiosel.rosacore.utils.NumberUtils;
import pl.kiosel.rosacore.utils.PlayerUtils;
import pl.kiosel.rosacore.utils.TimeUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.logs.VillageLogType;
import pl.kiosel.villages.api.events.VillageUpgradeEvent;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.config.CommandConfig;
import pl.kiosel.villages.config.CommandLang;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Permission;
import pl.kiosel.villages.data.village.Upgrade;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.level.Level;
import pl.kiosel.villages.gui.GUIS;
import pl.kiosel.villages.manager.VillageUtilsManager;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminCommand extends AVSubCommand {

	@Override
	public String getDescription() { return "Admin command"; }

	@Override
	public String getUsage() { return "/village admin <reload|give|manage|debug>"; }

	@Override
	public String getPermission() { return "villages.command.admin"; }

	@Override
	public boolean requireVillage() { return false; }

	@Override
	public Permission getVillagePermission() { return Permission.UNSET; }

	private final AdvancedVillages plugin;

	public AdminCommand(AdvancedVillages plugin) {
		super(plugin, CommandLang.ADMIN);
		this.plugin = plugin;
	}

	@Override
	public void run(Player player, User user, String[] args) {
		CommandConfig commandConfig = plugin.getCommandLang();

		String reloadCmd = commandConfig.getCommand(CommandLang.ADMIN_RELOAD).toLowerCase();
		String manageCmd = commandConfig.getCommand(CommandLang.ADMIN_MANAGE).toLowerCase();
		String giveCmd = commandConfig.getCommand(CommandLang.ADMIN_GIVE).toLowerCase();
		String debugCmd = commandConfig.getCommand(CommandLang.ADMIN_DEBUG).toLowerCase();

		String giveVillageCmd = commandConfig.getCommand(CommandLang.ADMIN_GIVE_VILLAGE).toLowerCase();
		String giveDestroyerCmd = commandConfig.getCommand(CommandLang.ADMIN_GIVE_DESTROYER).toLowerCase();
		String giveDestroyerHearthCmd = commandConfig.getCommand(CommandLang.ADMIN_GIVE_DESTROYER_HEARTH).toLowerCase();
		String giveVillageHearthCmd = commandConfig.getCommand(CommandLang.ADMIN_GIVE_VILLAGE_HEARTH).toLowerCase();
		String giveVillageHearthPartCmd = commandConfig.getCommand(CommandLang.ADMIN_GIVE_VILLAGE_HEARTH_PART).toLowerCase();

		String upgradeCmd = commandConfig.getCommand(CommandLang.ADMIN_UPGRADE).toLowerCase();
		String deleteCmd = commandConfig.getCommand(CommandLang.ADMIN_DELETE).toLowerCase();
		String protectionCmd = commandConfig.getCommand(CommandLang.ADMIN_PROTECTION).toLowerCase();
		String livesCmd = commandConfig.getCommand(CommandLang.ADMIN_LIVES).toLowerCase();
		String bankCmd = commandConfig.getCommand(CommandLang.ADMIN_BANK).toLowerCase();

		String addCmd = commandConfig.getCommand(CommandLang.ADMIN_ADD).toLowerCase();
		String removeCmd = commandConfig.getCommand(CommandLang.ADMIN_REMOVE).toLowerCase();

		if (args.length < 2) {
			sendUsage(player, Lang.COMMAND_ADMIN_USAGE_ROOT, null);
			return;
		}

		String input = args[1].toLowerCase();

		if (input.equals(debugCmd)) {
			if (args.length > 3) {
				sendUsage(player, Lang.COMMAND_ADMIN_USAGE_DEBUG, null);
				return;
			}

			Village village;
			if (args.length == 2) {
				village = plugin.getVillageUtilsManager().getVillageAt(player.getLocation());
				if (village == null) {
					sendLocalized(player, Lang.COMMAND_ADMIN_VILLAGE_NOT_FOUND);
					return;
				}
			} else {
				String owner = args[2];
				Optional<Village> villageOption = plugin.getVillageManager().findByOwner(owner, true);
				if (villageOption.isEmpty()) {
					sendLocalized(player, Lang.COMMAND_VILLAGE_NOT_FOUND, "village_owner", owner);
					return;
				}
				village = villageOption.get();
			}
			sendVillageInfo(player, village);
			return;
		}

		if (input.equals(reloadCmd)) {
			plugin.reloadConfig();
			sendLocalized(player, Lang.COMMAND_RELOAD);
			return;
		}

		if (input.equals(giveCmd)) {
			if (args.length == 3) {
				String giveType = args[2].toLowerCase();

				if (giveType.equals(giveVillageCmd)) {
					ItemStack itemStack = plugin.getApi().createVillageBlock();
					PlayerUtils.giveItem(player, itemStack);
					sendLocalized(player, Lang.COMMAND_ADMIN_GIVE_ITEM, "item", Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName());
					return;
				}

				if (giveType.equals(giveDestroyerHearthCmd)) {
					ItemStack itemStack = plugin.getApi().createDestroyerHearth();
					PlayerUtils.giveItem(player, itemStack);
					sendLocalized(player, Lang.COMMAND_ADMIN_GIVE_ITEM, "item", Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName());
					return;
				}

				if (giveType.equals(giveDestroyerCmd)) {
					ItemStack itemStack = plugin.getApi().createDestroyer();
					PlayerUtils.giveItem(player, itemStack);
					sendLocalized(player, Lang.COMMAND_ADMIN_GIVE_ITEM, "item", Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName());
					return;
				}

				if (giveType.equals(giveVillageHearthPartCmd)) {
					ItemStack itemStack = plugin.getApi().createHearthPart();
					PlayerUtils.giveItem(player, itemStack);
					sendLocalized(player, Lang.COMMAND_ADMIN_GIVE_ITEM, "item", Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName());
					return;
				}

				if (giveType.equals(giveVillageHearthCmd)) {
					ItemStack itemStack = plugin.getApi().createHearth();
					PlayerUtils.giveItem(player, itemStack);
					sendLocalized(player, Lang.COMMAND_ADMIN_GIVE_ITEM, "item", Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName());
					return;
				}
			}
			sendUsage(player, Lang.COMMAND_ADMIN_USAGE_GIVE, null);
			return;
		}

		if (input.equals(manageCmd)) {
			//      0      1     2      3     4   length
			//     -1      0     1      2     3   args
			// /villages admin manage owner type
			if (args.length <= 3) {
				sendUsage(player, Lang.COMMAND_ADMIN_USAGE_MANAGE, null);
				return;
			}
			String owner = args[2];
			Optional<Village> villageOption = plugin.getVillageManager().findByOwner(owner);
			if (villageOption.isEmpty()) {
				sendLocalized(player, Lang.COMMAND_VILLAGE_NOT_FOUND, "village_owner", owner);
				return;
			}
			Village village = villageOption.get();

			if (args.length == 4) {
				String manage = args[3].toLowerCase();
				if (manage.equals(deleteCmd)) {
					plugin.getVillageGui().openGui(village, player, GUIS.REMOVE);
					sendLocalized(player, Lang.COMMAND_ADMIN_DELETE, "village_owner", owner);
				}
				if (manage.equals(upgradeCmd)) {
					int currentLevel = village.getLevel().getLevel();
					Level nextLevel = plugin.getLevelManager().getLevel(currentLevel + 1);
					if (nextLevel == null) {
						sendLocalized(player, Lang.VILLAGE_MAX_LEVEL, "village_owner", owner);
						return;
					}
					if (!plugin.getUpgradeManager().canPasteLevel(nextLevel.getLevel())) {
						sendLocalized(player, Lang.BUILD_EDITOR_SCHEMATIC_MISSING, "schematic", "Turret" + nextLevel.getLevel() + ".schem");
						return;
					}
					VillageUpgradeEvent villageUpgradeEvent = new VillageUpgradeEvent(
							village, player,
							Upgrade.getByLevel(currentLevel),
							Upgrade.getByLevel(nextLevel.getLevel()),
							nextLevel.getCostEconomy());
					plugin.getServer().getPluginManager().callEvent(villageUpgradeEvent);
					if (villageUpgradeEvent.isCancelled()) return;

					if (plugin.getUpgradeManager().upgradeVillage(village)) {
						plugin.getLogManager().record(village, VillageLogType.VILLAGE_UPGRADE, player,
								"level", nextLevel.getLevel());
						sendLocalized(player, Lang.COMMAND_ADMIN_UPGRADE, "village_owner", owner);
					} else {
						sendLocalized(player, Lang.COMMAND_ADMIN_UPGRADE_FAILED);
					}
				}
				if (manage.equals(livesCmd)
						|| manage.equals(protectionCmd)
						|| manage.equals(bankCmd)) {
					sendUsage(player, Lang.COMMAND_ADMIN_USAGE_ACTION, manage);
				}
				return;
			}
			if (args.length == 5) {
				String manage = args[3].toLowerCase();
				String addrem = args[4].toLowerCase();
				if (manage.equals(protectionCmd)) {
					if (addrem.equals(removeCmd)) {
						village.setProtection(Instant.now());
						plugin.getLogManager().record(village, VillageLogType.SETTING_CHANGED, player,
								"setting", "protection", "value", "removed");
						VillageUtilsManager.replaceWith(player, village, Lang.COMMAND_ADMIN_PROTECTION_REMOVE).sendPrefixed(player);
					} else if (addrem.equals(addCmd)) {
						sendUsage(player, Lang.COMMAND_ADMIN_USAGE_DURATION, manage);
					}
				}
				if (manage.equals(bankCmd) || manage.equals(livesCmd)) {
					sendUsage(player, Lang.COMMAND_ADMIN_USAGE_NUMBER, manage);
				}
				return;
			}
			if (args.length == 6) {
				String manage = args[3].toLowerCase();
				String addrem = args[4].toLowerCase();
				Integer number = NumberUtils.isInt(args[5]) ? Integer.parseInt(args[5]) : null;
				if (number==null) {
					sendLocalized(player, Lang.COMMAND_INVALID, "village_owner", owner);
					return;
				}
				if (manage.equals(protectionCmd)) {
					if (addrem.equals(addCmd)) {
						sendUsage(player, Lang.COMMAND_ADMIN_USAGE_DURATION, manage);
					}
				}
				if (manage.equals(bankCmd)) {
					String bank_add = getMessage(Lang.COMMAND_ADMIN_BANK_ADD.getPath()).toText();
					String bank_remove = getMessage(Lang.COMMAND_ADMIN_BANK_REMOVE.getPath()).toText();
					if (addrem.equals(addCmd)) {
						village.addBank(number);
						plugin.getLogManager().record(village, VillageLogType.BANK_DEPOSIT, player,
								"amount", number);
						VillageUtilsManager.replaceWith(player, village, bank_add)
								.with("money", number)
								.sendPrefixed(player);
					}
					if (addrem.equals(removeCmd)) {
						village.removeBank(number);
						plugin.getLogManager().record(village, VillageLogType.BANK_WITHDRAW, player,
								"amount", number);
						VillageUtilsManager.replaceWith(player, village, bank_remove)
								.with("money", number)
								.sendPrefixed(player);
					}
					return;
				}
				if (manage.equals(livesCmd)) {
					String live_add = getMessage(Lang.COMMAND_ADMIN_LIVES_ADD.getPath()).toText();
					String live_remove = getMessage(Lang.COMMAND_ADMIN_LIVES_REMOVE.getPath()).toText();
					if (addrem.equals(addCmd)) {
						village.setLives(village.getLives() + number);
						plugin.getLogManager().record(village, VillageLogType.SETTING_CHANGED, player,
								"setting", "lives", "value", village.getLives());
						VillageUtilsManager.replaceWith(player, village, live_add)
								.with("lives", number)
								.sendPrefixed(player);
					}
					if (addrem.equals(removeCmd)) {
						if (village.getLives() <= 0) {
							sendLocalized(player, Lang.COMMAND_ADMIN_ALREADY_DYING, "village_owner", owner);
							return;
						}
						village.setLives(village.getLives() - number);
						plugin.getLogManager().record(village, VillageLogType.SETTING_CHANGED, player,
								"setting", "lives", "value", village.getLives());
						VillageUtilsManager.replaceWith(player, village, live_remove)
								.with("lives", number)
								.sendPrefixed(player);
					}
					return;
				}
				return;
			}
			if (args.length == 7) {
				String manage = args[3].toLowerCase();
				String addrem = args[4].toLowerCase();
				Integer number = NumberUtils.isInt(args[5]) ? Integer.parseInt(args[5]) : null;
				if (number==null) {
					sendLocalized(player, Lang.COMMAND_INVALID);
					return;
				}
				String unit = args[6].toLowerCase();
				if (manage.equals(protectionCmd)) {
					if (addrem.equals(addCmd)) {
						Duration duration = TimeUtils.getDuration(unit, number);
						if (duration==null) {
							sendLocalized(player, Lang.COMMAND_INVALID);
							return;
						}
						village.setProtection(Instant.now().plus(duration));
						plugin.getLogManager().record(village, VillageLogType.SETTING_CHANGED, player,
								"setting", "protection", "value", number + " " + unit);
						VillageUtilsManager.replaceWith(player, village, Lang.COMMAND_ADMIN_PROTECTION_ADD)
								.with("time", number + " " + unit)
								.with("full_time", Instant.now().plus(duration).toString())
								.sendPrefixed(player);
					}
				}
				return;
			}
		}
		sendLocalized(player, Lang.COMMAND_UNKNOWN, "input", input);
	}

	@Override
	public boolean isTabCompleteAvailable(Player player, User user) {
		return player.isOp() && super.isTabCompleteAvailable(player, user);
	}

	@Override
	public List<String> tabComplete(Player player, User user, String[] args) {
		if (args.length == 2) {
			return List.of(
					getCommand(CommandLang.ADMIN_RELOAD),
					getCommand(CommandLang.ADMIN_GIVE),
					getCommand(CommandLang.ADMIN_MANAGE),
					getCommand(CommandLang.ADMIN_DEBUG)
			);
		}

		if (args.length == 3) {
			if (isCommand(args[1], CommandLang.ADMIN_GIVE)) {
				return List.of(
						getCommand(CommandLang.ADMIN_GIVE_DESTROYER),
						getCommand(CommandLang.ADMIN_GIVE_VILLAGE),
						getCommand(CommandLang.ADMIN_GIVE_DESTROYER_HEARTH),
						getCommand(CommandLang.ADMIN_GIVE_VILLAGE_HEARTH),
						getCommand(CommandLang.ADMIN_GIVE_VILLAGE_HEARTH_PART)
				);
			}
			if (isCommand(args[1], CommandLang.ADMIN_MANAGE)) {
				return sortedVillageOwners();
			}
			if (isCommand(args[1], CommandLang.ADMIN_DEBUG)) {
				return sortedVillageOwners();
			}
		}

		if (!isCommand(args[1], CommandLang.ADMIN_MANAGE)) return List.of();

		if (args.length == 4) {
			return List.of(
					getCommand(CommandLang.ADMIN_UPGRADE),
					getCommand(CommandLang.ADMIN_PROTECTION),
					getCommand(CommandLang.ADMIN_LIVES),
					getCommand(CommandLang.ADMIN_BANK),
					getCommand(CommandLang.ADMIN_DELETE)
			);
		}

		if (args.length == 5 && isNumericAction(args[3])) {
			return List.of(getCommand(CommandLang.ADMIN_ADD), getCommand(CommandLang.ADMIN_REMOVE));
		}

		if (args.length == 6) {
			boolean add = isCommand(args[4], CommandLang.ADMIN_ADD);
			boolean remove = isCommand(args[4], CommandLang.ADMIN_REMOVE);
			if (isCommand(args[3], CommandLang.ADMIN_PROTECTION) && add) {
				return List.of("1", "5", "10", "24");
			}
			if (isCommand(args[3], CommandLang.ADMIN_LIVES) && (add || remove)) {
				return List.of("1", "2", "3");
			}
			if (isCommand(args[3], CommandLang.ADMIN_BANK) && (add || remove)) {
				return List.of("1", "5", "10", "50", "100", "1000");
			}
		}

		if (args.length == 7
				&& isCommand(args[3], CommandLang.ADMIN_PROTECTION)
				&& isCommand(args[4], CommandLang.ADMIN_ADD)) {
			return List.of("seconds", "minutes", "hours", "days");
		}
		return List.of();
	}

	private boolean isNumericAction(String input) {
		return isCommand(input, CommandLang.ADMIN_PROTECTION)
				|| isCommand(input, CommandLang.ADMIN_LIVES)
				|| isCommand(input, CommandLang.ADMIN_BANK);
	}

	private boolean isCommand(String input, CommandLang command) {
		return input.equalsIgnoreCase(getCommand(command));
	}

	private List<String> sortedVillageOwners() {
		return this.plugin.getVillageManager().getVillageOwners().stream()
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.toList();
	}

	private void sendUsage(Player player, Lang message, String action) {
		CommandConfig config = this.plugin.getCommandLang();
		this.plugin.getVillageMessages().format(message,
				"command", config.getCommandName(),
				"admin", config.getCommand(CommandLang.ADMIN),
				"reload", config.getCommand(CommandLang.ADMIN_RELOAD),
				"give", config.getCommand(CommandLang.ADMIN_GIVE),
				"manage", config.getCommand(CommandLang.ADMIN_MANAGE),
				"debug", config.getCommand(CommandLang.ADMIN_DEBUG),
				"village_block", config.getCommand(CommandLang.ADMIN_GIVE_VILLAGE),
				"destroyer", config.getCommand(CommandLang.ADMIN_GIVE_DESTROYER),
				"upgrade", config.getCommand(CommandLang.ADMIN_UPGRADE),
				"lives", config.getCommand(CommandLang.ADMIN_LIVES),
				"protection", config.getCommand(CommandLang.ADMIN_PROTECTION),
				"remove", config.getCommand(CommandLang.ADMIN_REMOVE),
				"add", config.getCommand(CommandLang.ADMIN_ADD),
				"action", action == null ? "" : action
		).sendMessage(player);
	}

	private void sendVillageInfo(Player player, Village village) {
		String notSet = this.plugin.getVillageMessages().text(Lang.COMMAND_ADMIN_DEBUG_NOT_SET);
		String none = this.plugin.getVillageMessages().text(Lang.COMMAND_ADMIN_DEBUG_NONE);
		String location = village.getLocation()
				.map(value -> LocationUtils.convertLocationToString(value, "&7-&c"))
				.orElse(notSet);
		String teleport = village.getHome()
				.map(value -> LocationUtils.convertLocationToString(value, "&7-&c"))
				.orElse(notSet);
		String villageLives = VillageUtilsManager.getLivesSymbol(village.getLives(), false) + " &7(&6" + village.getLives() +"&7)";
		String members = village.getMembersName().stream()
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.collect(Collectors.joining("&7, &6"));
		String protection = village.getProtection().isAfter(Instant.now())
				? TimeUtils.getStringDate(village.getProtection().getEpochSecond())
				: none;
		String allies = none;
		int wars = 0;
		if (this.plugin.getDiplomacyManager() != null && this.plugin.getDiplomacyManager().isEnabled()) {
			String alliesTags = this.plugin.getDiplomacyManager().getAlliesTags(village);
			allies = alliesTags.isBlank() ? none : alliesTags;
			wars = this.plugin.getDiplomacyManager().countCurrentWars(village);
		}
		int missedPayments = this.plugin.getUpkeepManager() != null && this.plugin.getUpkeepManager().isEnabled()
				? this.plugin.getUpkeepManager().getMissedPayments(village)
				: 0;
		Level level = village.getLevel();
		User owner = village.getOwner();

		this.plugin.getVillageMessages().format(Lang.COMMAND_ADMIN_DEBUG_INFO,
				"name", village.getName(),
				"uuid", village.getID(),
				"tag", village.isTag() ? village.getTag() : none,
				"owner", owner == null ? notSet : owner.getName(),
				"level", level == null ? notSet : level.getLevel(),
				"size", level == null ? notSet : level.getSize(),
				"location", location,
				"home", teleport,
				"lives", villageLives,
				"bank", village.getBank(),
				"members", members.isBlank() ? none : members,
				"online_members", village.getOnlineMembers().size(),
				"protection", protection,
				"allies", allies,
				"wars", wars,
				"missed_payments", missedPayments,
				"pvp", this.plugin.getVillageMessages().text(village.hasPvPEnabled() ? Lang.ON : Lang.OFF),
				"tnt", this.plugin.getVillageMessages().text(village.hasTntEnabled() ? Lang.ON : Lang.OFF),
				"animations", this.plugin.getVillageMessages().text(village.isAnimationsEnabled() ? Lang.ON : Lang.OFF),
				"persistence", this.plugin.getVillageMessages().text(village.wasChanged()
						? Lang.COMMAND_ADMIN_DEBUG_DIRTY : Lang.COMMAND_ADMIN_DEBUG_SAVED),
				"version", village.getChangeVersion()
		).sendMessage(player);
	}
}
