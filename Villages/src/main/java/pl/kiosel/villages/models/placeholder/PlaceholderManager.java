package pl.kiosel.villages.models.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import pl.kiosel.villages.Wioski;

public class PlaceholderManager {

    private final Wioski plugin;
    private final PlaceholderVillage expansion;

    public PlaceholderManager(Wioski plugin) {
        this.plugin = plugin;
        this.expansion = new PlaceholderVillage(plugin);
    }

    public String replacePlaceholder(Player player, String s) {
        return PlaceholderAPI.setPlaceholders(player, s);
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