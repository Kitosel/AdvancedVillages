package pl.kiosel.villages;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import pl.kiosel.rosacore.RosaPlugin;
import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.rosacore.database.DatabaseManager;
import pl.kiosel.rosacore.dependencies.nbtapi.NBT;
import pl.kiosel.rosacore.gui.GuiManager;
import pl.kiosel.rosacore.hook.economy.EconomyHook;
import pl.kiosel.rosacore.utils.ReflectionUtils;
import pl.kiosel.rosacore.version.Version;
import pl.kiosel.villages.addons.antylogout.CombatConfig;
import pl.kiosel.villages.addons.antylogout.CombatManager;
import pl.kiosel.villages.addons.antylogout.listener.CombatListener;
import pl.kiosel.villages.addons.antylogout.listener.CombatRegionListener;
import pl.kiosel.villages.addons.buildeditor.BuildEditorListener;
import pl.kiosel.villages.addons.buildeditor.VillageBuildEditorManager;
import pl.kiosel.villages.addons.development.DevelopmentConfiguration;
import pl.kiosel.villages.addons.development.VillageDevelopmentManager;
import pl.kiosel.villages.addons.diplomacy.DiplomacyConfiguration;
import pl.kiosel.villages.addons.diplomacy.DiplomacyListener;
import pl.kiosel.villages.addons.diplomacy.DiplomacyManager;
import pl.kiosel.villages.addons.logs.VillageLogConfiguration;
import pl.kiosel.villages.addons.logs.VillageLogManager;
import pl.kiosel.villages.addons.quests.QuestConfiguration;
import pl.kiosel.villages.addons.quests.QuestListener;
import pl.kiosel.villages.addons.quests.VillageQuestManager;
import pl.kiosel.villages.addons.ranking.RankingConfiguration;
import pl.kiosel.villages.addons.ranking.RankingListener;
import pl.kiosel.villages.addons.ranking.RankingManager;
import pl.kiosel.villages.addons.rent.RentConfiguration;
import pl.kiosel.villages.addons.rent.VillageUpkeepManager;
import pl.kiosel.villages.addons.scoreboard.ScoreboardHandler;
import pl.kiosel.villages.addons.scoreboard.ScoreboardManager;
import pl.kiosel.villages.addons.tablist.TablistConfiguration;
import pl.kiosel.villages.addons.tablist.TablistManager;
import pl.kiosel.villages.addons.tablist.TablistPlaceholdersService;
import pl.kiosel.villages.addons.trials.VillageAnimationManager;
import pl.kiosel.villages.api.VillageAPI;
import pl.kiosel.villages.commands.CommandCrafting;
import pl.kiosel.villages.commands.CommandTest;
import pl.kiosel.villages.commands.ConfiguredCommandRegistry;
import pl.kiosel.villages.config.*;
import pl.kiosel.villages.data.rank.DefaultTops;
import pl.kiosel.villages.data.rank.RankPlaceholdersService;
import pl.kiosel.villages.data.user.UserManager;
import pl.kiosel.villages.data.user.UserRankManager;
import pl.kiosel.villages.data.village.VillageManager;
import pl.kiosel.villages.data.village.VillageRankManager;
import pl.kiosel.villages.data.village.handler.BlockItemListener;
import pl.kiosel.villages.data.village.handler.InteractBlockListeners;
import pl.kiosel.villages.data.village.handler.PlayerListeners;
import pl.kiosel.villages.data.village.handler.TntPrimeListener;
import pl.kiosel.villages.data.village.handler.VillageListener;
import pl.kiosel.villages.data.village.level.LevelManager;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.integrations.AdvancedPlayerListIntegration;
import pl.kiosel.villages.integrations.PlayerListProfileIntegration;
import pl.kiosel.villages.listeners.*;
import pl.kiosel.villages.manager.*;
import pl.kiosel.villages.manager.placeholder.PlaceholderManager;
import pl.kiosel.villages.manager.teleport.TeleportManager;
import pl.kiosel.villages.storage.DataHelper;
import pl.kiosel.villages.storage.Dataloader;
import pl.kiosel.villages.storage.VillageDataManager;
import pl.kiosel.villages.storage.VillageDatabaseSettings;
import pl.kiosel.villages.storage.migrations._1_InitialMigration;
import pl.kiosel.villages.storage.migrations._2_DiplomacyMigration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;

