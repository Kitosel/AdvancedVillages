package pl.kiosel.villages.commands.subcommands;

import org.bukkit.entity.Player;
import pl.kiosel.core.commands.SubCommand;
import pl.kiosel.core.locale.Locale;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageManager;

public class LeaveCommand extends SubCommand {

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
    public void run(Player player, String[] args) {
        Village village = VillageManager.getVillageByOfflineOwner(player.getName());
		Locale locale = plugin.getLocale();
        if(village != null) {
            if(village.getOwner().equalsIgnoreCase(player.getName())) {
				locale.getMessage(Lang.LEAVE_OWNER.getPath()).sendPrefixedMessage(player);
                return;
            }
            VillageManager villageManager = plugin.getVillageManager();
			plugin.getDatabaseUserManager().removeUserFromVillage(village, player.getUniqueId());
			villageManager.removeMember(plugin.getVillageDataManager().getVillages().get(village.getVillageName()), player);

			locale.getMessage(Lang.LEAVE_VILLAGE.getPath()).sendPrefixedMessage(player);
        } else {
			locale.getMessage(Lang.VILLAGE_NO.getPath()).sendPrefixedMessage(player);
        }
    }
}
