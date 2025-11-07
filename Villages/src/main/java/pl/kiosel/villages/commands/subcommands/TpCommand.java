package pl.kiosel.villages.commands.subcommands;

import org.bukkit.entity.Player;
import pl.kiosel.core.commands.SubCommand;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageManager;

public class TpCommand extends SubCommand {

    @Override
    public String getName() { return "tp"; }

    @Override
    public String getDescription() { return "Teleport to village"; }

    @Override
    public String getUsage() { return "/village teleport"; }

	@Override
	public String getPermission() { return "villages.command.tp"; }

	private final AdvancedVillages plugin;

	public TpCommand(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	@Override
    public void run(Player player, String[] args) {
        Village village = VillageManager.getVillageByOfflineOwner(player.getName());
        if (village == null) return;

		if (plugin.getTeleportManager().teleportPlayerToVillage(player))
			plugin.getLocale().getMessage(Lang.TELEPORT.getPath()).sendPrefixedMessage(player);
    }
}
