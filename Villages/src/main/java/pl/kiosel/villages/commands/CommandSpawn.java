package pl.kiosel.villages.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kiosel.core.commands.SimpleCommand;
import pl.kiosel.core.utils.TabUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.CommandLang;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.manager.teleport.TeleportManager;
import pl.kiosel.villages.settings.Settings;

import java.util.Collections;
import java.util.List;

public class CommandSpawn extends SimpleCommand {

	private final AdvancedVillages plugin;
	private final TeleportManager teleportManager;

	public CommandSpawn(AdvancedVillages plugin) {
		super(plugin, plugin.getCommandLang().getSpawnCommandName(), plugin.getCommandLang().getSpawnCommandAliases(),
				plugin.getCommandLang().getSpawnCommandPermission());
		this.plugin = plugin;
		this.teleportManager = plugin.getTeleportManager();
	}

	@Override
	public boolean onExecute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			this.plugin.getMessages().get(Lang.COMMAND_CONSOLE).sendPrefixedMessage(sender);
			return true;
		}
		if (!Settings.ADDONS_SPAWN_ENABLE.getBoolean()) {
			this.plugin.getMessages().get(Lang.COMMAND_ENABLED).sendPrefixedMessage(sender);
			return true;
		}

		Player player = (Player) sender;
		if (args.length == 0) {
			return this.teleportManager.teleportPlayer(player, TeleportManager.TeleportType.SPAWN);
		}

		String setArgument = this.plugin.getCommandLang().getCommand(CommandLang.SET);
		if (args.length == 1 && args[0].equalsIgnoreCase(setArgument)) {
			if (!player.hasPermission(this.plugin.getCommandLang().getSpawnCommandSetPermission())) {
				this.plugin.getMessages().get(Lang.COMMAND_NO_PERMISSION).sendPrefixedMessage(sender);
				return true;
			}
			this.teleportManager.setSpawn(player.getLocation());
			this.plugin.getMessages().get(Lang.SPAWN_SET).sendPrefixedMessage(sender);
			return true;
		}

		return false;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!Settings.ADDONS_SPAWN_ENABLE.getBoolean() || args.length != 1
				|| !sender.hasPermission(this.plugin.getCommandLang().getSpawnCommandSetPermission())) {
			return TabUtils.returnEmpty();
		}
		return TabUtils.returnWith(args[0], Collections.singletonList(this.plugin.getCommandLang().getCommand(CommandLang.SET)));
	}
}
