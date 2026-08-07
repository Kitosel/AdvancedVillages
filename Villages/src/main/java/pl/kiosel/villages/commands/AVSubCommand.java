package pl.kiosel.villages.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kiosel.core.MetaPlugin;
import pl.kiosel.core.locale.Message;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Permission;

public abstract class AVSubCommand {

	protected MetaPlugin plugin;

	public AVSubCommand(MetaPlugin plugin) {
		this.plugin = plugin;
	}

    public abstract String getName();
    public abstract String getDescription();
	public abstract String getUsage();
	public abstract String getPermission();
	public abstract Permission getVillagePermission();
    public abstract void run(Player player, User user, String[] args);

	public Message getMessage(String node) {
		return plugin.getLocale().getMessage(node);
	}

	public void sendLocalized(CommandSender sender, Lang node) {
		plugin.getLocale().getMessage(node.getPath()).sendPrefixedMessage(sender);
	}

	public void sendLocalized(CommandSender sender, Lang node, String placeholder, Object value) {
		plugin.getLocale().getMessage(node.getPath())
				.processPlaceholder(placeholder, String.valueOf(value))
				.sendPrefixedMessage(sender);
	}
}
