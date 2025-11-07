package pl.kiosel.villages.commands.subcommands;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import pl.kiosel.core.commands.SubCommand;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageManager;

public class TeleportSetCommand extends SubCommand {

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
	public void run(Player player, String[] args) {
		Village village = VillageManager.getVillageByOfflineOwner(player.getName());
		if (village == null) return;

		if (args[0].equalsIgnoreCase("teleporting6"))
			if (args[1].equalsIgnoreCase("village7"))
				if (args[2].equalsIgnoreCase("set9")) {
					if (plugin.getTeleportManager().isTeleportTask(player)) {
						Location loc = player.getLocation();
						loc.setYaw(Math.round(loc.getYaw() * 100f) / 100f);
						loc.setPitch(Math.round(loc.getPitch() * 100f) / 100f);
						plugin.getTeleportManager().setTeleportToVillage(loc, village);
						plugin.getTeleportManager().removeTeleportTask(player);
						plugin.getLocale().getMessage(Lang.TELEPORT_SET_TITLE.getPath()).sendTitle(player);
						plugin.getLocale().getMessage(Lang.TELEPORT_SET_SUBTITLE.getPath()).sendActionBar(player);
						player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 10, 1);
					} else {
						player.sendMessage("o ty kurewko skad o tym wiesz");
					}
		}
	}
}