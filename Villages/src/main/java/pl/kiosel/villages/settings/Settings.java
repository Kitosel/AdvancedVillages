package pl.kiosel.villages.settings;

import pl.kiosel.core.configuration.Config;
import pl.kiosel.core.configuration.ConfigSetting;
import pl.kiosel.core.hooks.EconomyManager;
import pl.kiosel.villages.AdvancedVillages;

public class Settings {
    static final Config CONFIG = AdvancedVillages.getPlugin(AdvancedVillages.class).getCoreConfig();

    public static final ConfigSetting ECONOMY_PLUGIN = new ConfigSetting(CONFIG, "settings.economy", EconomyManager.getEconomy() == null ? "Vault" : EconomyManager.getEconomy().getName(),
            "Which economy plugin should be used?",
            "Supported plugins you have installed: \"" + String.join("\", \"", EconomyManager.getManager().getRegisteredPlugins()) + "\".");
	public static final ConfigSetting WORLDEDIT = new ConfigSetting(CONFIG, "settings.use-worldedit", false,
			"Should plugin use worldedit schematic for villages?");
	public static final ConfigSetting LICENSE = new ConfigSetting(CONFIG, "settings.license", "PUT-YOUR-LICENSE-HERE",
			"Put your license for your plugin");
    public static final ConfigSetting LANGUAGE_MODE = new ConfigSetting(CONFIG, "settings.language", "en_US",
            "The enabled language file.",
            "More language files (if available) can be found in the plugins data folder.");

	public static final ConfigSetting ADDONS_ANTYLOGOUT_ENABLE = new ConfigSetting(CONFIG, "addons.scoreboard", true,
			"Should addons be enabled");
	public static final ConfigSetting ADDONS_SCOREBOARD_ENABLE = new ConfigSetting(CONFIG, "addons.scoreboard", true);
	public static final ConfigSetting ADDONS_TABLIST_ENABLE = new ConfigSetting(CONFIG, "addons.scoreboard", true);

	public static final ConfigSetting SPAWN_WORLD = new ConfigSetting(CONFIG, "spawn.world", "world");
	public static final ConfigSetting SPAWN_X = new ConfigSetting(CONFIG, "spawn.x", 0.5);
	public static final ConfigSetting SPAWN_Y = new ConfigSetting(CONFIG, "spawn.y", 60);
	public static final ConfigSetting SPAWN_Z = new ConfigSetting(CONFIG, "spawn.z", 0.5);
	public static final ConfigSetting SPAWN_YAW = new ConfigSetting(CONFIG, "spawn.yaw", 90);
	public static final ConfigSetting SPAWN_PITCH = new ConfigSetting(CONFIG, "spawn.pitch", 0);
	public static final ConfigSetting SPAWN_COMMAND_ENABLE = new ConfigSetting(CONFIG, "spawn.command.enable", true);
	public static final ConfigSetting SPAWN_COMMAND_DELAY = new ConfigSetting(CONFIG, "spawn.command.delay", 5, "Time in seconds");
	public static final ConfigSetting SPAWN_COMMAND_COOLDOWN = new ConfigSetting(CONFIG, "spawn.command.cooldown", 60);

	public static final ConfigSetting TELEPORT_CANCEL_ON_MOVE = new ConfigSetting(CONFIG, "teleport.cancel-on-move", true,
			"Cancel teleport on player move");
	public static final ConfigSetting TELEPORT_BETWEEN_COOLDOWN = new ConfigSetting(CONFIG, "teleport.between-cooldown", 60,
			"In seconds");
	public static final ConfigSetting TELEPORT_COOLDOWN = new ConfigSetting(CONFIG, "teleport.cooldown", 5);

	public static final ConfigSetting CHAT_FORMAT_ENABLED = new ConfigSetting(CONFIG, "chat.enabled", false);
	public static final ConfigSetting CHAT_FORMAT_VILLAGE = new ConfigSetting(CONFIG, "chat.format", "&7[&c%village_tag%&7] &r%player%&7: &r%message%");
	public static final ConfigSetting CHAT_FORMAT_NO_VILLAGE = new ConfigSetting(CONFIG, "chat.format-novillage", "%player%&7: &r%message%");

	public static final ConfigSetting VILLAGE_MINIMAL_DISTANCE = new ConfigSetting(CONFIG, "village.village-minimal-distance", 300,
			"Distance between another villages");
	public static final ConfigSetting VILLAGE_SPAWN_MINIMAL_DISTANCE = new ConfigSetting(CONFIG, "village.spawn-minimal-distance", 1000);
	public static final ConfigSetting VILLAGE_MAX_MEMBERS = new ConfigSetting(CONFIG, "village.max-members", 7, "Real max is 27");
	public static final ConfigSetting VILLAGE_MAX_TAG_LENGTH = new ConfigSetting(CONFIG, "village.tag-max-length", 5);
	public static final ConfigSetting VILLAGE_UPGRADE_ECO = new ConfigSetting(CONFIG, "village.upgrade.eco", true);
	public static final ConfigSetting VILLAGE_UPGRADE_XP = new ConfigSetting(CONFIG, "village.upgrade.xp", false);
	public static final ConfigSetting VILLAGE_UPGRADE_ITEMS = new ConfigSetting(CONFIG, "village.upgrade.items", true);

	public static final ConfigSetting EFFECTS_REGENERATION_COST = new ConfigSetting(CONFIG, "village.effects.regeneration.cost", 125);
	public static final ConfigSetting EFFECTS_REGENERATION_AMPLIFIER = new ConfigSetting(CONFIG, "village.effects.regeneration.amplifier", 1);
	public static final ConfigSetting EFFECTS_SPEED_COST = new ConfigSetting(CONFIG, "village.effects.speed.cost", 225);
	public static final ConfigSetting EFFECTS_SPEED_AMPLIFIER = new ConfigSetting(CONFIG, "village.effects.speed.amplifier", 1);
	public static final ConfigSetting EFFECTS_JUMP_BOOST_COST = new ConfigSetting(CONFIG, "village.effects.jump-boost.cost", 350);
	public static final ConfigSetting EFFECTS_JUMP_BOOST_AMPLIFIER = new ConfigSetting(CONFIG, "village.effects.jump-boost.amplifier", 1);
	public static final ConfigSetting EFFECTS_HASTE_COST = new ConfigSetting(CONFIG, "village.effects.haste.cost", 500);
	public static final ConfigSetting EFFECTS_HASTE_AMPLIFIER = new ConfigSetting(CONFIG, "village.effects.haste.amplifier", 2);

    public static void setupConfig() {
        CONFIG.load();
        CONFIG.setAutoremove(true).setAutosave(true);

        // convert economy settings
        if (CONFIG.getBoolean("Economy.Use Vault Economy") && EconomyManager.getManager().isEnabled("Vault")) {
            CONFIG.set("Main.Economy", "Vault");
        } else if (CONFIG.getBoolean("Economy.Use Reserve Economy") && EconomyManager.getManager().isEnabled("Reserve")) {
            CONFIG.set("Main.Economy", "Reserve");
        } else if (CONFIG.getBoolean("Economy.Use Player Points Economy") && EconomyManager.getManager().isEnabled("PlayerPoints")) {
            CONFIG.set("Main.Economy", "PlayerPoints");
        }

        CONFIG.saveChanges();
    }
}
