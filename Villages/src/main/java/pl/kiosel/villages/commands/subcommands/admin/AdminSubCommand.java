package pl.kiosel.villages.commands.subcommands.admin;

import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.config.CommandConfig;
import pl.kiosel.villages.config.CommandLang;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Permission;

import java.util.List;

public abstract class AdminSubCommand extends AVSubCommand {

	protected AdminSubCommand(AdvancedVillages plugin, CommandLang commandKey) {
		super(plugin, commandKey);
	}

	@Override
	public String getPermission() {
		return "villages.command.admin";
	}

	@Override
	public boolean requireVillage() {
		return false;
	}

	@Override
	public Permission getVillagePermission() {
		return Permission.UNSET;
	}

	@Override
	public boolean isTabCompleteAvailable(Player player, User user) {
		return player.isOp() && super.isTabCompleteAvailable(player, user);
	}

	protected final boolean isCommand(String input, CommandLang command) {
		return input != null && input.equalsIgnoreCase(getCommand(command));
	}

	protected final List<String> sortedVillageOwners() {
		return this.plugin.getVillageManager().getVillageOwners().stream()
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.toList();
	}

	protected final void sendAdminUsage(Player player, Lang message, String action) {
		CommandConfig config = this.plugin.getCommandLang();
		this.plugin.getVillageMessages().format(message,
				"command", config.getCommandName(),
				"admin", config.getCommand(CommandLang.ADMIN),
				"reload", config.getCommand(CommandLang.ADMIN_RELOAD),
				"give", config.getCommand(CommandLang.ADMIN_GIVE),
				"manage", config.getCommand(CommandLang.ADMIN_MANAGE),
				"debug", config.getCommand(CommandLang.ADMIN_DEBUG),
				"integration", config.getCommand(CommandLang.ADMIN_INTEGRATION),
				"tablist", config.getCommand(CommandLang.ADMIN_INTEGRATION_TABLIST),
				"install", config.getCommand(CommandLang.ADMIN_INTEGRATION_INSTALL),
				"restore", config.getCommand(CommandLang.ADMIN_INTEGRATION_RESTORE),
				"status", config.getCommand(CommandLang.ADMIN_INTEGRATION_STATUS),
				"village_block", config.getCommand(CommandLang.ADMIN_GIVE_VILLAGE),
				"destroyer", config.getCommand(CommandLang.ADMIN_GIVE_DESTROYER),
				"upgrade", config.getCommand(CommandLang.ADMIN_UPGRADE),
				"lives", config.getCommand(CommandLang.ADMIN_LIVES),
				"protection", config.getCommand(CommandLang.ADMIN_PROTECTION),
				"bank", config.getCommand(CommandLang.ADMIN_BANK),
				"remove", config.getCommand(CommandLang.ADMIN_REMOVE),
				"add", config.getCommand(CommandLang.ADMIN_ADD),
				"action", action == null ? "" : action
		).sendMessage(player);
	}
}
