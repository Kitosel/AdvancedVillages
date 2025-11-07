package pl.kiosel.villages.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kiosel.core.commands.AbstractCommand;
import pl.kiosel.core.gui.GuiManager;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.gui.crafting.GUICrafting;

import java.util.List;

public class CommandCrafting extends AbstractCommand {

	private final AdvancedVillages plugin;
	private final GuiManager guiManager;

	public CommandCrafting(AdvancedVillages plugin, GuiManager guiManager) {
		super(CommandType.PLAYER_ONLY, "crafting");
		this.plugin = plugin;
		this.guiManager = guiManager;
	}

	@Override
	protected ReturnType runCommand(CommandSender sender, String... args) {
		Player player = (Player) sender;
		guiManager.showGUI(player, new GUICrafting(plugin, player));
		return ReturnType.SUCCESS;
	}

	@Override
	protected List<String> onTab(CommandSender sender, String... args) {
		return List.of();
	}

	@Override
	public String getPermissionNode() {
		return "advancedvillages.command.crafting";
	}

	@Override
	public String getSyntax() {
		return "crafting";
	}

	@Override
	public String getDescription() {
		return "Open Crafting gui";
	}
}