public final class AdvancedVillages extends RosaPlugin {

	@Getter private final VillageConfigManager configurationManager = new VillageConfigManager(this);
	@Getter private final VillageMessages villageMessages = new VillageMessages(this);
	@Getter private final RosaConfig coreConfig = this.configurationManager.get(VillageConfigFile.CONFIG);
	@Getter private final RosaConfig databaseConfig = this.configurationManager.get(VillageConfigFile.DATABASE);
	@Getter private final RosaConfig levelsFile = this.configurationManager.get(VillageConfigFile.LEVELS);
	@Getter private final RosaConfig villageFile = this.configurationManager.get(VillageConfigFile.VILLAGE);
	@Getter private final RosaConfig buildEditorFile = this.configurationManager.get(VillageConfigFile.BUILD_EDITOR);
	@Getter private final RosaConfig animationFile = this.configurationManager.get(VillageConfigFile.ANIMATIONS);
	@Getter private final RosaConfig commandFile = this.configurationManager.get(VillageConfigFile.COMMANDS);
	@Getter private final RosaConfig spawnFile = this.configurationManager.get(VillageConfigFile.SPAWN);
	@Getter private final RosaConfig combatFile = this.configurationManager.get(VillageConfigFile.COMBAT);
	@Getter private final RosaConfig scoreboardFile = this.configurationManager.get(VillageConfigFile.SCOREBOARD);
	@Getter private final RosaConfig guiConfig = this.configurationManager.get(VillageConfigFile.GUIS);
	@Getter private final RosaConfig tablistFile = this.configurationManager.get(VillageConfigFile.TABLIST);
	@Getter private final RosaConfig questFile = this.configurationManager.get(VillageConfigFile.QUESTS);
	@Getter private final RosaConfig logFile = this.configurationManager.get(VillageConfigFile.LOGS);
	@Getter private final RosaConfig developmentFile = this.configurationManager.get(VillageConfigFile.DEVELOPMENT);

	@Getter	private EconomyHook economy;

    @Getter private static AdvancedVillages instance;
    @Getter private VillageAPI api;
    @Getter private VillageUtils villageUtils;
	@Getter private RoleManager roleManager;
    @Getter private InviteManager inviteManager;
    @Getter private UpgradeManager upgradeManager;
	@Getter private TeleportManager teleportManager;
	@Getter private ScoreboardManager scoreboardManager;
	@Getter private DataHelper dataHelper;
	@Getter private LevelManager levelManager;
	@Getter private CraftingManager craftingManager;
	@Getter private VillageRemoveManager villageRemoveManager;
	@Getter private VillageAnimationManager villageAnimationManager;
	@Getter private VillageBuildEditorManager villageBuildEditorManager;

	@Getter private CommandConfig commandLang;
	@Getter private GuiConfig guiSettings;

	@Getter private VillageGUIManager villageGui;
	@Getter private GuiManager guiManager = new GuiManager(this);

	@Getter private boolean worldedit;
	@Getter private PlaceholderManager placeholder;

	@Getter private ScoreboardHandler scoreboardHandler;

	@Getter private TablistConfiguration tablistConfig;
	@Getter private TablistPlaceholdersService tablistPlaceholdersService;
	@Getter private TablistManager tablistManager;
	@Getter private PlayerListProfileIntegration advancedPlayerListIntegration;
	@Getter private RankPlaceholdersService rankPlaceholdersService;

	@Getter private UserManager userManager;
	@Getter private VillageManager villageManager;
	@Getter private VillageRankManager villageRankManager;
	@Getter private UserRankManager userRankManager;

	@Getter private Dataloader dataloader;
	@Getter private VillageDataManager dataManager;
	private VillageDataTaskHandler villageDataTaskHandler;

