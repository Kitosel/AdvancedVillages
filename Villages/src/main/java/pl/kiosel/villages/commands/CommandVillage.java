package pl.kiosel.villages.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.command.RosaCommand;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.commands.subcommands.*;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandVillage extends RosaCommand {

	private final AdvancedVillages plugin;
	private volatile List<AVSubCommand> villageSubCommands = Collections.emptyList();

	public CommandVillage(AdvancedVillages plugin) {
		super(plugin, plugin.getCommandLang().getCommandName(), plugin.getCommandLang().getCommandAliases(),
				plugin.getCommandLang().getCommandPermission());
		this.plugin = plugin;
		reloadArguments();
	}

	public synchronized void reloadArguments() {
		List<AVSubCommand> refreshed = Arrays.asList(
				new AdminCommand(plugin),
				new ChatCommand(plugin),
				new InviteCommand(plugin),
				new LeaveCommand(plugin),
				new RequestCommand(plugin),
				new TpCommand(plugin),
				new BuildEditCommand(plugin),
				new AllianceCommand(plugin),
				new WarCommand(plugin),
				new HelpCommand(plugin, this),
				new TeleportSetCommand(plugin)
		);
		setSubCommands(refreshed);
		this.villageSubCommands = Collections.unmodifiableList(refreshed);
	}

	@Override
	public boolean onExecute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			plugin.reloadConfig();
			plugin.getVillageMessages().get(Lang.COMMAND_RELOAD).sendPrefixed(sender);
			return true;
		}

		help((Player) sender);
		return true;
	}

	@Override
	protected boolean onUnknownSubCommand(CommandSender sender, String label, String[] args) {
		if (sender instanceof Player) {
			help((Player) sender);
			return true;
		}
		return super.onUnknownSubCommand(sender, label, args);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		return EMPTY;
	}

	public void help(Player sender) {
		User user = plugin.getUserManager().findByPlayer(sender).orElse(null);
		plugin.getVillageMessages().send(sender, Lang.SEPARATOR);
		if (user != null) {
			for (AVSubCommand subCommand : villageSubCommands) {
				if (subCommand.showInHelp() && subCommand.isTabCompleteAvailable(sender, user)) {
					sender.sendMessage(subCommand.getUsage() + " - " + subCommand.getDescription());
				}
			}
		}
		plugin.getVillageMessages().send(sender, Lang.SEPARATOR);
	}
}
