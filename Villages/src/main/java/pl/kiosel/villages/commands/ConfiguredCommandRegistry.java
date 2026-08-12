package pl.kiosel.villages.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import pl.kiosel.core.utils.ReflectionUtils;
import pl.kiosel.villages.AdvancedVillages;

import java.util.Map;
import java.util.logging.Level;

public final class ConfiguredCommandRegistry {

	private static final String FALLBACK_PREFIX = "village";

	private final AdvancedVillages plugin;
	private CommandVillage villageCommand;
	private CommandSpawn spawnCommand;

	public ConfiguredCommandRegistry(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public synchronized void register() {
		if (this.villageCommand == null && this.spawnCommand == null) {
			registerCommands();
		}
	}

	public synchronized void reload() {
		if (this.villageCommand != null)
			this.villageCommand.reloadArguments();
		Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
	}

	private void registerCommands() {
		CommandVillage newVillageCommand = new CommandVillage(this.plugin);
		CommandSpawn newSpawnCommand = new CommandSpawn(this.plugin);

		try {
			plugin.getDebug().debug("Registered prefix" + FALLBACK_PREFIX);
			ReflectionUtils.registerCommand(FALLBACK_PREFIX, newVillageCommand);
			plugin.getDebug().debug("Registering command " + newVillageCommand.getName());
			ReflectionUtils.registerCommand(FALLBACK_PREFIX, newSpawnCommand);
			plugin.getDebug().debug("Registering command " + newSpawnCommand.getName());

			this.villageCommand = newVillageCommand;
			plugin.getDebug().debug("Registered command " + villageCommand.getName());
			this.spawnCommand = newSpawnCommand;
			plugin.getDebug().debug("Registered command " + spawnCommand.getName());
			Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
		} catch (RuntimeException exception) {
			this.plugin.getLogger().log(Level.SEVERE, "Could not register configured commands", exception);
			plugin.getDebug().debug("!--------------------------------------!");
			plugin.getDebug().debug("Could not register configured commands");
			plugin.getDebug().debug("!--------------------------------------!");
		}
	}

	public synchronized void unregister() {
		try {
			CommandMap commandMap = ReflectionUtils.getCommandMap();
			Map<String, Command> knownCommands = ReflectionUtils.getKnownCommands(commandMap);
			ReflectionUtils.unregisterCommand(knownCommands, this.villageCommand);
			ReflectionUtils.unregisterCommand(knownCommands, this.spawnCommand);
			this.villageCommand = null;
			this.spawnCommand = null;
		} catch (ReflectiveOperationException | RuntimeException exception) {
			this.plugin.getLogger().log(Level.WARNING, "Could not unregister configured commands", exception);
		}
	}
}
