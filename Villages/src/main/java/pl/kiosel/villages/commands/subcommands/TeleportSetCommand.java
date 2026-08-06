package pl.kiosel.villages.commands.subcommands;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import pl.kiosel.core.math.MathUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.data.village.Village;

public class TeleportSetCommand extends AVSubCommand {

	@Override
	public String getName() { return "teleporting"; }

	@Override
	public String getDescription() { return "Set teleport to village"; }

	@Override
	public String getUsage() { return ""; }

	@Override
	public String getPermission() { return ""; }

	private final AdvancedVillages plugin;

	public TeleportSetCommand(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run(Player player, User user, String[] args) {
		Village village = user.getPresentVillage();
		if (village == null || args.length != 3) {
			return;
		}
		if (!args[0].equalsIgnoreCase("teleporting6")
				|| !args[1].equalsIgnoreCase("village7")
				|| !args[2].equalsIgnoreCase("set9")
				|| !plugin.getTeleportManager().isTeleportTask(player)) {
			return;
		}

		Location loc = player.getLocation();
		loc.setYaw(MathUtils.roundFloat(loc.getYaw(), 100));
		loc.setPitch(MathUtils.roundFloat(loc.getPitch(), 100));
		plugin.getTeleportManager().setTeleportToVillage(loc, village);
		plugin.getTeleportManager().removeTeleportTask(player);
		plugin.getLocale().getMessage(Lang.TELEPORT_SET_TITLE.getPath()).sendTitle(player);
		plugin.getLocale().getMessage(Lang.TELEPORT_SET_SUBTITLE.getPath()).sendActionBar(player);
		player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 10, 1);
	}
}
