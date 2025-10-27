package pl.kiosel.common;

import org.bukkit.configuration.file.FileConfiguration;

public interface IConfig {

    void reloadConfig();
    void saveConfig();
    void saveDefaultConfig();
    FileConfiguration getConfig();

}