package pl.kiosel.villages.commands.subcommands;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Permission;

public class LeaveCommand extends AVSubCommand {

    @Override
    public String getName() { return "leave"; }

    @Override
    public String getDescription() { return "Leave village"; }

    @Override
    public String getUsage() { return "/village leave"; }

	@Override
	public String getPermission() { return "villages.command.leave"; }

	@Override
	public Permission getVillagePermission() { return Permission.UNSET; }

	private final AdvancedVillages plugin;

	public LeaveCommand(AdvancedVillages plugin) {
		super(plugin);
		this.plugin = plugin;
	}

	@Override
	public void run(Player player, User user, String[] args) {
		Village village = user.getPresentVillage();
		if(village != null) {
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
			user.removeVillage();
			player.playSound(player, Sound.ENTITY_GHAST_HURT, 1, 2);
			sendLocalized(player, Lang.LEAVE_VILLAGE);
        } else {
			sendLocalized(player, Lang.VILLAGE_NO);
        }
    }
}
