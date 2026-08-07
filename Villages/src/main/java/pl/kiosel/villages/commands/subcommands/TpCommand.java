package pl.kiosel.villages.commands.subcommands;

import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Permission;

public class TpCommand extends AVSubCommand {

    @Override
    public String getName() { return "tp"; }

    @Override
    public String getDescription() { return "Teleport to village"; }

    @Override
    public String getUsage() { return "/village teleport"; }

	@Override
	public String getPermission() { return "villages.command.tp"; }

	@Override
	public Permission getVillagePermission() { return Permission.UNSET; }

	private final AdvancedVillages plugin;

	public TpCommand(AdvancedVillages plugin) {
		super(plugin);
		this.plugin = plugin;
	}

	@Override
	public void run(Player player, User user, String[] args) {
        Village village = user.getPresentVillage();
        if (village == null) return;

		if (plugin.getTeleportManager().teleportPlayerToVillage(player))
			sendLocalized(player, Lang.TELEPORT);
    }
}
