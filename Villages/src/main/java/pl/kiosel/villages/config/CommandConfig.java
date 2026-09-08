package pl.kiosel.villages.config;

import lombok.Getter;
import pl.kiosel.villages.AdvancedVillages;

import java.util.*;

public class CommandConfig {

	@Getter private String commandName;
	@Getter private List<String> commandAliases;

	@Getter private String spawnCommandName;
	@Getter private List<String> spawnCommandAliases;

	@Getter private String craftingCommandName;
	@Getter private List<String> craftingCommandAliases;

	private volatile Map<String, String> command = Collections.emptyMap();
	private final AdvancedVillages plugin;

	public CommandConfig(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public String getCommand(CommandLang lang) {
		String message = command.get(lang.getPath());
		if (message == null || message.trim().isEmpty()) {
			message = lang.getDef();
		}
		if (message.contains(" ")) {
			message = lang.getDef();
		}
		return message.toLowerCase(Locale.ROOT);
	}

	public void setConfig() {
		plugin.getDebug().debug("Setting command.yml");
		this.reload();

		commandName = getString("command.name", "village");
		commandAliases = getList("command.aliases", List.of("villages", "vil", "v"));

		spawnCommandName = getString("spawn.name", "spawn");
		spawnCommandAliases = getList("spawn.aliases", List.of("tpspawn"));

		craftingCommandName = getString("spawn.crafting", "crafting");
		craftingCommandAliases = getList("spawn.aliases", List.of("villagecrafting"));
	}

	public void reload() {
		Map<String, String> refreshed = new HashMap<>();

		var section = plugin.getCommandFile().getConfigurationSection("command-language");
		if (section != null) {
			for (String key : section.getKeys(false)) {
				String text = plugin.getCommandFile().getString("command-language." + key);
				if (text != null) {
					refreshed.put(key, text);
				}
			}
		}
		this.command = Collections.unmodifiableMap(refreshed);
	}

	private String getString(String path, String def) {
		if (path == null) return def;
		String value = plugin.getCommandFile().getString(path);
		if (value == null || value.trim().isEmpty()) return def;
		return value;
	}

	private List<String> getList(String path, List<String> def) {
		if (path == null) return def;
		List<String> list = plugin.getCommandFile().getStringList(path);
		if (list.isEmpty()) return def;
		return list;
	}
}
