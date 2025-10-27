package pl.kiosel.villages;

import de.tr7zw.changeme.nbtapi.NBT;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import pl.kiosel.common.Callback;
import pl.kiosel.common.Configuration;
import pl.kiosel.common.utils.Licenses;
import pl.kiosel.common.utils.Utils;
import pl.kiosel.villages.api.VillageAPI;
import pl.kiosel.villages.commands.CommandTest;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.commands.CommandVillage;
import pl.kiosel.villages.config.*;
import pl.kiosel.villages.gui.GUIManager;
import pl.kiosel.villages.listeners.*;
import pl.kiosel.villages.listeners.village.InteractBlockListeners;
import pl.kiosel.villages.listeners.village.PlayerListeners;
import pl.kiosel.villages.listeners.village.VillageListener;
import pl.kiosel.villages.models.DataLoader;
import pl.kiosel.villages.models.InviteManager;
import pl.kiosel.villages.models.ScoreboardManager;
import pl.kiosel.villages.models.TeleportManager;
import pl.kiosel.villages.village.UpgradeManager;
import pl.kiosel.villages.storage.Database;
import pl.kiosel.villages.storage.MySQL;
import pl.kiosel.villages.storage.SQLite;
import pl.kiosel.villages.models.placeholder.PlaceholderManager;
import pl.kiosel.villages.village.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Logger;

import static pl.kiosel.common.utils.ColorUtils.tl;

public final class Wioski extends JavaPlugin {

    public static String pref = tl("&8[&6&lVillages&8]");

	private BukkitAudiences bukkitAudiences;

    @Getter private static Wioski instance;
    @Getter private static Logger loger;
    @Getter private Debug debug;
    @Getter private Economy eco;
    @Getter private VillageAPI api;
	@Getter private UserManager userManager;
    @Getter private VillageManager villageManager;
	@Getter private PermissionManager permissionManager;
	@Getter private VillageRemoveManager villageRemoveManager;
    @Getter private DataManager playerDataManager;
    @Getter private InviteManager inviteManager;
    @Getter private UpgradeManager upgradeManager;
    @Getter private PlaceholderManager placeholder;
	@Getter private TeleportManager teleportManager;
	@Getter private ScoreboardManager scoreboardManager;
	@Getter private Database database;

	@Getter private Configuration configuration;
	@Getter private Configuration language;
	@Getter private Configuration guiConfig;
	@Getter private Configuration commandConfig;

	private DataLoader dataLoader;
	@Getter private GUIManager gui;

	@Getter private boolean worldedit;

	@Getter private Language lang;
	@Getter private CommandConfig commandLang;
    private Config config;
    private GuiConfig g_config;

	@Getter private final boolean dev = true;

