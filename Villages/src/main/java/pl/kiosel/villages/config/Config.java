package pl.kiosel.villages.config;

import pl.kiosel.common.Configuration;
import pl.kiosel.villages.Wioski;
import pl.kiosel.villages.storage.Database;
import pl.kiosel.common.utils.Utils;

import java.util.List;

import static pl.kiosel.common.utils.ColorUtils.tl;

public class Config {

    private final Wioski plugin;
	private final Configuration config;

    public Config(Wioski plugin) {
        this.plugin = plugin;
		this.config = plugin.getConfiguration();
    }

    public static String license;

	public static int minimal_distance;
	public static int spawn_minimal_distance;
	public static int max_tag_length;
	public static int max_members;

	public static String village_chat_format;

    public static int cost_set;
    public static int cost_upgrade_1;
    public static int cost_upgrade_2;
    public static int cost_upgrade_3;
    public static int cost_upgrade_4;

	public static int size_set;
	public static int size_upgrade_1;
	public static int size_upgrade_2;
	public static int size_upgrade_3;
	public static int size_upgrade_4;

	public static int effect_regeneration_cost;
	public static int effect_speed_cost;
	public static int effect_jump_cost;
	public static int effect_haste_cost;

	public static int effect_regeneration_amplifier;
	public static int effect_speed_amplifier;
	public static int effect_jump_amplifier;
	public static int effect_haste_amplifier;

    public static List<String> enabled_worlds;

    public static boolean debug_enable;
	public static boolean use_worldedit;

    public static Database.DatabaseType storage_type;
    public static String storage_mysql_host;
    public static int storage_mysql_port;
	public static int storage_mysql_save;
    public static String storage_mysql_database;
    public static String storage_mysql_user;
    public static String storage_mysql_pass;
    public static String storage_mysql_table;
    public static int storage_mysql_ping;

	public static int teleport_cooldown;
	public static int teleport_between_cooldown;
	public static boolean cancel_on_move;

    public static boolean scoreboard_enabled;
    public static String scoreboard_title;
    public static List<String> scoreboard_score;

	public static boolean scoreboard_animation_enabled;
	public static List<String> scoreboard_animation_titles;
	public static int scoreboard_animation_speed;

    public static String host = "{WEEEB}".replace("{WEEEB}", "kiosel")+".";

    public void setConfig() {
        plugin.getDebug().debug("Setting config.yml");
		plugin.getConfiguration().reloadConfig();

        license = getString("license", "");
        debug_enable = getBoolean("debug", false);
		use_worldedit = getBoolean("use-worldedit", false);

		teleport_cooldown = getInt("teleport.cooldown", 5);
		teleport_between_cooldown = getInt("teleport.between-cooldown", 60);
		cancel_on_move = getBoolean("teleport.cancel-on-move", true);

		minimal_distance = getInt("village.village-minimal-distance", 300);
		spawn_minimal_distance = getInt("village.spawn-minimal-distance", 1000);
		max_tag_length = getInt("village.tag-max-length", 5);
		max_members = getInt("village.max-members", 7, 27);

		village_chat_format = getString("chat.village-chat", "&8[&5&lVillage&8] &f%player_name%&7: &f%message%");

		cost_set = getInt("village.default.cost", 1000);
        cost_upgrade_1 = getInt("village.upgrade.1.cost", 2000);
        cost_upgrade_2 = getInt("village.upgrade.2.cost", 2500);
        cost_upgrade_3 = getInt("village.upgrade.3.cost", 3000);
        cost_upgrade_4 = getInt("village.upgrade.4.cost", 4000);

		size_set = getInt("village.default.size", 25);
		size_upgrade_1 = getInt("village.upgrade.1.size", 50);
		size_upgrade_2 = getInt("village.upgrade.2.size", 75);
		size_upgrade_3 = getInt("village.upgrade.3.size", 100);
		size_upgrade_4 = getInt("village.upgrade.4.size", 125);

		effect_regeneration_cost = getInt("village.effects.regeneration.cost",125);
		effect_regeneration_amplifier = getInt("village.effects.regeneration.amplifier", 1);
		effect_speed_cost = getInt("village.effects.speed.cost", 225);
		effect_speed_amplifier = getInt("village.effects.speed.amplifier", 1);
		effect_jump_cost = getInt("village.effects.jump-boost.cost", 350);
		effect_jump_amplifier = getInt("village.effects.jump-boost.amplifier", 1);
		effect_haste_cost = getInt("village.effects.haste.cost", 500);
		effect_haste_amplifier = getInt("village.effects.haste.amplifier", 2);

        enabled_worlds = getList("enabled-worlds", List.of("world"));

        storage_type = getStorageType();
        storage_mysql_host = getString("storage.mysql.host", "localhost");
        storage_mysql_port = getInt("storage.mysql.port", 3306);
        storage_mysql_save = getInt("storage.save", 30) * 20;
        storage_mysql_ping = getInt("storage.mysql.ping-interval", 3600);
        storage_mysql_database = getString("storage.mysql.database", "minecraft");
        storage_mysql_user = getString("storage.mysql.username", "admin");
        storage_mysql_pass = getString("storage.mysql.password", "admin");
        storage_mysql_table = getString("storage.mysql.table", "villages");

        scoreboard_enabled = getBoolean("scoreboard.enable", true);
        scoreboard_title = getString("scoreboard.title", "&6&lVillages");
        scoreboard_score = getList("scoreboard.score", Utils.of(
						"&7------------",
						"&7Nick: &e%player_name%",
						"&7Money: &e%player_money%",
						"&7Village:",
						" &7Owner: &e%village_owner%",
						" &7Life: &e%village_life%",
						" &7Level: &e%village_level%",
						" &7Tag: &e%village_tag%",
						"&7--------&7----"));

		scoreboard_animation_enabled = getBoolean("scoreboard.animation.enabled", true);
		scoreboard_animation_titles = getList("scoreboard.animation.titles", Utils.of(
				"&6&lVillages", "&b&lVillages", "&f&lWioski", "&b&lV&f&li&b&ll&b&ll&f&la&b&lg&f&le&f&ls"
		));
		scoreboard_animation_speed = getInt("scoreboard.animation.speed", 10);
    }

    private String getString(String path, String def) {
        if(path == null) return def;
        if(config.getConfig().getString(path) == null) return def;
        return tl(config.getConfig().getString(path));
    }

    private Database.DatabaseType getStorageType() {
        String type = config.getConfig().getString("storage.type");
        if(type == null) return Database.DatabaseType.SQL;
        return Database.DatabaseType.valueOf(type.toUpperCase());
    }

    private Boolean getBoolean(String path, boolean def) {
        if(path == null) return def;
        if(config.getConfig().getString(path) == null) return def;
        return config.getConfig().getBoolean(path);
    }

	private Integer getInt(String path, int def) {
		if(path == null) return def;
		return config.getConfig().getInt(path);
	}

	private Integer getInt(String path, int def, int max) {
		if(path == null) return def;
		int maxint = config.getConfig().getInt(path);
		return Math.max(maxint, max);
	}

    private List<String> getList(String path, List<String> def) {
        if(path == null) return def;
        if(config.getConfig().getString(path) == null) return def;
        return config.getConfig().getStringList(path);
    }
}