	@Getter private CombatManager combatManager;
	@Getter private CombatConfig combatConfig;
	@Getter private QuestConfiguration questConfig;
	@Getter private VillageQuestManager questManager;
	@Getter private VillageLogConfiguration logConfig;
	@Getter private VillageLogManager logManager;
	@Getter private RankingConfiguration rankingConfig;
	@Getter private RankingManager rankingManager;
	@Getter private DiplomacyConfiguration diplomacyConfig;
	@Getter private DiplomacyManager diplomacyManager;
	@Getter private DevelopmentConfiguration developmentConfig;
	@Getter private VillageDevelopmentManager developmentManager;
	@Getter private RentConfiguration upkeepConfig;
	@Getter private VillageUpkeepManager upkeepManager;
	@Getter private volatile boolean dataReady;
	private boolean runtimeHandlersRegistered;
	private ConfiguredCommandRegistry configuredCommandRegistry;

	@Override
	public void onPluginLoad() {
		if (Version.isServerVersionBelow(Version.V1_17)) {
			getRosaLogger().warning("--------------------------------------");
			getRosaLogger().warning(" ");
			getRosaLogger().warning("AdvancedVillages supporting only");
			getRosaLogger().warning("Minecraft version above 1.17");
			getRosaLogger().warning("Plugin is disabling now...");
			getRosaLogger().warning(" ");
			getRosaLogger().warning("--------------------------------------");
			emergencyStop();
		}
		instance = this;
		setDev(false);
	}

	@Override
	public void onPluginEnable() {
		getDebug().debug("Setup main config");
		Settings.setupConfig(this);

		getDebug().debug("Registering Economy...");
		if (!getHookManager().getEconomy().setPreferredHook(Settings.ECONOMY_PLUGIN.getString()))
			getRosaLogger().warning("Economy '" + Settings.ECONOMY_PLUGIN.getString() + "' is unavailable. Selecting automatically.");

		this.economy = getHookManager().getEconomy().getActiveHook()
				.orElseThrow(() -> new IllegalStateException("No supported economy plugin found"));
		this.villageMessages.reload(Settings.LANGUAGE_MODE.getString(), true);

		if (!this.configurationManager.loadAll())
			getRosaLogger().warning("One or more configuration files could not be loaded; safe fallbacks will be used");

		this.registerConfig();
		if (!NBT.preloadApi()) {
			getDebug().debug("NBT-API wasn't initialized properly, disabling the plugin");
			emergencyStop();
			return;
		}
		getDebug().debug("Enabling Plugin");

		this.levelManager = new LevelManager(this);
		if (!this.levelManager.loadLevels()) {
			getRosaLogger().severe("No valid village level configuration is available. Disabling the plugin.");
			emergencyStop();
			return;
		}
		this.scoreboardHandler = new ScoreboardHandler(this);

		this.userManager = new UserManager(this);
		this.villageManager = new VillageManager();
		this.villageRankManager = new VillageRankManager();
		this.villageRankManager.register(DefaultTops.defaultVillageTops(this.villageManager));
		this.userRankManager = new UserRankManager();
		this.userRankManager.register(DefaultTops.defaultUserTops(this.userManager));

		getDebug().debug("Set Village api");
		this.api = new VillageAPI(this);

		if (!this.databaseConfig.load().isSuccess())
			throw new IllegalStateException("Could not load database.yml");
		DatabaseManager database = createDatabase(
				VillageDatabaseSettings.read(this, this.databaseConfig),
				getName().toLowerCase() + '_',
				new _1_InitialMigration(),
				new _2_DiplomacyMigration());
		this.dataManager = new VillageDataManager(database);
		this.questConfig = new QuestConfiguration(this);
		this.questManager = new VillageQuestManager(this, this.questConfig);
		this.logConfig = new VillageLogConfiguration(this);
		this.logManager = new VillageLogManager(this, this.logConfig);
		this.rankingConfig = new RankingConfiguration(this);
		this.rankingManager = new RankingManager(this, this.rankingConfig);
		this.diplomacyConfig = new DiplomacyConfiguration(this);
		this.diplomacyManager = new DiplomacyManager(this, this.diplomacyConfig);
		this.developmentConfig = new DevelopmentConfiguration(this);
		this.developmentManager = new VillageDevelopmentManager(this, this.developmentConfig);
		this.upkeepConfig = new RentConfiguration(this);
		this.upkeepManager = new VillageUpkeepManager(this, this.upkeepConfig);

		if(getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			getDebug().debug("Hooked PlaceholderApi");
			this.placeholder = new PlaceholderManager(this);
			this.placeholder.register();
		}
		this.updateWorldEditState();

		if (this.worldedit)
			saveSchematics();

		this.inviteManager = new InviteManager(this);
		this.villageUtils = new VillageUtils(this);
		this.villageRemoveManager = new VillageRemoveManager(this);
		this.upgradeManager = new UpgradeManager(this);
		this.villageAnimationManager = new VillageAnimationManager(this, this.animationFile);
		this.teleportManager = new TeleportManager(this);
		this.roleManager = new RoleManager(this);
		this.villageGui = new VillageGUIManager(this);

		this.scoreboardManager = new ScoreboardManager(this);
		this.scoreboardManager.reload();

		this.tablistConfig = new TablistConfiguration(this);
		this.registerPlaceholders();
		if (getServer().getPluginManager().isPluginEnabled("AdvancedPlayerList")) {
			try {
				this.advancedPlayerListIntegration = new AdvancedPlayerListIntegration(this);
				this.advancedPlayerListIntegration.enable();
				getRosaLogger().info("AdvancedPlayerList integration enabled");
			} catch (RuntimeException exception) {
				this.advancedPlayerListIntegration = null;
				getRosaLogger().log(Level.WARNING, "Could not enable AdvancedPlayerList integration", exception);
			}
		}
		this.tablistManager = new TablistManager(
				this,
				this.tablistConfig,
				this.tablistPlaceholdersService,
				this.getTabLists()
		);

		this.villageAnimationManager.reload();
		if (getHookManager().getWorldEdit().isEnabled())
			this.villageBuildEditorManager = new VillageBuildEditorManager(this, this.buildEditorFile);

		this.craftingManager = new CraftingManager(this);
		this.craftingManager.createRecipe();
		this.craftingManager.registerRecipe();

		this.combatConfig = new CombatConfig(this);
		this.combatManager = new CombatManager(this, this.combatConfig);

		getDebug().debug("Plugin initialized; waiting for data");
	}

