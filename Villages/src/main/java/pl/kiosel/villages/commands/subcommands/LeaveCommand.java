package pl.kiosel.villages.commands.subcommands;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.logs.VillageLogType;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.config.CommandLang;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Permission;
import pl.kiosel.villages.data.village.Village;

public class LeaveCommand extends AVSubCommand {

    @Override
    public String getDescription() { return "Leave village"; }

    @Override
    public String getUsage() { return "/village leave"; }

	@Override
	public String getPermission() { return "villages.command.leave"; }

	@Override
	public boolean requireVillage() { return true; }

	@Override
	public Permission getVillagePermission() { return Permission.UNSET; }

	private final AdvancedVillages plugin;

	public LeaveCommand(AdvancedVillages plugin) {
		super(plugin, CommandLang.LEAVE);
		this.plugin = plugin;
	}

	@Override
	public void run(Player player, User user, String[] args) {
		Village village = user.getPresentVillage();
        if(village.isOwner(user)) {
			sendLocalized(player, Lang.LEAVE_OWNER);
            return;
        }

		if (!plugin.getInviteManager().isConfirm(player.getUniqueId())) {
			sendLocalized(player, Lang.LEAVE_CONFIRM_VILLAGE);
			player.playSound(player, Sound.ENTITY_VILLAGER_HURT, 1, 2);
			plugin.getInviteManager().addConfirm(player.getUniqueId());
			return;
		}
		plugin.getInviteManager().removeConfirm(player.getUniqueId());

		village.removeMember(user);
		plugin.getLogManager().record(village, VillageLogType.MEMBER_LEAVE, player,
				"member", player.getName());
		player.playSound(player, Sound.ENTITY_GHAST_HURT, 1, 2);
		village.broadcast(getMessage(Lang.LEAVE_VILLAGE_BROADCAST.getPath()).with("player", player.getName()).toText());
		sendLocalized(player, Lang.LEAVE_VILLAGE);
    }
}
