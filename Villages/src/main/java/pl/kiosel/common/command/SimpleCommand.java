package pl.kiosel.common.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import pl.kiosel.common.utils.ColorUtils;

import java.util.List;

public abstract class SimpleCommand extends Command {

    protected String permission;
    protected final String command;

    public SimpleCommand(String command, List<String> aliases, String permission) {
        super(command);
        this.permission = permission;
        this.command = command;

        this.setPermission(permission);
        this.setDescription("Command " + command);
        this.setAliases(aliases);
        this.setUsage("/" + command);
    }

    public SimpleCommand(String command, List<String> aliases) {
        super(command);
        this.command = command;

        this.setDescription("Command " + command);
        this.setAliases(aliases);
        this.setUsage("/" + command);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
        if(permission != null) {
            if (!sender.hasPermission(permission)) {
                sender.sendMessage(tl("&cYou don't have permission &7(" + permission.toLowerCase() + "&7)"));
                return true;
            }
        }

        try {
            return onExecute(sender, label, args);
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(tl("&cError while executing command &7'&e" + command.toLowerCase() + "&7'"));
            sender.sendMessage(tl("&cError while executing command &7'&e" + command.toLowerCase() + "&7'"));
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) throws IllegalArgumentException {
        return tabComplete(sender, args);
    }

    public String tl(String s) {
        return ColorUtils.color(s);
    }

    public abstract boolean onExecute(CommandSender sender, String label, String[] args);
    public abstract List<String> tabComplete(CommandSender sender, String[] args);
}
