package pl.kiosel.villages.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.utils.TabUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.config.CommandLang;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Permission;
import pl.kiosel.villages.data.village.Village;

import java.util.List;

public class InviteCommand extends AVSubCommand {

    @Override
    public String getDescription() { return "Invite a player to your village"; }

    @Override
    public String getUsage() { return "/village invite <player>"; }

	@Override
	public String getPermission() { return "villages.command.invite"; }

	@Override
	public boolean requireVillage() { return true; }

	@Override
	public Permission getVillagePermission() { return Permission.INVITE; }

	private final AdvancedVillages plugin;

	public InviteCommand(AdvancedVillages plugin) {
		super(plugin, CommandLang.INVITE);
		this.plugin = plugin;
	}

	@Override
	public void run(Player player, User user, String[] args) {
		Village village = user.getPresentVillage();
		if (village.getMembers().size() >= plugin.getDevelopmentManager().getMaxMembers(village)) {
			sendLocalized(player, Lang.MAX_MEMBERS);
			return;
		}

		if (args.length == 2) {
			Player invite = Bukkit.getPlayer(args[1]);
			if (invite == null || !invite.isOnline()) {
				sendLocalized(player, Lang.PLAYER_OFFLINE);
				return;
			}

			User inviteUser = plugin.getUserManager().findByPlayer(invite).orElse(null);
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

	@Override
	public List<String> tabComplete(Player player, User user, String[] args) {
		return args.length == 2 ? TabUtils.onlinePlayers() : List.of();
	}
}
