package pl.kiosel.villages.commands.subcommands.admin;

import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.CommandLang;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;

public final class AdminReloadCommand extends AdminSubCommand {

	public AdminReloadCommand(AdvancedVillages plugin) {
		super(plugin, CommandLang.ADMIN_RELOAD);
	}

	@Override
	public String getDescription() {
		return "Reloads the plugin configuration";
	}

	@Override
	public String getUsage() {
		return "/village admin reload";
	}

	@Override
	public void run(Player player, User user, String[] args) {
		this.plugin.reloadConfig();
		sendLocalized(player, Lang.COMMAND_RELOAD);
	}
}
