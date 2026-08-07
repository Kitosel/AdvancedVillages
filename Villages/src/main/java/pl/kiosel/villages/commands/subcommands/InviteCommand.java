package pl.kiosel.villages.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Permission;
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

	@Override
	public Permission getVillagePermission() { return Permission.INVITE; }

	private final AdvancedVillages plugin;

	public InviteCommand(AdvancedVillages plugin) {
		super(plugin);
		this.plugin = plugin;
	}

	@Override
	public void run(Player player, User user, String[] args) {
		Village village = user.getPresentVillage();
        if (village == null) {
			sendLocalized(player, Lang.VILLAGE_NO);
            return;
        }

		if (village.getMembers().size() >= Settings.VILLAGE_MAX_MEMBERS.getInt()) {
			sendLocalized(player, Lang.MAX_MEMBERS);
			return;
		}

		if (args.length == 2) {
			Player invite = Bukkit.getPlayer(args[1]);
			if (invite == null || !invite.isOnline()) {
				sendLocalized(player, Lang.PLAYER_OFFLINE);
				return;
			}

			User inviteUser = plugin.getUserManager().findByPlayer(invite).orNull();
			if (inviteUser == null) {
				sendLocalized(player, Lang.PLAYER_NOT_FOUND);
				return;
			}
			if (inviteUser.hasVillage()) {
				sendLocalized(player, Lang.HAS_VILLAGE);
				return;
			}

			if(plugin.getInviteManager().isPlayerInvited(invite)) {
				sendLocalized(player, Lang.HAS_INVITE);
			} else {
				sendLocalized(player, Lang.PLAYER_INVITE, "player", invite.getName());
				sendLocalized(invite, Lang.PLAYER_TARGET, "player", player.getName());
				plugin.getInviteManager().invitePlayer(village, invite);
			}
			return;
		}
		sendLocalized(player, Lang.COMMAND_USAGE_INVITE);
    }
}
