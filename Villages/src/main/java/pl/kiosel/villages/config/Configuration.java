package pl.kiosel.villages.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class Configuration {

    private final Plugin plugin;
    private FileConfiguration config = null;
    private File file = null;
    private final String name;

    public Configuration(Plugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public void reloadConfig() {
        if(this.file == null)
            this.file = new File(this.plugin.getDataFolder(), name);
        this.config = YamlConfiguration.loadConfiguration(this.file);

        InputStream defaultStream = this.plugin.getResource(name);
        if(defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.config.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getConfig() {
        if(this.config == null)
            reloadConfig();
        return this.config;
    }

    public void saveConfig() {
        if(this.config == null || this.file == null)
            return;
        try {
            this.getConfig().save(this.file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.file, e);
        }
    }

    public void saveDefaultConfig() {
        if(this.file == null)
            this.file = new File(this.plugin.getDataFolder(), name);

        if(!this.file.exists()) {
            this.plugin.saveResource(name, false);
        }
    }
}