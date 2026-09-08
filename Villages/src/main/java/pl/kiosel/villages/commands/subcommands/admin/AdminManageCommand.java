package pl.kiosel.villages.commands.subcommands.admin;

import org.bukkit.entity.Player;
import pl.kiosel.rosacore.utils.NumberUtils;
import pl.kiosel.rosacore.utils.TimeUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.features.logs.VillageLogType;
import pl.kiosel.villages.api.events.VillageUpgradeEvent;
import pl.kiosel.villages.config.CommandLang;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Upgrade;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.level.Level;
import pl.kiosel.villages.gui.GUIS;
import pl.kiosel.villages.manager.VillageUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public final class AdminManageCommand extends AdminSubCommand {

	public AdminManageCommand(AdvancedVillages plugin) {
		super(plugin, CommandLang.ADMIN_MANAGE);
	}

	@Override
	public String getDescription() {
		return "Manages a village";
	}

	@Override
	public String getUsage() {
		return "/village admin manage <owner> <action>";
	}

	@Override
	public void run(Player player, User user, String[] args) {
		if (args.length <= 2) {
			sendAdminUsage(player, Lang.COMMAND_ADMIN_USAGE_MANAGE, null);
			return;
		}

		String ownerName = args[1];
		Optional<Village> villageOption = this.plugin.getVillageManager().findByOwner(ownerName, true);
		if (villageOption.isEmpty()) {
			sendLocalized(player, Lang.COMMAND_VILLAGE_NOT_FOUND, "village_owner", ownerName);
			return;
		}

		Village village = villageOption.get();
		switch (args.length) {
			case 3:
				handleAction(player, village, ownerName, args[2]);
				break;
			case 4:
				handleOperation(player, village, args[2], args[3]);
				break;
			case 5:
				handleNumber(player, village, ownerName, args[2], args[3], args[4]);
				break;
			case 6:
				handleDuration(player, village, args[2], args[3], args[4], args[5]);
				break;
			default:
				sendAdminUsage(player, Lang.COMMAND_ADMIN_USAGE_MANAGE, null);
		}
	}

	@Override
	public List<String> tabComplete(Player player, User user, String[] args) {
		if (args.length == 2) {
			return sortedVillageOwners();
		}
		if (args.length == 3) {
			return List.of(
					getCommand(CommandLang.ADMIN_UPGRADE),
					getCommand(CommandLang.ADMIN_PROTECTION),
					getCommand(CommandLang.ADMIN_LIVES),
					getCommand(CommandLang.ADMIN_BANK),
					getCommand(CommandLang.ADMIN_DELETE)
			);
		}
		if (args.length == 4 && isNumericAction(args[2])) {
			return List.of(getCommand(CommandLang.ADMIN_ADD), getCommand(CommandLang.ADMIN_REMOVE));
		}
		if (args.length == 5) {
			boolean add = isCommand(args[3], CommandLang.ADMIN_ADD);
			boolean remove = isCommand(args[3], CommandLang.ADMIN_REMOVE);
			if (isCommand(args[2], CommandLang.ADMIN_PROTECTION) && add) {
				return List.of("1", "5", "10", "24");
			}
			if (isCommand(args[2], CommandLang.ADMIN_LIVES) && (add || remove)) {
				return List.of("1", "2", "3");
			}
			if (isCommand(args[2], CommandLang.ADMIN_BANK) && (add || remove)) {
				return List.of("1", "5", "10", "50", "100", "1000");
			}
		}
		if (args.length == 6
				&& isCommand(args[2], CommandLang.ADMIN_PROTECTION)
				&& isCommand(args[3], CommandLang.ADMIN_ADD)) {
			return List.of("seconds", "minutes", "hours", "days");
		}
		return List.of();
	}

	private void handleAction(Player player, Village village, String ownerName, String action) {
		if (isCommand(action, CommandLang.ADMIN_DELETE)) {
			this.plugin.getVillageGui().openGui(village, player, GUIS.REMOVE);
			sendLocalized(player, Lang.COMMAND_ADMIN_DELETE, "village_owner", ownerName);
			return;
		}
		if (isCommand(action, CommandLang.ADMIN_UPGRADE)) {
			upgrade(player, village, ownerName);
			return;
		}
		if (isNumericAction(action)) {
			sendAdminUsage(player, Lang.COMMAND_ADMIN_USAGE_ACTION, action);
			return;
		}
		sendLocalized(player, Lang.COMMAND_UNKNOWN, "input", action);
	}

	private void handleOperation(Player player, Village village, String action, String operation) {
		if (isCommand(action, CommandLang.ADMIN_PROTECTION)) {
			if (isCommand(operation, CommandLang.ADMIN_REMOVE)) {
				village.setProtection(Instant.now());
				this.plugin.getLogManager().record(village, VillageLogType.SETTING_CHANGED, player,
						"setting", "protection", "value", "removed");
				VillageUtils.replaceWith(player, village, Lang.COMMAND_ADMIN_PROTECTION_REMOVE)
						.sendPrefixed(player);
				return;
			}
			if (isCommand(operation, CommandLang.ADMIN_ADD)) {
				sendAdminUsage(player, Lang.COMMAND_ADMIN_USAGE_DURATION, action);
				return;
			}
		}
		if (isCommand(action, CommandLang.ADMIN_BANK) || isCommand(action, CommandLang.ADMIN_LIVES)) {
			sendAdminUsage(player, Lang.COMMAND_ADMIN_USAGE_NUMBER, action);
			return;
		}
		sendAdminUsage(player, Lang.COMMAND_ADMIN_USAGE_ACTION, action);
	}

	private void handleNumber(Player player, Village village, String ownerName,
	                          String action, String operation, String input) {
		Integer number = NumberUtils.isInt(input) ? Integer.parseInt(input) : null;
		if (number == null || number < 0) {
			sendLocalized(player, Lang.COMMAND_INVALID);
			return;
		}

		if (isCommand(action, CommandLang.ADMIN_PROTECTION)
				&& isCommand(operation, CommandLang.ADMIN_ADD)) {
			sendAdminUsage(player, Lang.COMMAND_ADMIN_USAGE_DURATION, action);
			return;
		}
		if (isCommand(action, CommandLang.ADMIN_BANK)) {
			updateBank(player, village, operation, number);
			return;
		}
		if (isCommand(action, CommandLang.ADMIN_LIVES)) {
			updateLives(player, village, ownerName, operation, number);
			return;
		}
		sendAdminUsage(player, Lang.COMMAND_ADMIN_USAGE_NUMBER, action);
	}

	private void handleDuration(Player player, Village village, String action,
	                            String operation, String numberInput, String unit) {
		if (!isCommand(action, CommandLang.ADMIN_PROTECTION)
				|| !isCommand(operation, CommandLang.ADMIN_ADD)) {
			sendAdminUsage(player, Lang.COMMAND_ADMIN_USAGE_MANAGE, null);
			return;
		}

		Integer number = NumberUtils.isInt(numberInput) ? Integer.parseInt(numberInput) : null;
		if (number == null || number < 0) {
			sendLocalized(player, Lang.COMMAND_INVALID);
			return;
		}
		Duration duration = TimeUtils.getDuration(unit.toLowerCase(), number);
		if (duration == null) {
			sendLocalized(player, Lang.COMMAND_INVALID);
			return;
		}

		Instant protectionUntil = Instant.now().plus(duration);
		village.setProtection(protectionUntil);
		this.plugin.getLogManager().record(village, VillageLogType.SETTING_CHANGED, player,
				"setting", "protection", "value", number + " " + unit);
		VillageUtils.replaceWith(player, village, Lang.COMMAND_ADMIN_PROTECTION_ADD)
				.with("time", number + " " + unit)
				.with("full_time", protectionUntil.toString())
				.sendPrefixed(player);
	}

	private void upgrade(Player player, Village village, String ownerName) {
		int currentLevel = village.getLevel().getLevel();
		Level nextLevel = this.plugin.getLevelManager().getLevel(currentLevel + 1);
		if (nextLevel == null) {
			sendLocalized(player, Lang.VILLAGE_MAX_LEVEL, "village_owner", ownerName);
			return;
		}
		if (!this.plugin.getUpgradeManager().canPasteLevel(nextLevel.getLevel())) {
			sendLocalized(player, Lang.BUILD_EDITOR_SCHEMATIC_MISSING,
					"schematic", "Turret" + nextLevel.getLevel() + ".schem");
			return;
		}

		VillageUpgradeEvent event = new VillageUpgradeEvent(
				village,
				player,
				Upgrade.getByLevel(currentLevel),
				Upgrade.getByLevel(nextLevel.getLevel()),
				nextLevel.getCostEconomy()
		);
		this.plugin.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) return;

		if (this.plugin.getUpgradeManager().upgradeVillage(village)) {
			this.plugin.getLogManager().record(village, VillageLogType.VILLAGE_UPGRADE, player,
					"level", nextLevel.getLevel());
			sendLocalized(player, Lang.COMMAND_ADMIN_UPGRADE, "village_owner", ownerName);
		} else {
			sendLocalized(player, Lang.COMMAND_ADMIN_UPGRADE_FAILED);
		}
	}

	private void updateBank(Player player, Village village, String operation, int amount) {
		if (isCommand(operation, CommandLang.ADMIN_ADD)) {
			village.addBank(amount);
			this.plugin.getLogManager().record(village, VillageLogType.BANK_DEPOSIT, player,
					"amount", amount);
			VillageUtils.replaceWith(player, village, Lang.COMMAND_ADMIN_BANK_ADD)
					.with("money", amount)
					.sendPrefixed(player);
			return;
		}
		if (isCommand(operation, CommandLang.ADMIN_REMOVE)) {
			village.removeBank(amount);
			this.plugin.getLogManager().record(village, VillageLogType.BANK_WITHDRAW, player,
					"amount", amount);
			VillageUtils.replaceWith(player, village, Lang.COMMAND_ADMIN_BANK_REMOVE)
					.with("money", amount)
					.sendPrefixed(player);
		}
	}

	private void updateLives(Player player, Village village, String ownerName,
	                         String operation, int amount) {
		if (isCommand(operation, CommandLang.ADMIN_ADD)) {
			village.setLives(village.getLives() + amount);
			this.plugin.getLogManager().record(village, VillageLogType.SETTING_CHANGED, player,
					"setting", "lives", "value", village.getLives());
			VillageUtils.replaceWith(player, village, Lang.COMMAND_ADMIN_LIVES_ADD)
					.with("lives", amount)
					.sendPrefixed(player);
			return;
		}
		if (!isCommand(operation, CommandLang.ADMIN_REMOVE)) return;
		if (village.getLives() <= 0) {
			sendLocalized(player, Lang.COMMAND_ADMIN_ALREADY_DYING, "village_owner", ownerName);
			return;
		}

		village.setLives(village.getLives() - amount);
		this.plugin.getLogManager().record(village, VillageLogType.SETTING_CHANGED, player,
				"setting", "lives", "value", village.getLives());
		VillageUtils.replaceWith(player, village, Lang.COMMAND_ADMIN_LIVES_REMOVE)
				.with("lives", amount)
				.sendPrefixed(player);
	}

	private boolean isNumericAction(String input) {
		return isCommand(input, CommandLang.ADMIN_PROTECTION)
				|| isCommand(input, CommandLang.ADMIN_LIVES)
				|| isCommand(input, CommandLang.ADMIN_BANK);
	}
}
