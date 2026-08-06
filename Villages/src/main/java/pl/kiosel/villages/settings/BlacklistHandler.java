package pl.kiosel.villages.settings;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import pl.kiosel.core.configuration.Config;

import java.util.Arrays;
import java.util.List;

public class BlacklistHandler {

    private final Config blackConfig;

    public BlacklistHandler(Plugin plugin) {
        this.blackConfig = new Config(plugin, "blacklist.yml");
        loadBlacklistFile();
    }

    public boolean isBlacklisted(World world) {
        List<String> list = this.blackConfig.getStringList("settings.blacklist");
        final String checkWorld = world.getName();
        return list.stream().anyMatch(w -> w.equalsIgnoreCase(checkWorld));
    }

    private void loadBlacklistFile() {
        this.blackConfig.addDefault("settings.blacklist", Arrays.asList("world_nether", "world_the_end", "world1", "world2"));
        this.blackConfig.load();

        this.blackConfig.saveChanges();
    }

    public void reload() {
        loadBlacklistFile();
    }
}
