package pl.kiosel.villages.commands.subcommands;

import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.buildeditor.VillageBuildEditorManager;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.config.CommandLang;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Permission;

import java.util.List;

public final class BuildEditCommand extends AVSubCommand {

    private final AdvancedVillages plugin;
	private final VillageBuildEditorManager editorManager;

    public BuildEditCommand(AdvancedVillages plugin) {
	    super(plugin, CommandLang.EDIT);
		this.plugin = plugin;
		this.editorManager = plugin.getVillageBuildEditorManager();
    }

    @Override
    public String getDescription() { return "Schematic edit"; }

    @Override
    public String
    getUsage() { return "/village edit [save|cancel]"; }

    @Override
    public String getPermission() { return "villages.command.edit"; }

	@Override
	public boolean requireVillage() { return false; }

    @Override
    public Permission getVillagePermission() { return Permission.UNSET; }

    @Override
    public void run(Player player, User user, String[] args) {
        if (this.editorManager == null) {
            sendLocalized(player, Lang.BUILD_EDITOR_WORLD_EDIT_REQUIRED);
            return;
        }
        if (args.length == 1) {
            this.editorManager.openLevelMenu(player);
            return;
        }

		String action = args[1].toLowerCase();
		if (action.equals(getCommand(CommandLang.EDIT_SAVE))) {
			this.editorManager.saveSession(player);
			return;
		}
		if (action.equals(getCommand(CommandLang.EDIT_CANCEL))) {
            this.editorManager.cancelSession(player);
            return;
        }
        getMessage(Lang.COMMAND_USAGE_BUILD_EDITOR.getPath())
                .with("save", getCommand(CommandLang.EDIT_SAVE))
                .with("cancel", getCommand(CommandLang.EDIT_CANCEL))
                .sendPrefixed(player);
    }

	@Override
	public List<String> tabComplete(Player player, User user, String[] args) {
		if (args.length != 2 || this.editorManager == null
				|| !this.editorManager.hasSession(player)) {
			return List.of();
		}
		return List.of(
				getCommand(CommandLang.EDIT_SAVE),
				getCommand(CommandLang.EDIT_CANCEL)
		);
	}
}
