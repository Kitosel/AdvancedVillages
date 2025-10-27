package pl.kiosel.villages.config;

import pl.kiosel.villages.Wioski;
import pl.kiosel.villages.enums.Lang;

import java.util.HashMap;
import java.util.Map;

import static pl.kiosel.common.utils.ColorUtils.tl;

public class Language {

    private final Map<String, String> messages = new HashMap<>();
    private final Wioski plugin;

    public Language(Wioski plugin) {
        this.plugin = plugin;
    }

    public String getMessage(Lang lang) {
        String message = messages.get(lang.getPath());
        if (message == null)
            message = lang.getDef();

        return tl(message.replace("%PREFIX%", Wioski.pref));
    }

    public void setConfig() {
        messages.clear();
		plugin.getDebug().debug("Setting language.yml");
		plugin.getLanguage().reloadConfig();

        Wioski.pref = getString("prefix", "&8[&6&lVillages&8]");

		var section = plugin.getLanguage().getConfig();
		if (section != null) {
			for (String key : section.getKeys(false)) {
				String text = plugin.getLanguage().getConfig().getString(key);
				if (text != null) {
					messages.put(key, text);
				}
			}
		}
    }

    private String getString(String path, String def) {
        if(path == null) return def;
        if(plugin.getLanguage().getConfig().getString(path) == null) return def;

        return tl(plugin.getLanguage().getConfig().getString(path)).replace("%PREFIX%", Wioski.pref);
    }
}