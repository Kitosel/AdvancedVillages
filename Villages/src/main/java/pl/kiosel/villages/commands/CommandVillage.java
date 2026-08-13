package pl.kiosel.villages.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import panda.std.Option;
import pl.kiosel.core.commands.SimpleCommand;
import pl.kiosel.core.utils.TabUtils;
import pl.kiosel.core.utils.TextUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.commands.subcommands.*;
import pl.kiosel.villages.config.CommandConfig;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.CommandLang;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Permission;
import pl.kiosel.villages.manager.PermissionManager;

import java.util.*;

public class CommandVillage extends SimpleCommand {

	private final AdvancedVillages plugin;
	private volatile Map<String, AVSubCommand> subCommandMap = Collections.emptyMap();
	private final CommandConfig commandConfig;
	private final PermissionManager permissionManager;

	public CommandVillage(AdvancedVillages plugin) {
		super(plugin, plugin.getCommandLang().getCommandName(), plugin.getCommandLang().getCommandAliases(), plugin.getCommandLang().getCommandPermission());
		this.plugin = plugin;
		this.commandConfig = plugin.getCommandLang();
		this.permissionManager = plugin.getPermissionManager();
		this.reloadArguments();
	}

	public synchronized void reloadArguments() {
		Map<String, AVSubCommand> refreshed = new LinkedHashMap<>();
		refreshed.put(commandConfig.getCommand(CommandLang.ADMIN), new AdminCommand(plugin));
		refreshed.put(commandConfig.getCommand(CommandLang.CHAT), new ChatCommand(plugin));
		refreshed.put(commandConfig.getCommand(CommandLang.INVITE), new InviteCommand(plugin));
		refreshed.put(commandConfig.getCommand(CommandLang.LEAVE), new LeaveCommand(plugin));
		refreshed.put(commandConfig.getCommand(CommandLang.REQUEST), new RequestCommand(plugin));
		refreshed.put(commandConfig.getCommand(CommandLang.TELEPORT), new TpCommand(plugin));
		refreshed.put(commandConfig.getCommand(CommandLang.EDIT), new BuildEditCommand(plugin));
		this.subCommandMap = Collections.unmodifiableMap(refreshed);
	}

