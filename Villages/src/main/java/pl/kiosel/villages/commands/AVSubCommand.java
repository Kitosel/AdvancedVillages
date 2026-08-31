package pl.kiosel.villages.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.command.RosaSubCommand;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.CommandConfig;
import pl.kiosel.villages.config.CommandLang;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.config.VillageMessage;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Permission;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class AVSubCommand extends RosaSubCommand {

	protected final AdvancedVillages plugin;
	private final CommandConfig commandConfig;
	private final CommandLang commandKey;
	private final String fixedName;

	protected AVSubCommand(AdvancedVillages plugin, CommandLang commandKey) {
		super(plugin);
		this.plugin = plugin;
		this.commandConfig = plugin.getCommandLang();
		this.commandKey = Objects.requireNonNull(commandKey, "commandKey");
		this.fixedName = null;
	}

	protected AVSubCommand(AdvancedVillages plugin, String fixedName) {
		super(plugin);
		this.plugin = plugin;
		this.commandConfig = plugin.getCommandLang();
		this.commandKey = null;
		this.fixedName = Objects.requireNonNull(fixedName, "fixedName");
	}

	@Override
	public final String getName() {
		return commandKey == null ? fixedName : getCommand(commandKey);
	}

	public abstract boolean requireVillage();

	public abstract Permission getVillagePermission();

	public abstract void run(Player player, User user, String[] args);

	public List<String> tabComplete(Player player, User user, String[] args) {
		return Collections.emptyList();
	}

	public boolean showInHelp() {
		return true;
	}

	protected boolean isHidden() {
		return false;
	}

	@Override
	public final boolean isPlayerOnly() {
		return true;
	}

	@Override
	public final boolean isVisible(CommandSender sender) {
		if (isHidden() || !(sender instanceof Player)) return false;
		Player player = (Player) sender;
		User user = plugin.getUserManager().findByPlayer(player).orElse(null);
		return user != null && isTabCompleteAvailable(player, user);
	}

	@Override
	public final void run(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		User user = plugin.getUserManager().findByPlayer(player).orElse(null);
		if (user == null) return;

		if (requireVillage() && user.getPresentVillage() == null) {
			sendLocalized(player, Lang.VILLAGE_NO);
			return;
		}
		if (!plugin.getRoleManager().hasCommandPermission(player, getVillagePermission())) {
			sendLocalized(player, Lang.COMMAND_NO_PERMISSION);
			return;
		}

		run(player, user, withCommandName(args));
	}

	@Override
	public final List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) return Collections.emptyList();
		Player player = (Player) sender;
		User user = plugin.getUserManager().findByPlayer(player).orElse(null);
		return user == null ? Collections.emptyList() : tabComplete(player, user, withCommandName(args));
	}

	public boolean isTabCompleteAvailable(Player player, User user) {
		String permission = getPermission();
		if (permission != null && !permission.trim().isEmpty() && !player.hasPermission(permission)) return false;
		if (requireVillage() && user.getPresentVillage() == null) return false;
		return plugin.getRoleManager().hasCommandPermission(player, getVillagePermission());
	}

	@Override
	public String getNoPermissionMessage() {
		return plugin.getVillageMessages().prefixedText(Lang.COMMAND_NO_PERMISSION);
	}

	public VillageMessage getMessage(String node) {
		return plugin.getVillageMessages().get(node);
	}

	public void sendLocalized(CommandSender sender, Lang node) {
		plugin.getVillageMessages().sendPrefixed(sender, node);
	}

	public void sendLocalized(CommandSender sender, Lang node, String placeholder, Object value) {
		plugin.getVillageMessages().sendPrefixed(sender, node, placeholder, value);
	}

	protected String getCommand(CommandLang commandLang) {
		return commandConfig.getCommand(commandLang);
	}

	private String[] withCommandName(String[] args) {
		String[] legacyArgs = new String[args.length + 1];
		legacyArgs[0] = getName();
		System.arraycopy(args, 0, legacyArgs, 1, args.length);
		return legacyArgs;
	}
}
