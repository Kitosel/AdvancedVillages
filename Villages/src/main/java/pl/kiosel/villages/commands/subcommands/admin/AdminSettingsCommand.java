package pl.kiosel.villages.commands.subcommands.admin;

import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.firststeps.TutorialGUI;
import pl.kiosel.villages.config.CommandLang;
import pl.kiosel.villages.data.user.User;

public class AdminSettingsCommand extends AdminSubCommand {

	private final AdvancedVillages plugin;

	public AdminSettingsCommand(AdvancedVillages plugin) {
		super(plugin, CommandLang.ADMIN_SETTINGS);
		this.plugin = plugin;
	}

	@Override
	public String getDescription() {
		return "Manage basic plugin settings";
	}

	@Override
	public String getUsage() {
		return "/village admin settings";
	}

	@Override
	public boolean showInHelp() {
		return false;
	}

	@Override
	protected boolean isHidden() {
		return true;
	}

	@Override
	public void run(Player player, User user, String[] args) {
		plugin.getGuiManager().showGUI(player, new TutorialGUI(plugin, false));
	}

}
