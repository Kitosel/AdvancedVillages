package pl.kiosel.villages.commands.subcommands;

import org.bukkit.entity.Player;
import pl.kiosel.villages.Wioski;
import pl.kiosel.common.command.SubCommand;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.village.Village;
import pl.kiosel.villages.village.VillageManager;

public class TpCommand extends SubCommand {

    @Override
    public String getName() { return "tp"; }

    @Override
    public String getDescription() { return "Teleport to village"; }

    @Override
    public String getUsage() { return "/village teleport"; }

	@Override
	public String getPermission() { return "villages.command.tp"; }

	@Override
    public void run(Player player, Wioski plugin, String[] args) {
        Village village = VillageManager.getVillageByOfflineOwner(player.getName());
        if (village == null) return;

		if (plugin.getTeleportManager().teleportPlayerToVillage(player))
			player.sendMessage(plugin.getLang().getMessage(Lang.TELEPORT));
    }
}
