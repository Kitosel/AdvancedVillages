package pl.kiosel.villages.config;

import lombok.Getter;
import pl.kiosel.core.utils.TextUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.CommandLang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pl.kiosel.core.utils.ColorUtils.tl;

public class CommandConfig {

	@Getter private String commandName;
	@Getter private String commandPermission;
	@Getter private List<String> commandAliases;

	@Getter private String spawnCommandName;
	@Getter private String spawnCommandPermission;
	@Getter private String spawnCommandSetPermission;
	@Getter private List<String> spawnCommandAliases;

	private final Map<String, String> command = new HashMap<>();
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
		return message.toLowerCase();
	}

	public void setConfig() {
		command.clear();
		plugin.getDebug().debug("Setting command.yml");
		if (!plugin.getCommandFile().getFile().exists()) {
			plugin.saveResource("command.yml", false);
		}
		plugin.getCommandFile().load();

		var section = plugin.getCommandFile().getConfigurationSection("command-language");
		if (section != null) {
			for (String key : section.getKeys(false)) {
				String text = plugin.getCommandFile().getString("command-language." + key);
				if (text != null) {
					command.put(key, text);
				}
			}
		}

		commandName = getString("command.name", "village");
		commandAliases = getList("command.aliases", TextUtils.of("wioski", "vil"));
		commandPermission = getString("command.permission", "villages.command");

		spawnCommandName = getString("spawn.name", "spawn");
		spawnCommandAliases = getList("spawn.aliases", TextUtils.of("tpspawn"));
		spawnCommandPermission = getString("spawn.permission", "villages.spawn");
		spawnCommandSetPermission = getString("spawn.permission-set", "villages.spawn.set");
	}

	private String getString(String path, String def) {
		if (path == null) return def;
		String value = plugin.getCommandFile().getString(path);
		if (value == null || value.trim().isEmpty()) return def;
		return tl(value);
	}

	private List<String> getList(String path, List<String> def) {
		if (path == null) return def;
		List<String> list = plugin.getCommandFile().getStringList(path);
		if (list.isEmpty()) return def;
		return list;
	}
}
