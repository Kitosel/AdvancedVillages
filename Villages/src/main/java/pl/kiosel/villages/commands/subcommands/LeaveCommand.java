package pl.kiosel.villages.commands.subcommands;

import org.bukkit.entity.Player;
import pl.kiosel.core.locale.Locale;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.data.village.Village;

public class LeaveCommand extends AVSubCommand {

    @Override
    public String getName() { return "leave"; }

    @Override
    public String getDescription() { return "Leave village"; }

    @Override
    public String getUsage() { return "/village leave"; }

	@Override
	public String getPermission() { return "villages.command.leave"; }

	private final AdvancedVillages plugin;

	public LeaveCommand(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run(Player player, User user, String[] args) {
		Locale locale = plugin.getLocale();
		Village village = user.getPresentVillage();
		if(village != null) {
            if(village.isOwner(user)) {
				locale.getMessage(Lang.LEAVE_OWNER.getPath()).sendPrefixedMessage(player);
                return;
            }
			user.setVillage(null);
			locale.getMessage(Lang.LEAVE_VILLAGE.getPath()).sendPrefixedMessage(player);
        } else {
			locale.getMessage(Lang.VILLAGE_NO.getPath()).sendPrefixedMessage(player);
        }
    }
}
