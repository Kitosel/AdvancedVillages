package pl.kiosel.villages.commands.subcommands;

import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.commands.CommandVillage;
import pl.kiosel.villages.config.CommandLang;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.VillagePermission;

public final class HelpCommand extends AVSubCommand {

	private final CommandVillage command;

	public HelpCommand(AdvancedVillages plugin, CommandVillage command) {
		super(plugin, CommandLang.HELP);
		this.command = command;
	}

	@Override
	public String getDescription() {
		return "Show command help";
	}

	@Override
	public String getUsage() {
		return "/" + plugin.getCommandLang().getCommandName() + " " + getName();
	}

	@Override
	public String getPermission() {
		return "villages.command.help";
	}

	@Override
	public boolean requireVillage() {
		return false;
	}

	@Override
	public VillagePermission getVillagePermission() {
		return VillagePermission.UNSET;
	}

	@Override
	public boolean showInHelp() {
		return false;
	}

	@Override
	public void run(Player player, User user, String[] args) {
		command.help(player);
	}
}
