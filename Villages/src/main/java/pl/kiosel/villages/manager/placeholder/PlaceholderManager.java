package pl.kiosel.villages.manager.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;

import java.util.Map;

public class PlaceholderManager {

    private final AdvancedVillages plugin;
    private final PlaceholderVillage expansion;

    public PlaceholderManager(AdvancedVillages plugin) {
        this.plugin = plugin;
        this.expansion = new PlaceholderVillage(plugin);
    }

    public String replacePlaceholder(Player player, String text) {
		if (!plugin.isPlaceholder())
			return text;
        return PlaceholderAPI.setPlaceholders(player, text);
    }

	public String replacePlaceholder(Player player, String content, Map<String, Object> replacements) {
		for (Map.Entry<String, Object> entry : replacements.entrySet()) {
			content = content.replace(String.format("{%s}", entry.getKey()), entry.getValue().toString());
		}
		return replacePlaceholder(player, content);
	}

    public void register() {
        plugin.getDebug().debug("Placeholder - Register");
        expansion.register();
    }

    public void unregister() {
        plugin.getDebug().debug("Placeholder - UnRegister");
        expansion.unregister();
    }
}