	@Override
	public void onPluginDisable() {
		getDebug().debug("Disabling Plugin");

		runShutdownStep("canceling pending teleports", () -> {
			if (this.teleportManager != null)
				this.teleportManager.shutdown();
		});
		runShutdownStep("stopping scheduled tasks", () -> {
			if (this.tablistManager != null)
				this.tablistManager.shutdown();

			if (this.combatManager != null)
				this.combatManager.shutdown();

			if (this.rankingManager != null)
				this.rankingManager.shutdown();

			if (this.diplomacyManager != null)
				this.diplomacyManager.shutdown();

			if (this.upkeepManager != null)
				this.upkeepManager.shutdown();

			if (this.villageBuildEditorManager != null)
				this.villageBuildEditorManager.shutdown();

			if (this.villageAnimationManager != null)
				this.villageAnimationManager.shutdown();

			if (this.villageDataTaskHandler != null)
				this.villageDataTaskHandler.shutdown();

			getServer().getScheduler().cancelTasks(this);
		});
		runShutdownStep("clearing runtime views", () -> {
			if (this.scoreboardManager != null)
				this.scoreboardManager.clearBoards();
		});
		runShutdownStep("unregistering recipes", () -> {
			if (this.craftingManager != null)
				this.craftingManager.unRegisterRecipe();
		});
		runShutdownStep("saving data", () -> {
			if (this.dataReady && this.dataloader != null)
				this.dataloader.save(false);
		});
		runShutdownStep("clearing villages", () -> {
			if (this.villageManager != null)
				this.villageManager.onDisable();
		});
		runShutdownStep("unregistering PlaceholderAPI", () -> {
			if (isPlaceholder())
				this.placeholder.unregister();
		});
		runShutdownStep("unregistering AdvancedPlayerList integration", () -> {
			if (this.advancedPlayerListIntegration != null)
				this.advancedPlayerListIntegration.disable();
		});
		runShutdownStep("unregistering configured commands", () -> {
			if (this.configuredCommandRegistry != null)
				this.configuredCommandRegistry.unregister();
		});
		runShutdownStep("unregistering listeners", () -> HandlerList.unregisterAll((Plugin) this));
		this.dataReady = false;
		this.runtimeHandlersRegistered = false;
		this.worldedit = false;
		this.advancedPlayerListIntegration = null;
		instance = null;
	}

