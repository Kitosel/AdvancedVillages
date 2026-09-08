package pl.kiosel.villages.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.command.RosaCommand;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.gui.crafting.CraftingEditGUI;
import pl.kiosel.villages.gui.crafting.CraftingGUI;

import java.util.Collections;
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
	public boolean onExecute(CommandSender sender, String s, String[] args) {
		Player player = (Player) sender;
		if (args.length == 0) {
			plugin.getGuiManager().showGUI(player, new CraftingGUI(plugin));
			return true;
		}
		if (args.length == 1 && args[0].equalsIgnoreCase("edit") && plugin.isDev()) {
			if (!player.hasPermission("advancedvillages.crafting.edit")) {
				plugin.getVillageMessages().sendPrefixed(player, Lang.COMMAND_NO_PERMISSION);
				return true;
			}
			plugin.getGuiManager().showGUI(player, new CraftingEditGUI(plugin));
			return true;
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender commandSender, String[] args) {
		if (args.length == 1 && commandSender.hasPermission("advancedvillages.crafting.edit") && plugin.isDev()) {
			return complete(args[0], Collections.singletonList("edit"));
		}
		return EMPTY;
	}
}
