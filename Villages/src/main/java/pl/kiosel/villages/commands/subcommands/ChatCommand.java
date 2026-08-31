package pl.kiosel.villages.commands.subcommands;

import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.config.CommandLang;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Permission;

public class ChatCommand extends AVSubCommand {

	@Override
	public String getDescription() { return "Chat command"; }

	@Override
	public String getUsage() { return "/village chat *message*"; }

	@Override
	public String getPermission() { return "villages.command.chat"; }

	@Override
	public boolean requireVillage() { return true; }

	@Override
	public Permission getVillagePermission() { return Permission.UNSET; }

	private final AdvancedVillages plugin;

	public ChatCommand(AdvancedVillages plugin) {
		super(plugin, CommandLang.CHAT);
		this.plugin = plugin;
	}

	@Override
	public void run(Player player, User user, String[] args) {
		if (args.length < 2) {
			getMessage(Lang.COMMAND_USAGE_CHAT.getPath())
					.with("command", plugin.getCommandLang().getCommandName())
					.with("chat", plugin.getCommandLang().getCommand(CommandLang.CHAT))
					.sendPrefixed(player);
			return;
		}
		String message = String.join(" ", args).substring(args[0].length()).trim();
		String formatted = getMessage(Lang.VILLAGE_CHAT_FORMAT.getPath()).
				with("player", player.getName()).
				with("message", message).toString();
		user.getPresentVillage().broadcast(formatted);
	}
}
