package pl.kiosel.villages.config;

import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.rosacore.hook.economy.EconomyManager;
import pl.kiosel.villages.AdvancedVillages;

import java.util.Arrays;
import java.util.List;

public class Settings {

	static final AdvancedVillages plugin = AdvancedVillages.getPlugin(AdvancedVillages.class);
    static final RosaConfig CONFIG = plugin.getCoreConfig();

	private static final EconomyManager ECONOMY_REGISTRY = plugin.getHookManager().getEconomy();

	public static final ConfigSetting ECONOMY_PLUGIN = new ConfigSetting(CONFIG,
					"settings.economy", ECONOMY_REGISTRY.getActiveHook()
							.map(hook -> hook.getName()).orElse("Vault"),
					"Which economy plugin should be used?",
					"Supported plugins you have installed: \"" + String.join("\", \"", getEnabledEconomies()) + "\".");
	public static final ConfigSetting WORLDEDIT = new ConfigSetting(CONFIG, "settings.use-worldedit", false,
			"Should plugin use worldedit schematic for villages?");
	public static final ConfigSetting LANGUAGE_MODE = new ConfigSetting(CONFIG, "settings.language", "en_US",
			"The enabled language file.",
			"More language files (if available) can be found in the plugins data folder.");
	public static final ConfigSetting TIME_ZONE = new ConfigSetting(CONFIG, "settings.time-zone", "Europe/Warsaw");

	public static final ConfigSetting ADDONS_ANTYLOGOUT_ENABLE = new ConfigSetting(CONFIG, "addons.antylogout", true,
			"Should addons be enabled");
	public static final ConfigSetting ADDONS_SCOREBOARD_ENABLE = new ConfigSetting(CONFIG, "addons.scoreboard", true);
	public static final ConfigSetting ADDONS_TABLIST_ENABLE = new ConfigSetting(CONFIG, "addons.tablist", true);
	public static final ConfigSetting ADDONS_SPAWN_ENABLE = new ConfigSetting(CONFIG, "addons.spawn", true);
	public static final ConfigSetting ADDONS_QUESTS_ENABLE = new ConfigSetting(CONFIG, "addons.quests", true,
			"Should shared village quests be enabled?");
	public static final ConfigSetting ADDONS_DEVELOPMENT_ENABLE = new ConfigSetting(CONFIG, "addons.development", true,
			"Should the village development tree be enabled?");
	public static final ConfigSetting ADDONS_VILLAGE_ANIMATIONS_ENABLE = new ConfigSetting(CONFIG, "addons.village-animations", true,
			"Should village creation, level and removal animations be enabled globally?");

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
	public static final ConfigSetting VILLAGE_DESTROY_PROTECTION = new ConfigSetting(CONFIG, "village.destroy-protection", "12h", "How many hours village should be protected after attack");
	public static final ConfigSetting VILLAGE_DEFAULT_LIVES = new ConfigSetting(CONFIG, "village.default-lives", 3);
	public static final ConfigSetting VILLAGE_MAX_LIVES = new ConfigSetting(CONFIG, "village.max-lives", 3);
	public static final ConfigSetting VILLAGE_REMOVE_BEACONS = new ConfigSetting(CONFIG, "village.remove-beacons", true, "Remove beacons when deleting a village?");
	public static final ConfigSetting VILLAGE_INVITE_EXPIRE = new ConfigSetting(CONFIG, "village.invite-expire", 30, "How long until the invitation expires? (seconds)");
	public static final ConfigSetting VILLAGE_UPGRADE_ECO = new ConfigSetting(CONFIG, "village.upgrade.eco", true);
	public static final ConfigSetting VILLAGE_UPGRADE_XP = new ConfigSetting(CONFIG, "village.upgrade.xp", false);
	public static final ConfigSetting VILLAGE_UPGRADE_ITEMS = new ConfigSetting(CONFIG, "village.upgrade.items", true);
	public static final ConfigSetting VILLAGE_UPGRADE_NO_TAG = new ConfigSetting(CONFIG, "village.upgrade-without-tag", false, "Can a village be upgraded without being tagged?");
	public static final ConfigSetting VILLAGE_BLACKLIST_AS_WHITELIST = new ConfigSetting(CONFIG, "village.blacklist-as-whitelist", false, "Invert blacklist into whitelist");
	public static final ConfigSetting VILLAGE_BLACKLISTED_WORLDS = new ConfigSetting(CONFIG, "village.blacklisted-worlds", Arrays.asList("world_nether", "world_the_end", "world1", "world2"), "Where village can't be placed");
	public static final ConfigSetting VILLAGE_ATTACK_WHEN_OFFLINE = new ConfigSetting(CONFIG, "village.attack-when-offline", false, "Can a village be attacked when none of its members are present?");

	public static final ConfigSetting EFFECTS_REGENERATION_COST = new ConfigSetting(CONFIG, "village.effects.regeneration.cost", 125);
	public static final ConfigSetting EFFECTS_REGENERATION_AMPLIFIER = new ConfigSetting(CONFIG, "village.effects.regeneration.amplifier", 1);
	public static final ConfigSetting EFFECTS_SPEED_COST = new ConfigSetting(CONFIG, "village.effects.speed.cost", 225);
	public static final ConfigSetting EFFECTS_SPEED_AMPLIFIER = new ConfigSetting(CONFIG, "village.effects.speed.amplifier", 1);
	public static final ConfigSetting EFFECTS_JUMP_BOOST_COST = new ConfigSetting(CONFIG, "village.effects.jump-boost.cost", 350);
	public static final ConfigSetting EFFECTS_JUMP_BOOST_AMPLIFIER = new ConfigSetting(CONFIG, "village.effects.jump-boost.amplifier", 1);
	public static final ConfigSetting EFFECTS_HASTE_COST = new ConfigSetting(CONFIG, "village.effects.haste.cost", 500);
	public static final ConfigSetting EFFECTS_HASTE_AMPLIFIER = new ConfigSetting(CONFIG, "village.effects.haste.amplifier", 2);

    public static void setupConfig(AdvancedVillages plugin) {
		if (!CONFIG.load().isSuccess())
			throw new IllegalStateException("Could not load config.yml");

        // convert economy settings
		EconomyManager economies = plugin.getHookManager().getEconomy();
        if (CONFIG.getBoolean("Economy.Use Vault Economy") && economies.isAvailable("Vault")) {
            CONFIG.set("settings.economy", "Vault");
		} else if (CONFIG.getBoolean("Economy.Use Reserve Economy") && economies.isAvailable("Reserve")) {
            CONFIG.set("settings.economy", "Reserve");
		} else if (CONFIG.getBoolean("Economy.Use Player Points Economy") && economies.isAvailable("PlayerPoints")) {
            CONFIG.set("settings.economy", "PlayerPoints");
        }

		if (CONFIG.isDirty() && !CONFIG.save().isSuccess())
			plugin.getRosaLogger().warning("Could not save migrated settings in config.yml");
    }

	private static List<String> getEnabledEconomies() {
		return ECONOMY_REGISTRY.getAvailableNames();
	}
}
