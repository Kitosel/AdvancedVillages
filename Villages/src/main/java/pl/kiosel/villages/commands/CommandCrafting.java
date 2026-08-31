package pl.kiosel.villages.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.command.RosaCommand;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.gui.crafting.GUICrafting;

import java.util.List;

public class CommandCrafting extends RosaCommand {

	private final AdvancedVillages plugin;

	public CommandCrafting(AdvancedVillages plugin) {
		super(plugin, "crafting", List.of("villagecrafting"), "advancedvillages.command.crafting");
		this.plugin = plugin;
		setDescription("Open Crafting gui");
	}

	@Override
	public boolean isPlayerOnly() {
		return true;
	}

	@Override
	public boolean onExecute(CommandSender sender, String s, String[] strings) {
		Player player = (Player) sender;
		plugin.getGuiManager().showGUI(player, new GUICrafting(plugin, player));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender commandSender, String[] strings) {
		return EMPTY;
	}
}
