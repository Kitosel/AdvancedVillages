package pl.kiosel.villages.commands.subcommands;

import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.config.CommandLang;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.VillagePermission;
import pl.kiosel.villages.manager.teleport.TeleportManager;

public class TpCommand extends AVSubCommand {

    @Override
    public String getDescription() { return "Teleport to village"; }

    @Override
    public String getUsage() { return "/village teleport"; }

	@Override
	public String getPermission() { return "villages.command.tp"; }

	@Override
	public boolean requireVillage() { return true; }

	@Override
	public VillagePermission getVillagePermission() { return VillagePermission.UNSET; }

	private final AdvancedVillages plugin;

	public TpCommand(AdvancedVillages plugin) {
		super(plugin, CommandLang.TELEPORT);
		this.plugin = plugin;
	}

	@Override
	public void run(Player player, User user, String[] args) {
		if (plugin.getTeleportManager().teleportPlayer(player, TeleportManager.TeleportType.VILLAGE))
			sendLocalized(player, Lang.TELEPORT);
    }
}
