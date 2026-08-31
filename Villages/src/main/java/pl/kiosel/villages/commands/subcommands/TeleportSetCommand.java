package pl.kiosel.villages.commands.subcommands;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.compatibility.ZSound;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.logs.VillageLogType;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Permission;
import pl.kiosel.villages.data.village.Village;

public class TeleportSetCommand extends AVSubCommand {

	@Override
	public String getDescription() { return "Set teleport to village"; }

	@Override
	public String getUsage() { return ""; }

	@Override
	public String getPermission() { return ""; }

	@Override
	public boolean requireVillage() { return true; }

	@Override
	public Permission getVillagePermission() { return Permission.SETTINGS; }

	private final AdvancedVillages plugin;

	public TeleportSetCommand(AdvancedVillages plugin) {
		super(plugin, "teleporting6");
		this.plugin = plugin;
	}

	@Override
	public boolean showInHelp() {
		return false;
	}

	@Override
	protected boolean isHidden() {
		return true;
	}

	@Override
	public void run(Player player, User user, String[] args) {
		if (args.length != 3
				|| !args[0].equalsIgnoreCase("teleporting6")
				|| !args[1].equalsIgnoreCase("village7")
				|| !args[2].equalsIgnoreCase("set9")
				|| !plugin.getTeleportManager().isTeleportTask(player)) {
			return;
		}
		Village village = user.getPresentVillage();

		if (village.getRegion().get().isIn(player.getLocation())) {
			Location loc = player.getLocation();
			plugin.getTeleportManager().setTeleportToVillage(loc, village);
			plugin.getLogManager().record(village, VillageLogType.SETTING_CHANGED, player,
					"setting", "teleport", "value", "updated");
			plugin.getTeleportManager().removeTeleportTask(player);
			getMessage(Lang.TELEPORT_SET_TITLE.getPath()).sendTitle(player);
			getMessage(Lang.TELEPORT_SET_SUBTITLE.getPath()).sendActionBar(player);
			ZSound.ENTITY_VILLAGER_YES.play(player, 1f,1f);
		} else {
			sendLocalized(player, Lang.TELEPORT_SET_OUT_OF_VILLAGE);
		}
	}
}
