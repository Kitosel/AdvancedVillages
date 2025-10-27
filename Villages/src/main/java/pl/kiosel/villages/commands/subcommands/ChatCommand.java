package pl.kiosel.villages.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.kiosel.villages.Wioski;
import pl.kiosel.common.command.SubCommand;
import pl.kiosel.villages.config.Config;
import pl.kiosel.villages.enums.CommandLang;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.village.Village;
import pl.kiosel.villages.village.VillageManager;
import pl.kiosel.villages.village.VillageMember;

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

	@Override
	public void run(Player sender, Wioski plugin, String[] args) {
		if (args.length < 2) {
			sender.sendMessage("/" + plugin.getCommandLang().getCommandName() + " " + plugin.getCommandLang().getCommand(CommandLang.CHAT) + " *message*");
			return;
		}
		Village village = VillageManager.getVillageByOfflineOwner(sender.getName());
		if (village == null) {
			sender.sendMessage(plugin.getLang().getMessage(Lang.VILLAGE_NO));
			return;
		}
		String message = String.join(" ", args).substring(args[0].length()).trim();
		String formatted = Config.village_chat_format.replace("%player_name%", sender.getName()).replace("%message%", message);

		for (UUID uuid : village.getMembers()) {
			Player target = Bukkit.getPlayer(uuid);
			if (target != null && target.isOnline()) {
				target.sendMessage(formatted);
			}
		}
	}
}