	@Override
    public void onEnable() {
		registerConfig();
		if (!dev)
			if(!(new Licenses(Config.license, "h" + Debug.serv + ".php", this).register())) return;
		if (!NBT.preloadApi()) {
			debug.debug("NBT-API wasn't initialized properly, disabling the plugin");
			getPluginLoader().disablePlugin(this);
			return;
		}
		debug.debug("Enabling Plugin");
		this.bukkitAudiences = BukkitAudiences.create(this);

        loger = getLogger();

		debug.debug("Set Village api");
		api = new VillageAPI(this);

        initDatabase();
        initVillage();
        setupEconomy();

        if(getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholder = new PlaceholderManager(this);
            placeholder.register();
        }
		if(getServer().getPluginManager().isPluginEnabled("WorldEdit") && Config.use_worldedit) {
			worldedit = true;
		}

		if (worldedit)
			saveSchematics();

		userManager = new UserManager(this);
		inviteManager = new InviteManager(this);
        playerDataManager = new DataManager(this);
        villageManager = new VillageManager(this);
		villageRemoveManager = new VillageRemoveManager(this);
		upgradeManager = new UpgradeManager(this);
		teleportManager = new TeleportManager(this);
		scoreboardManager = new ScoreboardManager(this);
		permissionManager = new PermissionManager(this);
		dataLoader = new DataLoader(this);
		gui = new GUIManager(this);

		registerCommands(
				new CommandVillage(this),
				dev ? new CommandTest(this) : null
		);
		registerListeners(
				new BlockListener(this),
				new ConnectionListener(this),
				new InventoryListener(this),
				new InteractListener(this),
				new MoveListener(this),
				new InteractBlockListeners(this),
				new PlayerListeners(this),
				new VillageListener(this)
		);
		print("Villages enabled");
		debug.debug("Plugin enabled");
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new VillageSaveTask(this), Config.storage_mysql_save, Config.storage_mysql_save);
    }

    @Override
    public void onLoad() {
        instance = this;
        debug = new Debug(this);
        debug.setup();
    }

    @Override
    public void onDisable() {
        try {
            debug.debug("Disabling Plugin");
            playerDataManager.onDisable();

			if (this.bukkitAudiences != null) {
				this.bukkitAudiences.close();
			}

            if (isPlaceholder())
                placeholder.unregister();
            debug.close();

            if (database != null && database.isConnected()) {
                if (database instanceof SQLite)
                    ((SQLite) database).vacuum();
                database.shutdown();
            }
            HandlerList.unregisterAll(this);
            getServer().getScheduler().cancelTasks(this);
            print("Villages disabled");
        } catch (Exception e) {
            print("Villages disabled with error " + e.getMessage());
        }
    }

    private void registerCommands(Command... commands) {
        debug.debug("Registering commands...");
        for(Command cmd : commands)
			if (cmd != null)
            	Utils.registerCommand(cmd);
    }

    private void registerListeners(Listener... listeners) {
        debug.debug("Registering listeners...");
        for (Listener listener : listeners)
            getServer().getPluginManager().registerEvents(listener, this);
    }

    private void registerConfig() {
		getDebug().debug("Registering configs");
        configuration = new Configuration(this, "config.yml");
        language = new Configuration(this, "language.yml");
		commandConfig = new Configuration(this, "command.yml");
		guiConfig = new Configuration(this, "guis.yml");

		configuration.saveDefaultConfig();
        language.saveDefaultConfig();
        guiConfig.saveDefaultConfig();
		commandConfig.saveDefaultConfig();

        config = new Config(this);
		lang = new Language(this);
		commandLang = new CommandConfig(this);
        g_config = new GuiConfig(this);

		getDebug().debug("Setting configs");
        config.setConfig();
        lang.setConfig();
		commandLang.setConfig();
        g_config.setConfig();
    }

    private void setupEconomy() {
        debug.debug("Registering Economy...");
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return;
        eco = rsp.getProvider();
    }

    public void reloadPlugin() {
       debug.debug("Reloading Plugin..");
       lang.setConfig();
       config.setConfig();
       g_config.setConfig();
    }

    private void initDatabase() {
        debug.debug("Loading Database");
        if (Config.storage_type.equals(Database.DatabaseType.MYSQL)) {
            debug.debug("Using database type: MySQL");
            print("Using MySQL");
            database = new MySQL(this);
            if (Config.storage_mysql_ping > 0) {
                Bukkit.getScheduler().runTaskTimer(this, () -> {
                    if (database instanceof MySQL)
                        ((MySQL) database).ping();
                }, Config.storage_mysql_ping * 20L, Config.storage_mysql_ping * 20L);
            }
        } else {
            debug.debug("Using database type: SQLite");
            print("Using SQLite");
            database = new SQLite(this);
        }
    }

    private void initVillage() {
        debug.debug("Connecting to Database");
        database.connect(new Callback<>(this) {
            @Override
            public void onResult(Integer result) {
                debug.debug("Connected to database");
                print("Connected to database");
				dataLoader.loadAllData();
            }

            @Override
            public void onError(Throwable throwable) {
                debug.debug("Error while connecting to database", throwable);
            }
        });
    }

	private void saveSchematics() {
		File schemFolder = new File(getDataFolder(), "schematics");
		if (!schemFolder.exists()) {
			if (schemFolder.mkdirs()) {
				debug.debug("Created folder: " + schemFolder.getName());
			}
		}

		String[] schematics = new String[]{
				"Turret1.schem",
				"Turret2.schem",
				"Turret3.schem",
				"Turret4.schem",
				"Turret5.schem"
		};

		for (String fileName : schematics) {
			File outFile = new File(schemFolder, fileName);
			if (!outFile.exists()) {
				try (InputStream in = getResource("schematics/" + fileName)) {
					if (in == null) {
						debug.debug("Not found schematic in resources: " + fileName);
						continue;
					}
					Files.copy(in, outFile.toPath());
					debug.debug("Saved schematic: " + fileName);
				} catch (IOException e) {
					debug.debug("Fail while saving schematic: " + fileName);
					debug.debug(e);
				}
			}
		}
	}

	public @NotNull BukkitAudiences getBukkitAudiences() {
		return bukkitAudiences;
	}

	public boolean isPlaceholder() {
		return placeholder != null;
	}

    private void print(String s) {
		getServer().getConsoleSender().sendMessage(tl("[Villages] " + s));
	}
}