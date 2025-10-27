package pl.kiosel.common.command;

import org.bukkit.entity.Player;
import pl.kiosel.common.utils.ColorUtils;
import pl.kiosel.villages.Wioski;

public abstract class SubCommand {

    public abstract String getName();
    public abstract String getDescription();
	public abstract String getUsage();
	public abstract String getPermission();
    public abstract void run(Player player, Wioski plugin, String[] args);

    public String tl(String s) {
        return ColorUtils.color(s);
    }
}