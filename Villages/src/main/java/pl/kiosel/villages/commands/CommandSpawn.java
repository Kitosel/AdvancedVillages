package pl.kiosel.villages.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.command.RosaCommand;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.CommandLang;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.config.Settings;
import pl.kiosel.villages.manager.teleport.TeleportManager;

import java.util.Collections;
import java.util.List;

public class CommandSpawn extends RosaCommand {

	private final AdvancedVillages plugin;
	private final TeleportManager teleportManager;

	public CommandSpawn(AdvancedVillages plugin) {
		super(plugin, plugin.getCommandLang().getSpawnCommandName(), plugin.getCommandLang().getSpawnCommandAliases(),
				"advancedvillages.command.spawn");
		this.plugin = plugin;
		this.teleportManager = plugin.getTeleportManager();
	}

	@Override
	public boolean isPlayerOnly() {
		return true;
	}

	@Override
	public boolean onExecute(CommandSender sender, String label, String[] args) {
		if (!Settings.ADDONS_SPAWN_ENABLE.getBoolean()) {
			this.plugin.getVillageMessages().get(Lang.COMMAND_ENABLED).sendPrefixed(sender);
			return true;
		}

		Player player = (Player) sender;
		if (args.length == 0) {
			return this.teleportManager.teleportPlayer(player, TeleportManager.TeleportType.SPAWN);
		}

		String setArgument = this.plugin.getCommandLang().getCommand(CommandLang.SET);
		if (args.length == 1 && args[0].equalsIgnoreCase(setArgument)) {
			if (!player.hasPermission("advancedvillages.command.spawn.set")) {
				this.plugin.getVillageMessages().get(Lang.COMMAND_NO_PERMISSION).sendPrefixed(sender);
				return true;
			}
			this.teleportManager.setSpawn(player.getLocation());
			this.plugin.getVillageMessages().get(Lang.SPAWN_SET).sendPrefixed(sender);
			return true;
		}

		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		if (!Settings.ADDONS_SPAWN_ENABLE.getBoolean() || args.length != 1
				|| !sender.hasPermission("advancedvillages.command.spawn.set")) {
			return EMPTY;
		}
		return complete(args[0], Collections.singletonList(this.plugin.getCommandLang().getCommand(CommandLang.SET)));
	}
}