	@Override
	public boolean onExecute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			plugin.reloadConfig();
			plugin.getMessages().get(Lang.COMMAND_RELOAD).sendPrefixedMessage(sender);
			return false;
		}
		Player player = (Player) sender;
		if (args.length == 0) {
			help(player);
			return false;
		}

		Option<User> userOption = plugin.getUserManager().findByUuid(player.getUniqueId());
		if (userOption.isEmpty()) {
			return false;
		}
		User user = userOption.get();
		String input = args[0].toLowerCase();
		AVSubCommand sub = subCommandMap.get(input);

		if (sub != null) {
			if (sub.requireVillage()) {
				Village village = user.getPresentVillage();
				if (village == null) {
					sendLocalized(player, Lang.VILLAGE_NO.getPath());
					return true;
				}
			}
			if (player.hasPermission(sub.getPermission()) && permissionManager.hasCommandPermission(player, sub.getVillagePermission())) {
				sub.run(player, user, args);
			} else {
				sendLocalized(player, Lang.COMMAND_NO_PERMISSION.getPath());
			}
			return true;
		}

		if (input.equalsIgnoreCase("teleporting6")) {
			new TeleportSetCommand(plugin).run(player, user, args);
			return true;
		}

		help(player);
		return false;
	}

	public void help(Player sender) {
		plugin.getMessages().send(sender, Lang.SEPARATOR);
		for (AVSubCommand sub : subCommandMap.values()) {
			if (sender.hasPermission(sub.getPermission()) && permissionManager.hasCommandPermission(sender, sub.getVillagePermission())) {
				sender.sendMessage(sub.getUsage() + " - " + sub.getDescription());
			}
		}
		plugin.getMessages().send(sender, Lang.SEPARATOR);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) return TabUtils.returnEmpty();

		Player player = (Player) sender;
		Option<User> userOption = plugin.getUserManager().findByUuid(player.getUniqueId());
		if (userOption.isEmpty()) {
			return TabUtils.returnEmpty();
		}
		User user = userOption.get();
		Village village = user.getPresentVillage();
		List<String> arg1 = new ArrayList<>();
		arg1.add(commandConfig.getCommand(CommandLang.HELP));

		for (Map.Entry<String, AVSubCommand> entry : subCommandMap.entrySet()) {
			String name = entry.getKey();
			AVSubCommand sub = entry.getValue();

			if (sub.getName().equalsIgnoreCase("edit")) {
				if (player.hasPermission(sub.getPermission())) arg1.add(name);
				continue;
			}
			if (!user.hasPermission(sub.getPermission())) continue;

			switch (sub.getName().toLowerCase()) {
				case "admin":
					if (player.isOp()) arg1.add(name);
					break;
				case "tp":
				case "leave":
				case "chat":
				case "quests":
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

			if (subCmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.EDIT))
					&& player.hasPermission("villages.command.edit")
					&& plugin.getVillageBuildEditorManager().hasSession(player)) {
				return TabUtils.returnWith(args[1], TextUtils.of(
						commandConfig.getCommand(CommandLang.EDIT_SAVE),
						commandConfig.getCommand(CommandLang.EDIT_CANCEL)
				));
			}

			if (subCmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN))) {
				if (user.hasPermission(getPermission()+".admin")) {
					return TabUtils.returnWith(args[1], TextUtils.of(
							commandConfig.getCommand(CommandLang.ADMIN_RELOAD),
							commandConfig.getCommand(CommandLang.ADMIN_GIVE),
							commandConfig.getCommand(CommandLang.ADMIN_MANAGE),
							commandConfig.getCommand(CommandLang.ADMIN_SETTINGS)
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
				if (user.hasPermission(getPermission()+".admin")) {
					if (sub2Cmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN_GIVE)))
						return TabUtils.returnWith(args[2], TextUtils.of(
								commandConfig.getCommand(CommandLang.ADMIN_GIVE_DESTROYER),
								commandConfig.getCommand(CommandLang.ADMIN_GIVE_VILLAGE),
								commandConfig.getCommand(CommandLang.ADMIN_GIVE_DESTROYER_HEARTH),
								commandConfig.getCommand(CommandLang.ADMIN_GIVE_VILLAGE_HEARTH),
								commandConfig.getCommand(CommandLang.ADMIN_GIVE_VILLAGE_HEARTH_PART)
						));
					if (sub2Cmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN_MANAGE)))
						return TabUtils.returnWith(args[2], plugin.getVillageManager().getVillageOwners());
				}
			}
		}

		if (args.length == 4) {
			String subCmd = args[0].toLowerCase();
			String sub2Cmd = args[1].toLowerCase();

			if (subCmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN))) {
				if (user.hasPermission(getPermission()+".admin")) {
					if (sub2Cmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN_MANAGE)))
						return TabUtils.returnWith(args[3], TextUtils.of(
								commandConfig.getCommand(CommandLang.ADMIN_UPGRADE),
								commandConfig.getCommand(CommandLang.ADMIN_PROTECTION),
								commandConfig.getCommand(CommandLang.ADMIN_LIVES),
								commandConfig.getCommand(CommandLang.ADMIN_BANK),
								commandConfig.getCommand(CommandLang.ADMIN_DELETE)
						));
				}
			}
		}

		if (args.length == 5) {
			String subCmd = args[0].toLowerCase(); //admin
			String sub2Cmd = args[1].toLowerCase(); //manage
			String sub3Cmd = args[3].toLowerCase(); //tryb

			if (!subCmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN))
					|| !user.hasPermission(getPermission() + ".admin")) return TabUtils.returnEmpty();

			if (sub2Cmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN_MANAGE))) {
				if (sub3Cmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN_PROTECTION))
						|| sub3Cmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN_LIVES))
						|| sub3Cmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN_BANK)))
					return TabUtils.returnWith(args[4], TextUtils.of(
							commandConfig.getCommand(CommandLang.ADMIN_ADD),
							commandConfig.getCommand(CommandLang.ADMIN_REMOVE)
					));
			}
		}

		if (args.length == 6) {
			String subCmd = args[0].toLowerCase(); //admin
			String sub2Cmd = args[1].toLowerCase(); //manage
			String sub3Cmd = args[3].toLowerCase(); //tryb
			String sub4Cmd = args[4].toLowerCase(); //add/remove

			if (!subCmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN))
					|| !user.hasPermission(getPermission() + ".admin")) return TabUtils.returnEmpty();

			if (sub2Cmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN_MANAGE))) {
				if (sub3Cmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN_PROTECTION)))
					if (sub4Cmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN_ADD)))
						return TabUtils.returnWith(args[5], TextUtils.of(
								"1", "5", "10", "24"
						));
				if (sub3Cmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN_LIVES)))
					if (sub4Cmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN_ADD))
							|| sub4Cmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN_REMOVE)))
						return TabUtils.returnWith(args[5], TextUtils.of(
								"1", "2", "3"
						));
				if (sub3Cmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN_BANK)))
					if (sub4Cmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN_ADD))
							|| sub4Cmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN_REMOVE)))
						return TabUtils.returnWith(args[5], TextUtils.of(
								"1", "5", "10", "50", "100", "1000"
						));
			}
		}

		if (args.length == 7) {
			String subCmd = args[0].toLowerCase();
			String sub2Cmd = args[1].toLowerCase();
			String sub3Cmd = args[3].toLowerCase();
			String sub4Cmd = args[4].toLowerCase();

			if (subCmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN))) {
				if (user.hasPermission(getPermission()+".admin")) {
					if (sub2Cmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN_MANAGE)))
						if (sub3Cmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN_PROTECTION)))
							if (sub4Cmd.equalsIgnoreCase(commandConfig.getCommand(CommandLang.ADMIN_ADD)))
								return TabUtils.returnWith(args[6], TextUtils.of(
										"seconds", "minutes", "hours", "days"
								));
				}
			}
		}
		return TabUtils.returnEmpty();
	}
}
