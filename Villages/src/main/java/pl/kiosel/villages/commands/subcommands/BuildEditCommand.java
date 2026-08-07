package pl.kiosel.villages.commands.subcommands;

import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.enums.CommandLang;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Permission;

public final class BuildEditCommand extends AVSubCommand {

    private final AdvancedVillages plugin;

    public BuildEditCommand(AdvancedVillages plugin) {
	    super(plugin);
		this.plugin = plugin;
    }

    @Override
    public String getName() { return "edit"; }

    @Override
    public String getDescription() { return "Schematic edit"; }

    @Override
    public String
    getUsage() { return "/village edit [save|cancel]"; }

    @Override
    public String getPermission() { return "villages.command.edit"; }

    @Override
    public Permission getVillagePermission() { return Permission.UNSET; }

    @Override
    public void run(Player player, User user, String[] args) {
        if (plugin.getVillageBuildEditorManager() == null) {
            sendLocalized(player, Lang.BUILD_EDITOR_WORLD_EDIT_REQUIRED);
            return;
        }
        if (args.length == 1) {
            plugin.getVillageBuildEditorManager().openLevelMenu(player);
            return;
        }

		String action = args[1].toLowerCase();
		if (action.equals(plugin.getCommandLang().getCommand(CommandLang.EDIT_SAVE))) {
			plugin.getVillageBuildEditorManager().saveSession(player);
			return;
		}
		if (action.equals(plugin.getCommandLang().getCommand(CommandLang.EDIT_CANCEL))) {
            plugin.getVillageBuildEditorManager().cancelSession(player);
            return;
        }
        getMessage(Lang.COMMAND_USAGE_BUILD_EDITOR.getPath())
                .processPlaceholder("save", plugin.getCommandLang().getCommand(CommandLang.EDIT_SAVE))
                .processPlaceholder("cancel", plugin.getCommandLang().getCommand(CommandLang.EDIT_CANCEL))
                .sendPrefixedMessage(player);
    }
}
