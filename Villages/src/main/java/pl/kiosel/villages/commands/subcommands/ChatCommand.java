package pl.kiosel.villages.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.kiosel.core.commands.SubCommand;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.CommandLang;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageManager;

import java.util.UUID;

public class ChatCommand extends SubCommand {

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
	public void run(Player sender, String[] args) {
		if (args.length < 2) {
			sender.sendMessage("/" + plugin.getCommandLang().getCommandName() + " " + plugin.getCommandLang().getCommand(CommandLang.CHAT) + " *message*");
			return;
		}
		Village village = VillageManager.getVillageByOfflineOwner(sender.getName());
		if (village == null) {
			plugin.getLocale().getMessage(Lang.VILLAGE_NO.getPath()).sendPrefixedMessage(sender);
			return;
		}
		String message = String.join(" ", args).substring(args[0].length()).trim();
		String formatted = plugin.getLocale().getMessage(Lang.VILLAGE_CHAT_FORMAT.getPath()).
				processPlaceholder("player", sender.getName()).
				processPlaceholder("message", message).toString();

		for (UUID uuid : village.getMembers()) {
			Player target = Bukkit.getPlayer(uuid);
			if (target != null && target.isOnline()) {
				target.sendMessage(formatted);
			}
		}
	}
}