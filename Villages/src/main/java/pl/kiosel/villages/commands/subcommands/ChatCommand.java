package pl.kiosel.villages.commands.subcommands;

import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.enums.CommandLang;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.data.village.Village;

public class ChatCommand extends AVSubCommand {

	@Override
	public String getName() { return "chat"; }

	@Override
	public String getDescription() { return "Chat command"; }

	@Override
	public String getUsage() { return "/village chat *message*"; }

	@Override
	public String getPermission() { return "villages.command.chat"; }

	private final AdvancedVillages plugin;

	public ChatCommand(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run(Player player, User user, String[] args) {
		if (args.length < 2) {
			plugin.getLocale().getMessage(Lang.COMMAND_USAGE_CHAT.getPath())
					.processPlaceholder("command", plugin.getCommandLang().getCommandName())
					.processPlaceholder("chat", plugin.getCommandLang().getCommand(CommandLang.CHAT))
					.sendPrefixedMessage(player);
			return;
		}
		Village village = user.getPresentVillage();
		if (village == null) {
			plugin.getLocale().getMessage(Lang.VILLAGE_NO.getPath()).sendPrefixedMessage(player);
			return;
		}
		String message = String.join(" ", args).substring(args[0].length()).trim();
		String formatted = plugin.getLocale().getMessage(Lang.VILLAGE_CHAT_FORMAT.getPath()).
				processPlaceholder("player", player.getName()).
				processPlaceholder("message", message).toString();
		village.broadcast(formatted);
	}
}
