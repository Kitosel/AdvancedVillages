package pl.kiosel.villages.config;

import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.rosacore.hook.RosaHook;
import pl.kiosel.rosacore.hook.economy.EconomyManager;
import pl.kiosel.villages.AdvancedVillages;

import java.util.Arrays;

public class Settings {

	static final AdvancedVillages plugin = AdvancedVillages.getPlugin(AdvancedVillages.class);
    static final RosaConfig CONFIG = plugin.getCoreConfig();

	private static final EconomyManager ECONOMY_REGISTRY = plugin.getHookManager().getEconomy();

	public static final ConfigSet ECONOMY_PLUGIN = new ConfigSet(CONFIG, "settings.economy",
			ECONOMY_REGISTRY.getActiveHook().map(RosaHook::getName).orElse("Vault"));
	public static final ConfigSet WORLDEDIT = new ConfigSet(CONFIG, "settings.use-worldedit", false);
	public static final ConfigSet LANGUAGE_MODE = new ConfigSet(CONFIG, "settings.language", "en_US");
	public static final ConfigSet TIME_ZONE = new ConfigSet(CONFIG, "settings.time-zone", "Europe/Warsaw");

	public static final ConfigSet ADDONS_ANTYLOGOUT_ENABLE = new ConfigSet(CONFIG, "addons.antylogout", true);
	public static final ConfigSet ADDONS_SCOREBOARD_ENABLE = new ConfigSet(CONFIG, "addons.scoreboard", true);
	public static final ConfigSet ADDONS_TABLIST_ENABLE = new ConfigSet(CONFIG, "addons.tablist", true);
	public static final ConfigSet ADDONS_SPAWN_ENABLE = new ConfigSet(CONFIG, "addons.spawn", true);

	public static final ConfigSet TELEPORT_CANCEL_ON_MOVE = new ConfigSet(CONFIG, "teleport.cancel-on-move", true);
	public static final ConfigSet TELEPORT_BETWEEN_COOLDOWN = new ConfigSet(CONFIG, "teleport.between-cooldown", "60s");
	public static final ConfigSet TELEPORT_COOLDOWN = new ConfigSet(CONFIG, "teleport.cooldown", "5s");

	public static final ConfigSet CHAT_FORMAT_ENABLED = new ConfigSet(CONFIG, "chat.enabled", false);
	public static final ConfigSet CHAT_FORMAT_VILLAGE = new ConfigSet(CONFIG, "chat.format", "&7[&c%village_tag%&7] &r%player%&7: &r%message%");
	public static final ConfigSet CHAT_FORMAT_NO_VILLAGE = new ConfigSet(CONFIG, "chat.format-novillage", "%player%&7: &r%message%");

	public static final ConfigSet VILLAGE_MINIMAL_DISTANCE = new ConfigSet(CONFIG, "village.village-minimal-distance", 300);
	public static final ConfigSet VILLAGE_SPAWN_MINIMAL_DISTANCE = new ConfigSet(CONFIG, "village.spawn-minimal-distance", 1000);
	public static final ConfigSet VILLAGE_MAX_MEMBERS = new ConfigSet(CONFIG, "village.max-members", 7);
	public static final ConfigSet VILLAGE_MAX_TAG_LENGTH = new ConfigSet(CONFIG, "village.tag-max-length", 5);
	public static final ConfigSet VILLAGE_DESTROY_PROTECTION = new ConfigSet(CONFIG, "village.destroy-protection", "12h");
	public static final ConfigSet VILLAGE_DEFAULT_LIVES = new ConfigSet(CONFIG, "village.default-lives", 3);
	public static final ConfigSet VILLAGE_MAX_LIVES = new ConfigSet(CONFIG, "village.max-lives", 3);
	public static final ConfigSet VILLAGE_REMOVE_BEACONS = new ConfigSet(CONFIG, "village.remove-beacons", true);
	public static final ConfigSet VILLAGE_INVITE_EXPIRE = new ConfigSet(CONFIG, "village.invite-expire", 30);
	public static final ConfigSet VILLAGE_UPGRADE_ECO = new ConfigSet(CONFIG, "village.upgrade.eco", true);
	public static final ConfigSet VILLAGE_UPGRADE_XP = new ConfigSet(CONFIG, "village.upgrade.xp", false);
	public static final ConfigSet VILLAGE_UPGRADE_ITEMS = new ConfigSet(CONFIG, "village.upgrade.items", true);
	public static final ConfigSet VILLAGE_UPGRADE_NO_TAG = new ConfigSet(CONFIG, "village.upgrade-without-tag", false);
	public static final ConfigSet VILLAGE_BLACKLIST_AS_WHITELIST = new ConfigSet(CONFIG, "village.blacklist-as-whitelist", false);
	public static final ConfigSet VILLAGE_BLACKLISTED_WORLDS = new ConfigSet(CONFIG, "village.blacklisted-worlds",
			Arrays.asList("world_nether", "world_the_end", "world1", "world2"));
	public static final ConfigSet VILLAGE_ATTACK_WHEN_OFFLINE = new ConfigSet(CONFIG, "village.attack-when-offline", false);

	public static final ConfigSet EFFECTS_REGENERATION_COST = new ConfigSet(CONFIG, "village.effects.regeneration.cost", 125);
	public static final ConfigSet EFFECTS_REGENERATION_AMPLIFIER = new ConfigSet(CONFIG, "village.effects.regeneration.amplifier", 1);
	public static final ConfigSet EFFECTS_SPEED_COST = new ConfigSet(CONFIG, "village.effects.speed.cost", 225);
	public static final ConfigSet EFFECTS_SPEED_AMPLIFIER = new ConfigSet(CONFIG, "village.effects.speed.amplifier", 1);
	public static final ConfigSet EFFECTS_JUMP_BOOST_COST = new ConfigSet(CONFIG, "village.effects.jump-boost.cost", 350);
	public static final ConfigSet EFFECTS_JUMP_BOOST_AMPLIFIER = new ConfigSet(CONFIG, "village.effects.jump-boost.amplifier", 1);
	public static final ConfigSet EFFECTS_HASTE_COST = new ConfigSet(CONFIG, "village.effects.haste.cost", 500);
	public static final ConfigSet EFFECTS_HASTE_AMPLIFIER = new ConfigSet(CONFIG, "village.effects.haste.amplifier", 2);

	public static final ConfigSet FIRST_STEPS = new ConfigSet(CONFIG, "first-steps-completed", false);

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
}
