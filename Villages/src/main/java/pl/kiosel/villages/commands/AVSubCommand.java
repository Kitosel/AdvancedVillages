package pl.kiosel.villages.commands;

import org.bukkit.entity.Player;
import pl.kiosel.villages.data.user.User;

public abstract class AVSubCommand {

    public abstract String getName();
    public abstract String getDescription();
	public abstract String getUsage();
	public abstract String getPermission();
    public abstract void run(Player player, User user, String[] args);

}