	@Override
	public synchronized void onDataLoad() {
		if (!Bukkit.isPrimaryThread()) {
			getServer().getScheduler().runTask(this, this::onDataLoad);
			return;
		}
		if (this.dataReady) {
			getRosaLogger().warning("Ignoring duplicate data-load callback");
			return;
		}

		getDebug().debug("Loading data");
		try {
			this.dataHelper = new DataHelper(this);

			this.dataloader = new Dataloader(this);
			this.dataloader.load();
			this.dataReady = true;

			this.registerRuntimeHandlers();
			this.synchronizeOnlineUsers();
			this.villageDataTaskHandler = new VillageDataTaskHandler(this);
			this.villageDataTaskHandler.startHandler();
			this.villageAnimationManager.start();
			this.diplomacyManager.start();
			this.upkeepManager.start();
			this.reloadAddons();
			getDebug().debug("Plugin fully loaded");
			diagnostics();
		} catch (RuntimeException exception) {
			getRosaLogger().log(Level.SEVERE, "Could not finish loading village data", exception);
			emergencyStop();
		}
	}

	@Override
	public void onConfigReload() {
		getDebug().debug("Reloading Plugin..");
		if (!this.configurationManager.loadAll())
			getRosaLogger().warning("One or more configuration files were invalid and kept their previous values");
		this.villageMessages.reload(Settings.LANGUAGE_MODE.getString(), true);

		this.villageAnimationManager.reload();
		this.commandLang.reload();
		if (this.configuredCommandRegistry != null)
			this.configuredCommandRegistry.reload();

		this.guiSettings.reload();
		if (this.roleManager != null)
			this.roleManager.reload();
		this.scoreboardManager.reload();
		this.tablistConfig.reload();
		this.teleportManager.reload();
		if (!Settings.ADDONS_SPAWN_ENABLE.getBoolean())
			this.teleportManager.cancelTeleports(TeleportManager.TeleportType.SPAWN, true);

		this.levelManager.loadLevels();
		this.updateWorldEditState();
		if (this.upgradeManager != null)
			this.upgradeManager.refreshWorldEditIntegration();

		if (this.worldedit)
			this.saveSchematics();

		if (this.villageBuildEditorManager != null)
			this.villageBuildEditorManager.reload();

		this.reloadAddons();
		if (this.villageDataTaskHandler != null)
			this.villageDataTaskHandler.reload();
		diagnostics();
	}

	@Override
	public void reloadConfig() {
		if (this.configurationManager.reloadMainConfig())
			this.onConfigReload();
	}

	private void registerRuntimeHandlers() {
		if (this.runtimeHandlersRegistered)
			return;

		this.configuredCommandRegistry = new ConfiguredCommandRegistry(this);
		this.configuredCommandRegistry.register();
		registerCommands("test",
				new CommandCrafting(this),
				isDev() ? new CommandTest(this) : null
		);
		ReflectionUtils.syncCommands();
		Bukkit.getOnlinePlayers().forEach(Player::updateCommands);

		registerListeners(
				new JoinListener(this),
				new QuitListener(this),
				new BlockListener(this),
				new InteractListener(this),
				new MoveListener(this),
				new ChatListener(this),
				new InteractBlockListeners(this),
				createTntPrimeListener(),
				new PlayerListeners(this),
				new VillageListener(this),
				new BlockItemListener(this),
				new QuestListener(this),
				new RankingListener(this),
				new DiplomacyListener(this),
				this.villageBuildEditorManager == null ? null : new BuildEditorListener(this),
				new CombatListener(this),
				getHookManager().getWorldEdit().isEnabled() ? new CombatRegionListener(this) : null
		);
		this.runtimeHandlersRegistered = true;
	}

	private TntPrimeListener createTntPrimeListener() {
		try {
			Class.forName("org.bukkit.event.block.TNTPrimeEvent", false, getClass().getClassLoader());
			return new TntPrimeListener(this);
		} catch (ClassNotFoundException | LinkageError ignored) {
			return null;
		}
	}

	private void synchronizeOnlineUsers() {
		for (Player player : this.getServer().getOnlinePlayers())
			this.userManager.getOrCreate(player);
	}

