package pl.kiosel.villages.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.kiosel.villages.Wioski;
import pl.kiosel.common.command.SubCommand;
import pl.kiosel.villages.config.Language;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.village.Village;
import pl.kiosel.villages.village.VillageManager;

public class InviteCommand extends SubCommand {

    @Override
    public String getName() { return "leave"; }

    @Override
    public String getDescription() { return "Leave village"; }

    @Override
    public String getUsage() { return "/village leave"; }

	@Override
	public String getPermission() { return "villages.command.invite"; }

	@Override
    public void run(Player player, Wioski plugin, String[] args) {
        Language lang = plugin.getLang();
        Village village = VillageManager.getVillageByOfflineOwner(player.getName());
        if (village == null) {
            player.sendMessage(lang.getMessage(Lang.VILLAGE_NO));
            return;
        }

		if (args.length == 2) {
			Player invite = Bukkit.getPlayer(args[1]);
			if (invite == null || !invite.isOnline()) {
				player.sendMessage(lang.getMessage(Lang.PLAYER_OFFLINE));
				return;
			}

			if (plugin.getVillageManager().hasVillage(invite) || village.isMember(invite)) {
				player.sendMessage(lang.getMessage(Lang.HAS_VILLAGE));
				return;
			}

			if(plugin.getInviteManager().isPlayerInvited(invite)) {
				player.sendMessage(lang.getMessage(Lang.HAS_INVITE));
			} else {
				player.sendMessage(lang.getMessage(Lang.PLAYER_INVITE).replace("%TARGET%", invite.getName()));
				invite.sendMessage(lang.getMessage(Lang.PLAYER_TARGET).replace("%PLAYER%", player.getName()));
				plugin.getInviteManager().invitePlayer(village, invite);
			}
		}
    }
}