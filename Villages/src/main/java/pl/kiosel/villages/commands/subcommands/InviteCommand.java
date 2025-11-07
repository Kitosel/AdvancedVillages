package pl.kiosel.villages.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.kiosel.core.commands.SubCommand;
import pl.kiosel.core.locale.Locale;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageManager;

public class InviteCommand extends SubCommand {

    @Override
    public String getName() { return "leave"; }

    @Override
    public String getDescription() { return "Leave village"; }

    @Override
    public String getUsage() { return "/village leave"; }

	@Override
	public String getPermission() { return "villages.command.invite"; }

	private final AdvancedVillages plugin;

	public InviteCommand(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	@Override
    public void run(Player player, String[] args) {
		Locale locale = plugin.getLocale();
        Village village = VillageManager.getVillageByOfflineOwner(player.getName());
        if (village == null) {
			locale.getMessage(Lang.VILLAGE_NO.getPath()).sendPrefixedMessage(player);
            return;
        }

		if (args.length == 2) {
			Player invite = Bukkit.getPlayer(args[1]);
			if (invite == null || !invite.isOnline()) {
				locale.getMessage(Lang.PLAYER_OFFLINE.getPath()).sendPrefixedMessage(player);
				return;
			}

			if (plugin.getVillageManager().hasVillage(invite) || village.isMember(invite)) {
				locale.getMessage(Lang.HAS_VILLAGE.getPath()).sendPrefixedMessage(player);
				return;
			}

			if(plugin.getInviteManager().isPlayerInvited(invite)) {
				locale.getMessage(Lang.HAS_INVITE.getPath()).sendPrefixedMessage(player);
			} else {
				locale.getMessage(Lang.PLAYER_INVITE.getPath()).processPlaceholder("player", invite.getName()).sendPrefixedMessage(player);
				locale.getMessage(Lang.PLAYER_TARGET.getPath()).processPlaceholder("player", player.getName()).sendPrefixedMessage(invite);
				plugin.getInviteManager().invitePlayer(village, invite);
			}
		}
    }
}