package pl.kiosel.villages.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kiosel.core.commands.SimpleCommand;
import pl.kiosel.core.commands.SubCommand;
import pl.kiosel.core.utils.TabUtils;
import pl.kiosel.core.utils.TextUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.commands.subcommands.*;
import pl.kiosel.villages.config.CommandConfig;
import pl.kiosel.villages.enums.CommandLang;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Permission;
import pl.kiosel.villages.manager.PermissionManager;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageManager;

import java.util.*;

public class CommandVillage extends SimpleCommand {

	private final AdvancedVillages plugin;
	private final Map<String, SubCommand> subCommandMap = new HashMap<>();
	private final CommandConfig commandConfig;
	private final PermissionManager permissionManager;

	public CommandVillage(AdvancedVillages plugin) {
		super(plugin.getCommandLang().getCommandName(), plugin.getCommandLang().getCommandAliases(), plugin.getCommandLang().getCommandPermission());
		this.plugin = plugin;
		this.commandConfig = plugin.getCommandLang();
		this.permissionManager = plugin.getPermissionManager();
		registerSubCommands(plugin);
	}

	private void registerSubCommands(AdvancedVillages plugin) {
		subCommandMap.put(commandConfig.getCommand(CommandLang.TELEPORT).toLowerCase(), new TpCommand(plugin));
		subCommandMap.put(commandConfig.getCommand(CommandLang.ADMIN).toLowerCase(), new AdminCommand(plugin));
		subCommandMap.put(commandConfig.getCommand(CommandLang.LEAVE).toLowerCase(), new LeaveCommand(plugin));
		subCommandMap.put(commandConfig.getCommand(CommandLang.INVITE).toLowerCase(), new InviteCommand(plugin));
		subCommandMap.put(commandConfig.getCommand(CommandLang.REQUEST).toLowerCase(), new RequestCommand(plugin));
		subCommandMap.put(commandConfig.getCommand(CommandLang.CHAT).toLowerCase(), new ChatCommand(plugin));
	}

	@Override
	public boolean onExecute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			plugin.reloadConfig();
			plugin.getLocale().getMessage(Lang.COMMAND_RELOAD.getPath()).sendPrefixedMessage(sender);
			return false;
		}

		Player player = (Player) sender;
		if (args.length == 0) {
			help(sender);
			return false;
		}

		String input = args[0].toLowerCase();
		SubCommand sub = subCommandMap.get(input);

		if (sub != null) {
			if (player.hasPermission(sub.getPermission())) {
				sub.run(player, args);
			} else {
				plugin.getLocale().getMessage(Lang.COMMAND_NO_PERMISSION.getPath()).sendPrefixedMessage(sender);
			}
			return true;
		}

		if (input.equalsIgnoreCase("teleporting6")) {
			new TeleportSetCommand(plugin).run(player, args);
			return true;
		}

		help(sender);
		return false;
	}

	public void help(CommandSender sender) {
		sender.sendMessage(tl("&8--------------------------------"));
		for (SubCommand sub : subCommandMap.values()) {
			if (sender.hasPermission(sub.getPermission())) {
				sender.sendMessage(sub.getUsage() + " - " + sub.getDescription());
			}
		}
		sender.sendMessage(tl("&8--------------------------------"));
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) return TabUtils.returnEmpty();

		Player player = (Player) sender;
		Village village = VillageManager.getVillageByOfflineOwner(player.getName());
		List<String> arg1 = new ArrayList<>();
		arg1.add(commandConfig.getCommand(CommandLang.HELP));

		for (Map.Entry<String, SubCommand> entry : subCommandMap.entrySet()) {
			String name = entry.getKey();
			SubCommand sub = entry.getValue();

			if (!player.hasPermission(sub.getPermission()) && !player.isOp()) continue;

			switch (sub.getName().toLowerCase()) {
				case "admin":
					if (player.isOp()) arg1.add(name);
					break;
				case "tp":
				case "leave":
				case "chat":
					if (village != null) arg1.add(name);
					break;
				case "invite":
					if (village != null && permissionManager.hasPermission(player, Permission.INVITE))
						arg1.add(name);
					break;
				case "request":
					if (village != null)
						break;
					if (plugin.getInviteManager().isPlayerInvited(player))
						arg1.add(name);
					break;
				default:
					break;
			}
		}

		if (args.length == 1) {
			return TabUtils.returnWith(args[0], arg1);
		}

		if (args.length == 2) {
			String subCmd = args[0].toLowerCase();

			if (subCmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN))) {
				if (player.isOp()) {
					return TabUtils.returnWith(args[1], TextUtils.of(
							commandConfig.getCommand(CommandLang.ADMIN_RELOAD),
							commandConfig.getCommand(CommandLang.ADMIN_GIVE),
							commandConfig.getCommand(CommandLang.ADMIN_UPGRADE),
							commandConfig.getCommand(CommandLang.ADMIN_DELETE)
					));
				}
			}

			if (subCmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.REQUEST))) {
				if (village == null && plugin.getInviteManager().isPlayerInvited(player)) {
					return TabUtils.returnWith(args[1], TextUtils.of(
							commandConfig.getCommand(CommandLang.REQUEST_ACCEPT),
							commandConfig.getCommand(CommandLang.REQUEST_DENY)
					));
				}
			}

			if (subCmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.INVITE))) {
				if (village != null && permissionManager.hasPermission(player, Permission.INVITE))
					return TabUtils.onlinePlayers();
			}
		}

		if (args.length == 3) {
			String subCmd = args[0].toLowerCase();
			String sub2Cmd = args[1].toLowerCase();

			if (subCmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN))) {
				if (player.isOp()) {
					if (sub2Cmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN_GIVE)))
						return TabUtils.returnWith(args[2], TextUtils.of(
								commandConfig.getCommand(CommandLang.ADMIN_GIVE_DESTROYER),
								commandConfig.getCommand(CommandLang.ADMIN_GIVE_VILLAGE)
						));
					if (sub2Cmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN_UPGRADE))
						|| sub2Cmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN_DELETE)))
						return TabUtils.returnWith(args[2], plugin.getVillageDataManager().getVillageOwners());
				}
			}
		}
		return TabUtils.returnEmpty();
	}
}
