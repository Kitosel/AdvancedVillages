package pl.kiosel.villages.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.commands.subcommands.admin.*;
import pl.kiosel.villages.config.CommandLang;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;

public final class AdminCommand extends AdminSubCommand {

	public AdminCommand(AdvancedVillages plugin) {
		super(plugin, CommandLang.ADMIN);
		addSubCommand(new AdminReloadCommand(plugin));
		addSubCommand(new AdminGiveCommand(plugin));
		addSubCommand(new AdminManageCommand(plugin));
		addSubCommand(new AdminDebugCommand(plugin));
		addSubCommand(new AdminIntegrationCommand(plugin));
		if (plugin.isDev())
			addSubCommand(new AdminSettingsCommand(plugin));
	}

	@Override
	public String getPermission() {
		return "advancedvillages.command.admin";
	}

	@Override
	public String getDescription() {
		return "Manages the AdvancedVillages plugin";
	}

	@Override
	public String getUsage() {
		return "/village admin <reload|give|manage|debug|integration>";
	}

	@Override
	public void run(Player player, User user, String[] args) {
		sendAdminUsage(player, Lang.COMMAND_ADMIN_USAGE_ROOT, null);
	}

	@Override
	protected void onUnknownSubCommand(CommandSender sender, String input) {
		sendLocalized(sender, Lang.COMMAND_UNKNOWN, "input", input);
	}
}