	private void updateWorldEditState() {
		boolean available = getHookManager().getWorldEdit().isEnabled()
				&& Settings.WORLDEDIT.getBoolean();
		if (available != this.worldedit)
			getRosaLogger().info("WorldEdit village builds " + (available ? "enabled" : "disabled"));
		this.worldedit = available;
	}

	private void registerPlaceholders() {
		getDebug().debug("Registering tablist placeholders");
		this.rankPlaceholdersService = new RankPlaceholdersService(this.tablistConfig, this.userRankManager, this.villageRankManager);
		this.tablistPlaceholdersService = new TablistPlaceholdersService(this, this.rankPlaceholdersService);
	}

	private void reloadAddons() {
		this.scoreboardManager.synchronizeBoards();
		if (this.tablistManager != null)
			this.tablistManager.reload();

		if (this.combatManager != null)
			this.combatManager.reload();

		if (this.questManager != null)
			this.questManager.reload();

		if (this.logManager != null)
			this.logManager.reload();

		if (this.rankingManager != null)
			this.rankingManager.reload();

		if (this.diplomacyManager != null)
			this.diplomacyManager.reload();

		if (this.developmentManager != null)
			this.developmentManager.reload();

		if (this.upkeepManager != null)
			this.upkeepManager.reload();
	}

    private void registerConfig() {
		getDebug().debug("Registering configs");
		commandLang = new CommandConfig(this);
		guiSettings = new GuiConfig(this);

		getDebug().debug("Setting configs");
		commandLang.setConfig();
    }

	private void diagnostics() {
		if (!isDev()) return;
		getDebug().debug("Diagnostics: ");
		try {
			getDebug().debug("Database: " + dataManager.getDatabase().getConnection());
		} catch (SQLException e) {
			getDebug().error("Database connection error");
		}
		File schemaFolder = new File(getDataFolder(), "schematics");
		if (!schemaFolder.exists() || !schemaFolder.isDirectory()) {
			getDebug().error("Schematic folder does not exist");
		} else {
			if (schemaFolder.listFiles() != null) {
				for (File file : Objects.requireNonNull(schemaFolder.listFiles())) {
					getDebug().debug("Schematic File: " + file.getName());
				}
			}
		}
		getDebug().debug("Worldedit integration: " + worldedit);
		getDebug().debug("PlaceholderApi integration: " + isPlaceholder());
		getDebug().debug("Economy integration: " + getHookManager().getEconomy().getActiveName());
		getDebug().debug("Lang: " + getLocale().getLocale());
		getDebug().debug("Addons: ");
		getDebug().debug("AntiLogout: " + Settings.ADDONS_ANTYLOGOUT_ENABLE.getBoolean());
		getDebug().debug("Scoreboard: " + Settings.ADDONS_SCOREBOARD_ENABLE.getBoolean());
		getDebug().debug("Tablist: " + Settings.ADDONS_TABLIST_ENABLE.getBoolean());
		getDebug().debug("Spawn: " + Settings.ADDONS_SPAWN_ENABLE.getBoolean());
		getDebug().debug("Quests: " + Settings.ADDONS_QUESTS_ENABLE.getBoolean());
		getDebug().debug("Diplomacy: " + this.diplomacyConfig.snapshot().isEnabled());
		getDebug().debug("Development: " + Settings.ADDONS_DEVELOPMENT_ENABLE.getBoolean());
		getDebug().debug("Upkeep: " + this.upkeepConfig.snapshot().isEnabled());
		getDebug().debug("Village-animations: " + Settings.ADDONS_VILLAGE_ANIMATIONS_ENABLE.getBoolean());
	}

	private void saveSchematics() {
		getDebug().debug("Saving schematics");
		File schemaFolder = new File(getDataFolder(), "schematics");
		if (!schemaFolder.exists())
			if (schemaFolder.mkdirs())
				getDebug().debug("Created folder: " + schemaFolder.getName());

		String[] schematics = new String[]{
				"Turret1.schem",
				"Turret2.schem",
				"Turret3.schem",
				"Turret4.schem",
				"Turret5.schem"
		};

		for (String fileName : schematics) {
			File outFile = new File(schemaFolder, fileName);
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
}
