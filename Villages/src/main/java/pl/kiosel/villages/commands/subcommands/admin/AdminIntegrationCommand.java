package pl.kiosel.villages.commands.subcommands.admin;

import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.CommandLang;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.integrations.TabListIntegration;

import java.util.ArrayList;
import java.util.List;

public class AdminIntegrationCommand extends AdminSubCommand {

	private final List<String> tablistPlugins = new ArrayList<>();

	public AdminIntegrationCommand(AdvancedVillages plugin) {
		super(plugin, CommandLang.ADMIN_INTEGRATION);
		this.tablistPlugins.add("AdvancedPlayerList");
	}

	@Override
	public String getDescription() {
		return "Integration with other plugins";
	}

	@Override
	public String getUsage() {
		return "/village admin integration <plugin> <action>";
	}

	@Override
	public void run(Player player, User user, String[] args) {
		if (args.length != 4
				|| !isCommand(args[1], CommandLang.ADMIN_INTEGRATION_TABLIST)
				|| !"AdvancedPlayerList".equalsIgnoreCase(args[2])) {
			sendAdminUsage(player, Lang.COMMAND_ADMIN_USAGE_INTEGRATION, null);
			return;
		}

		TabListIntegration integration = plugin.getIntegrationManager().getTabListIntegration();
		if (integration == null) {
			sendLocalized(player, Lang.COMMAND_ADMIN_INTEGRATION_UNAVAILABLE);
			return;
		}

		try {
			if (isCommand(args[3], CommandLang.ADMIN_INTEGRATION_INSTALL)) {
				integration.installProfile();
				sendLocalized(player, Lang.COMMAND_ADMIN_INTEGRATION_INSTALLED);
				return;
			}
			if (isCommand(args[3], CommandLang.ADMIN_INTEGRATION_RESTORE)) {
				String profile = integration.restorePreviousProfile();
				sendLocalized(player, Lang.COMMAND_ADMIN_INTEGRATION_RESTORED, "profile", profile);
				return;
			}
			if (isCommand(args[3], CommandLang.ADMIN_INTEGRATION_STATUS)) {
				plugin.getVillageMessages().format(Lang.COMMAND_ADMIN_INTEGRATION_STATUS,
						"installed", integration.isProfileInstalled(),
						"active", integration.isProfileActive(),
						"profile", integration.getActiveProfile()).sendMessage(player);
				return;
			}
			sendAdminUsage(player, Lang.COMMAND_ADMIN_USAGE_INTEGRATION, null);
		} catch (RuntimeException exception) {
			plugin.getRosaLogger().warning("AdvancedPlayerList integration command failed: " + exception.getMessage());
			sendLocalized(player, Lang.COMMAND_ADMIN_INTEGRATION_ERROR, "error", exception.getMessage());
		}
	}

	@Override
	public List<String> tabComplete(Player player, User user, String[] args) {
		if (args.length == 2) {
			return complete(args[1], List.of(getCommand(CommandLang.ADMIN_INTEGRATION_TABLIST)));
		}
		if (args.length == 3) {
			return complete(args[2], getIntegration(args[1]));
		}
		if (args.length == 4) {
			return complete(args[3], List.of(
					getCommand(CommandLang.ADMIN_INTEGRATION_INSTALL),
					getCommand(CommandLang.ADMIN_INTEGRATION_RESTORE),
					getCommand(CommandLang.ADMIN_INTEGRATION_STATUS)
			));
		}
		return EMPTY;
	}

	private List<String> getIntegration(String args) {
		if (isCommand(args, CommandLang.ADMIN_INTEGRATION_TABLIST)) {
			return this.tablistPlugins;
		}
		return EMPTY;
	}

}
