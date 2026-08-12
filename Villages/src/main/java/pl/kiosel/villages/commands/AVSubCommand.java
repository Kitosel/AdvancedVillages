package pl.kiosel.villages.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kiosel.core.locale.Message;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Permission;

public abstract class AVSubCommand {

	protected AdvancedVillages plugin;

	public AVSubCommand(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

    public abstract String getName();
    public abstract String getDescription();
	public abstract String getUsage();
	public abstract String getPermission();
	public abstract boolean requireVillage();
	public abstract Permission getVillagePermission();
    public abstract void run(Player player, User user, String[] args);

	public Message getMessage(String node) {
		return plugin.getMessages().get(node);
	}

	public void sendLocalized(CommandSender sender, Lang node) {
		plugin.getMessages().sendPrefixed(sender, node);
	}

	public void sendLocalized(CommandSender sender, Lang node, String placeholder, Object value) {
		plugin.getMessages().sendPrefixed(sender, node, placeholder, value);
	}
}
