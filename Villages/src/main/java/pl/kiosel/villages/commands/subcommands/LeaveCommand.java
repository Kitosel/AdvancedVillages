package pl.kiosel.villages.commands.subcommands;

import org.bukkit.entity.Player;
import pl.kiosel.villages.Wioski;
import pl.kiosel.common.command.SubCommand;
import pl.kiosel.villages.config.Language;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.village.Village;
import pl.kiosel.villages.village.VillageManager;

public class LeaveCommand extends SubCommand {

    @Override
    public String getName() { return "leave"; }

    @Override
    public String getDescription() { return "Leave village"; }

    @Override
    public String getUsage() { return "/village leave"; }

	@Override
	public String getPermission() { return "villages.command.leave"; }

	@Override
    public void run(Player player, Wioski plugin, String[] args) {
        Language lang = plugin.getLang();
        Village village = VillageManager.getVillageByOfflineOwner(player.getName());
        if(village != null) {
            if(village.getOwner().equalsIgnoreCase(player.getName())) {
                player.sendMessage(lang.getMessage(Lang.LEAVE_OWNER));
                return;
            }
            VillageManager villageManager = plugin.getVillageManager();
			plugin.getUserManager().removeUserFromVillage(village, player.getUniqueId());
			villageManager.removeMember(plugin.getPlayerDataManager().getVillages().get(village.getVillageName()), player);

			player.sendMessage(lang.getMessage(Lang.LEAVE_VILLAGE));
        } else {
            player.sendMessage(lang.getMessage(Lang.VILLAGE_NO));
        }
    }
}
