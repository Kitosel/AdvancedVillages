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
