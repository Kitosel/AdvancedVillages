package pl.kiosel.villages;

import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import pl.kiosel.villages.addons.tablist.services.service.PlayerPlaceholdersService;
import pl.kiosel.villages.config.Configuration;
import pl.kiosel.core.MetaCore;
import pl.kiosel.core.MetaPlugin;
import pl.kiosel.core.commands.CommandManager;
import pl.kiosel.core.compatibility.CompatibleMaterial;
import pl.kiosel.core.configuration.Config;
import pl.kiosel.core.dependencies.de.tr7zw.nbtapi.NBT;
import pl.kiosel.core.gui.GuiManager;
import pl.kiosel.core.hooks.EconomyManager;
import pl.kiosel.core.input.Licenses;
import pl.kiosel.core.utils.ReflectionUtils;
import pl.kiosel.dependencies.com.cryptomorin.xseries.XMaterial;
import pl.kiosel.villages.api.VillageAPI;
import pl.kiosel.villages.commands.CommandCrafting;
import pl.kiosel.villages.commands.CommandTest;
import pl.kiosel.villages.commands.CommandVillage;
import pl.kiosel.villages.config.*;
import pl.kiosel.villages.addons.tablist.TablistBroadcastHandler;
import pl.kiosel.villages.addons.tablist.services.service.BasicPlaceholdersService;
import pl.kiosel.villages.addons.tablist.services.service.TimePlaceholdersService;
import pl.kiosel.villages.addons.tablist.services.TablistPlaceholdersService;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.addons.tablist.services.RankPlaceholdersService;
import pl.kiosel.villages.data.village.handler.InteractBlockListeners;
import pl.kiosel.villages.data.village.level.LevelManager;
import pl.kiosel.villages.listeners.*;
import pl.kiosel.villages.data.village.handler.BlockItemListener;
import pl.kiosel.villages.data.village.handler.PlayerListeners;
import pl.kiosel.villages.data.village.handler.VillageListener;
import pl.kiosel.villages.manager.*;
import pl.kiosel.villages.manager.InviteManager;
import pl.kiosel.villages.addons.scoreboard.ScoreboardHandler;
import pl.kiosel.villages.addons.scoreboard.ScoreboardManager;
import pl.kiosel.villages.manager.TeleportManager;
import pl.kiosel.villages.settings.BlacklistHandler;
import pl.kiosel.villages.settings.Settings;
import pl.kiosel.villages.storage.DataHelper;
import pl.kiosel.villages.storage.migrations._1_InitialMigration;
import pl.kiosel.villages.manager.placeholder.PlaceholderManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AdvancedVillages extends MetaPlugin {

	private final Config levelsFile = new Config(this, "levels.yml");

    @Getter private static AdvancedVillages instance;
    @Getter private Economy eco;
    @Getter private VillageAPI api;
	@Getter private DatabaseUserManager databaseUserManager;
    @Getter private VillageManager villageManager;
	@Getter private PermissionManager permissionManager;
	@Getter private VillageRemoveManager villageRemoveManager;
    @Getter private VillageDataManager villageDataManager;
    @Getter private InviteManager inviteManager;
    @Getter private UpgradeManager upgradeManager;
	@Getter private TeleportManager teleportManager;
	@Getter private ScoreboardManager scoreboardManager;
	@Getter private DataHelper dataHelper;
	@Getter private LevelManager levelManager;
	@Getter private CraftingManager craftingManager;

	@Getter private Configuration guiConfig;
	@Getter private Configuration commandConfig;

	@Getter private VillageGUIManager villageGui;
	@Getter private GuiManager guiManager = new GuiManager(this);

	@Getter private boolean worldedit;
	@Getter private PlaceholderManager placeholder;

	@Getter private CommandConfig commandLang;
    private GuiConfig g_config;

	@Getter private BlacklistHandler blacklistHandler;
	@Getter private ScoreboardHandler scoreboardHandler;

	@Getter private TablistConfiguration tablistConfig;
	@Getter private TablistPlaceholdersService tablistPlaceholdersService;
	@Getter private RankPlaceholdersService rankPlaceholdersService;

	@Override
	public void onPluginLoad() {
		instance = this;
		setDev(true);
		getDebug().setup();
	}

	@Override
	public void onPluginEnable() {
		MetaCore.registerPlugin(this, 1, XMaterial.NOTE_BLOCK);

		getDebug().debug("Registering Economy...");
		EconomyManager.load();

		Settings.setupConfig();
		setLocale(Settings.LANGUAGE_MODE.getString(), false);
		EconomyManager.getManager().setPreferredHook(Settings.ECONOMY_PLUGIN.getString());

		registerConfig();

		if (!isDev()) {
			getDebug().debug("Checking license");
			if (!(new Licenses(Settings.LICENSE.getString(), this).register())) return;
		}
		if (!NBT.preloadApi()) {
			getDebug().debug("NBT-API wasn't initialized properly, disabling the plugin");
			getPluginLoader().disablePlugin(this);
			return;
		}
		getDebug().debug("Enabling Plugin");

		this.blacklistHandler = new BlacklistHandler(this);
		this.scoreboardHandler = new ScoreboardHandler(this);

		getDebug().debug("Set Village api");
		this.api = new VillageAPI(this);

		initDatabase(new _1_InitialMigration());

		if(getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			this.placeholder = new PlaceholderManager(this);
			this.placeholder.register();
		}
		if(getServer().getPluginManager().isPluginEnabled("WorldEdit") && Settings.WORLDEDIT.getBoolean()) {
			this.worldedit = true;
		}

		if (this.worldedit)
			saveSchematics();

		this.databaseUserManager = new DatabaseUserManager(this);
		this.inviteManager = new InviteManager(this);
		this.villageDataManager = new VillageDataManager(this);
		this.villageManager = new VillageManager(this);
		this.villageRemoveManager = new VillageRemoveManager(this);
		this.upgradeManager = new UpgradeManager(this);
		this.teleportManager = new TeleportManager(this);
		this.scoreboardManager = new ScoreboardManager(this);
		this.permissionManager = new PermissionManager(this);
		this.villageGui = new VillageGUIManager(this);

		this.tablistConfig = new TablistConfiguration();
		registerPlaceholders();
		loadLevels();

		this.craftingManager = new CraftingManager(this);
		craftingManager.createRecipe();
		craftingManager.registerRecipe();

		registerCommands(
				new CommandVillage(this),
				isDev() ? new CommandTest(this) : null
		);
		new CommandManager(this).addCommand(new CommandCrafting(this, this.guiManager));

		registerListeners(
				new BlockListener(this),
				new ConnectionListener(this),
				new InventoryListener(this),
				new InteractListener(this),
				new MoveListener(this),
				new InteractBlockListeners(this),
				new PlayerListeners(this),
				new VillageListener(this),
				new ChatListener(this),
				new BlockItemListener(this)
		);
		print("Villages enabled");
		getDebug().debug("Plugin enabled");

		getServer().getScheduler().runTaskTimerAsynchronously(this, new VillageSaveTask(this), 20*20, 20*20);
		getServer().getScheduler().runTaskTimerAsynchronously(this, new TablistBroadcastHandler(this), 20L, this.tablistConfig.updateInterval);
	}

	@Override
	public void onPluginDisable() {
		getDebug().debug("Disabling Plugin");
		try {
			craftingManager.unRegisterRecipe();
			villageDataManager.onDisable();
			dataManager.shutdown();

			if (isPlaceholder())
				placeholder.unregister();
			getDebug().close();

			HandlerList.unregisterAll(this);
			getServer().getScheduler().cancelTasks(this);
			print("Villages disabled");
		} catch (Exception e) {
			print("Villages disabled with error " + e.getMessage());
		}
	}

	@Override
	public void onDataLoad() {
		dataHelper = new DataHelper(this);
		dataHelper.loadData(getDataManager());
	}

	@Override
	public void onConfigReload() {
		getDebug().debug("Reloading Plugin..");
		this.setLocale(getConfig().getString("settings.language"), true);
		this.locale.reloadMessages();
		this.g_config.setConfig();
		this.blacklistHandler.reload();
		this.scoreboardHandler.reload();
		loadLevels();
	}

	@Override
	public List<Config> getExtraConfig() {
		return Collections.singletonList(this.levelsFile);
	}

	private void registerPlaceholders() {
		BasicPlaceholdersService basicPlaceholdersService = new BasicPlaceholdersService();
		basicPlaceholdersService.register(this, "simple", BasicPlaceholdersService.createSimplePlaceholders(this));

		TimePlaceholdersService timePlaceholdersService = new TimePlaceholdersService();
		timePlaceholdersService.register(this, "time", TimePlaceholdersService.createTimePlaceholders());

		PlayerPlaceholdersService playerPlaceholdersService = new PlayerPlaceholdersService();
		playerPlaceholdersService.register(this, "player", PlayerPlaceholdersService.createPlayerPlaceholders());
		playerPlaceholdersService.register(this, "world", PlayerPlaceholdersService.createPlayerWorldPlaceholders());

		rankPlaceholdersService = new RankPlaceholdersService(this, tablistConfig);

		this.tablistPlaceholdersService = new TablistPlaceholdersService(
				basicPlaceholdersService,
				timePlaceholdersService,
				playerPlaceholdersService
		);
	}

	private void loadLevels() {
		if (!this.levelsFile.getFile().exists()) {
			this.saveResource("levels.yml", false);
		}
		this.levelsFile.load();

		// Load a plugin of LevelManager
		this.levelManager = new LevelManager();
		/*
		 * Register Levels into LevelManager from configuration.
		 */
		this.levelManager.clear();
		for (String levelName : this.levelsFile.getKeys(false)) {
			ConfigurationSection levels = this.levelsFile.getConfigurationSection(levelName);

			int level = Integer.parseInt(levelName.split("-")[1]);
			int costExperience = levels.getInt("Cost-xp");
			int costEconomy = levels.getInt("Cost-eco");
			int size = levels.getInt("Size");

			Map<XMaterial, Integer> materials = new LinkedHashMap<>();
			if (levels.contains("Cost-item"))
				for (String materialStr : levels.getStringList("Cost-item")) {
					String[] materialSplit = materialStr.split(":");
					materials.put(CompatibleMaterial.getMaterial(materialSplit[0]).get(), Integer.parseInt(materialSplit[1]));
				}
			this.levelManager.addLevel(level, costExperience, costEconomy, size, materials);
		}
	}

    private void registerCommands(Command... commands) {
		getDebug().debug("Registering commands...");
        for(Command cmd : commands)
			if (cmd != null)
            	ReflectionUtils.registerCommand("village", cmd);
    }

    private void registerListeners(Listener... listeners) {
		getDebug().debug("Registering listeners...");
        for (Listener listener : listeners)
            getServer().getPluginManager().registerEvents(listener, this);
    }

    private void registerConfig() {
		getDebug().debug("Registering configs");
		commandConfig = new Configuration(this, "command.yml");
		guiConfig = new Configuration(this, "guis.yml");

        guiConfig.saveDefaultConfig();
		commandConfig.saveDefaultConfig();

		commandLang = new CommandConfig(this);
        g_config = new GuiConfig(this);

		getDebug().debug("Setting configs");
		commandLang.setConfig();
        g_config.setConfig();
    }

	private void saveSchematics() {
		File schemFolder = new File(getDataFolder(), "schematics");
		if (!schemFolder.exists())
			if (schemFolder.mkdirs())
				getDebug().debug("Created folder: " + schemFolder.getName());

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
						getDebug().debug("Not found schematic in resources: " + fileName);
						continue;
					}
					Files.copy(in, outFile.toPath());
					getDebug().debug("Saved schematic: " + fileName);
				} catch (IOException e) {
					getDebug().debug("Fail while saving schematic: " + fileName);
					getDebug().debug(e);
				}
			}
		}
	}

	public boolean isPlaceholder() {
		return placeholder != null;
	}

	private void print(String s) {
		getServer().getConsoleSender().sendMessage("[Villages] " + s);
	}
}