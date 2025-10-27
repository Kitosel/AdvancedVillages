package pl.kiosel.villages.config;

import lombok.Getter;
import pl.kiosel.villages.Wioski;
import pl.kiosel.villages.enums.CommandLang;
import pl.kiosel.common.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pl.kiosel.common.utils.ColorUtils.tl;

public class CommandConfig {

	@Getter private String commandName;
	@Getter private String commandPermission;
	@Getter private List<String> commandAliases;

	private final Map<String, String> command = new HashMap<>();
	private final Wioski plugin;

	public CommandConfig(Wioski plugin) {
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
		plugin.getCommandConfig().reloadConfig();

		var section = plugin.getCommandConfig().getConfig().getConfigurationSection("command-language");
		if (section != null) {
			for (String key : section.getKeys(false)) {
				String text = plugin.getCommandConfig().getConfig().getString("command-language." + key);
				if (text != null) {
					command.put(key, text);
				}
			}
		}

		commandName = getString("command.name", "village");
		commandAliases = getList("command.aliases", Utils.of("wioski", "vil"));
		commandPermission = getString("command.permission", "villages.command");
	}

	private String getString(String path, String def) {
		if (path == null) return def;
		String value = plugin.getCommandConfig().getConfig().getString(path);
		if (value == null || value.trim().isEmpty()) return def;
		return tl(value);
	}

	private List<String> getList(String path, List<String> def) {
		if (path == null) return def;
		List<String> list = plugin.getCommandConfig().getConfig().getStringList(path);
		if (list == null || list.isEmpty()) return def;
		return list;
	}
}
