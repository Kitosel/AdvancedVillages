package pl.kiosel.villages.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.kiosel.core.locale.Locale;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.settings.Settings;

public class InviteCommand extends AVSubCommand {

    @Override
    public String getName() { return "invite"; }

    @Override
    public String getDescription() { return "Invite a player to your village"; }

    @Override
    public String getUsage() { return "/village invite <player>"; }

	@Override
	public String getPermission() { return "villages.command.invite"; }

	private final AdvancedVillages plugin;

	public InviteCommand(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run(Player player, User user, String[] args) {
		Locale locale = plugin.getLocale();
		Village village = user.getPresentVillage();
        if (village == null) {
			locale.getMessage(Lang.VILLAGE_NO.getPath()).sendPrefixedMessage(player);
            return;
        }

		if (village.getMembers().size() >= Settings.VILLAGE_MAX_MEMBERS.getInt()) {
			locale.getMessage(Lang.MAX_MEMBERS.getPath()).sendPrefixedMessage(player);
			return;
		}

		if (args.length == 2) {
			Player invite = Bukkit.getPlayer(args[1]);
			if (invite == null || !invite.isOnline()) {
				locale.getMessage(Lang.PLAYER_OFFLINE.getPath()).sendPrefixedMessage(player);
				return;
			}

			User inviteUser = plugin.getUserManager().findByPlayer(invite).orNull();
			if (inviteUser == null) {
				locale.getMessage(Lang.PLAYER_NOT_FOUND.getPath()).sendPrefixedMessage(player);
				return;
			}
			if (inviteUser.hasVillage()) {
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
			return;
		}

		locale.getMessage(Lang.COMMAND_USAGE_INVITE.getPath()).sendPrefixedMessage(player);
    }
}
