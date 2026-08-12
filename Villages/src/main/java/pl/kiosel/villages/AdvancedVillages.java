package pl.kiosel.villages;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import pl.kiosel.core.MetaCore;
import pl.kiosel.core.MetaPlugin;
import pl.kiosel.core.commands.CommandManager;
import pl.kiosel.core.compatibility.CompatibleMaterial;
import pl.kiosel.core.configuration.Config;
import pl.kiosel.core.dependencies.de.tr7zw.nbtapi.NBT;
import pl.kiosel.core.gui.GuiManager;
import pl.kiosel.core.hooks.WorldEditHook;
import pl.kiosel.core.hooks.WorldGuardHook;
import pl.kiosel.core.hooks.economy.EconomyHook;
import pl.kiosel.core.input.Licenses;
import pl.kiosel.core.locale.Locale;
import pl.kiosel.core.nms.Nms;
import pl.kiosel.dependencies.com.cryptomorin.xseries.XMaterial;
import pl.kiosel.villages.addons.antylogout.CombatConfig;
import pl.kiosel.villages.addons.antylogout.CombatManager;
import pl.kiosel.villages.addons.antylogout.listener.CombatListener;
import pl.kiosel.villages.addons.antylogout.listener.CombatRegionListener;
import pl.kiosel.villages.addons.buildeditor.BuildEditorListener;
import pl.kiosel.villages.addons.buildeditor.VillageBuildEditorManager;
import pl.kiosel.villages.addons.logs.VillageLogConfiguration;
import pl.kiosel.villages.addons.logs.VillageLogManager;
import pl.kiosel.villages.addons.placeholder.PlaceholderManager;
import pl.kiosel.villages.addons.quests.QuestConfiguration;
import pl.kiosel.villages.addons.quests.QuestListener;
import pl.kiosel.villages.addons.quests.VillageQuestManager;
import pl.kiosel.villages.addons.ranking.RankingConfiguration;
import pl.kiosel.villages.addons.ranking.RankingListener;
import pl.kiosel.villages.addons.ranking.RankingManager;
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
import pl.kiosel.villages.data.village.handler.VillageListener;
import pl.kiosel.villages.data.village.level.LevelManager;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.listeners.*;
import pl.kiosel.villages.manager.*;
import pl.kiosel.villages.manager.teleport.TeleportManager;
import pl.kiosel.villages.settings.Settings;
import pl.kiosel.villages.storage.DataHelper;
import pl.kiosel.villages.storage.Dataloader;
import pl.kiosel.villages.storage.migrations._1_InitialMigration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class AdvancedVillages extends MetaPlugin {

	@Getter private final VillageConfigManager configurationManager = new VillageConfigManager(this);
	@Getter private final VillageMessages messages = new VillageMessages(this);
	@Getter private final Config levelsFile = this.configurationManager.get(VillageConfigFile.LEVELS);
	@Getter private final Config buildEditorFile = this.configurationManager.get(VillageConfigFile.BUILD_EDITOR);
	@Getter private final Config animationFile = this.configurationManager.get(VillageConfigFile.ANIMATIONS);
	@Getter private final Config commandFile = this.configurationManager.get(VillageConfigFile.COMMANDS);
	@Getter private final Config spawnFile = this.configurationManager.get(VillageConfigFile.SPAWN);
	@Getter private final Config combatFile = this.configurationManager.get(VillageConfigFile.COMBAT);
	@Getter private final Config scoreboardFile = this.configurationManager.get(VillageConfigFile.SCOREBOARD);
	@Getter private final Config guiConfig = this.configurationManager.get(VillageConfigFile.GUIS);
	@Getter private final Config tablistFile = this.configurationManager.get(VillageConfigFile.TABLIST);
	@Getter private final Config questFile = this.configurationManager.get(VillageConfigFile.QUESTS);
	@Getter private final Config logFile = this.configurationManager.get(VillageConfigFile.LOGS);
	@Getter private final Config rankingFile = this.configurationManager.get(VillageConfigFile.RANKING);

	@Getter	private EconomyHook economy;

    @Getter private static AdvancedVillages instance;
    @Getter private VillageAPI api;
    @Getter private VillageUtilsManager villageUtilsManager;
	@Getter private PermissionManager permissionManager;
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
	@Getter private RankPlaceholdersService rankPlaceholdersService;

	@Getter private UserManager userManager;
	@Getter private VillageManager villageManager;
	@Getter private VillageRankManager villageRankManager;
	@Getter private UserRankManager userRankManager;

	@Getter private Dataloader dataloader;
	private VillageDataTaskHandler villageDataTaskHandler;

	@Getter private CombatManager combatManager;
	@Getter private CombatConfig combatConfig;
	@Getter private QuestConfiguration questConfig;
	@Getter private VillageQuestManager questManager;
	@Getter private VillageLogConfiguration logConfig;
	@Getter private VillageLogManager logManager;
	@Getter private RankingConfiguration rankingConfig;
	@Getter private RankingManager rankingManager;
	@Getter private volatile boolean dataReady;
	private boolean runtimeHandlersRegistered;
	private ConfiguredCommandRegistry configuredCommandRegistry;

	@Override
	public void onPluginLoad() {
		instance = this;
		setDev(true);
		getDebug().setup();
	}

	@Override
	public void onPluginEnable() {
		MetaCore.registerPlugin(this, 1, XMaterial.NOTE_BLOCK);

		getDebug().debug("Setup main config");
		Settings.setupConfig(this);
		saveBundledLocales();

		getDebug().debug("Registering Economy...");
		if (!getHookManager().getEconomyHookRegistry().setPreferredHook(Settings.ECONOMY_PLUGIN.getString())) {
			getLogger().warning("Economy '" + Settings.ECONOMY_PLUGIN.getString() + "' is unavailable. Selecting automatically.");
		}

		this.economy = getHookManager().getEconomyHookRegistry().getActive().orElseThrow(() -> {
			emergencyStop();
			return new IllegalStateException("No supported economy plugin found");
		});

		this.messages.reload(Settings.LANGUAGE_MODE.getString(), true);

		if (!this.configurationManager.loadAll()) {
			getLogger().warning("One or more configuration files could not be loaded; safe fallbacks will be used");
		}
		this.registerConfig();
		if (!this.loadLevels()) {
			getLogger().severe("No valid village level configuration is available. Disabling the plugin.");
			emergencyStop();
			return;
		}
		if (!NBT.preloadApi()) {
			getDebug().debug("NBT-API wasn't initialized properly, disabling the plugin");
			emergencyStop();
			return;
		}
		getDebug().debug("Enabling Plugin");

		if (!isDev()) {
			getDebug().debug("Checking license");
			if (!(new Licenses(Settings.LICENSE.getString(), this).register())) {
				getLogger().warning("--------------------------------------");
				getLogger().warning(" ");
				getLogger().warning("The license check failed.");
				getLogger().warning("Please check to see if you have");
				getLogger().warning("entered the license in config.yml.");
				getLogger().warning(" ");
				getLogger().warning("--------------------------------------");
				emergencyStop();
				return;
			}
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

		initDatabase(new _1_InitialMigration());
		this.questConfig = new QuestConfiguration(this);
		this.questManager = new VillageQuestManager(this, this.questConfig);
		this.logConfig = new VillageLogConfiguration(this);
		this.logManager = new VillageLogManager(this, this.logConfig);
		this.rankingConfig = new RankingConfiguration(this);
		this.rankingManager = new RankingManager(this, this.rankingConfig);

		if(getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			getDebug().debug("Hooked PlaceholderApi");
			this.placeholder = new PlaceholderManager(this);
			this.placeholder.register();
		}
		this.updateWorldEditState();

		if (this.worldedit)
			saveSchematics();

		this.inviteManager = new InviteManager(this);
		this.villageUtilsManager = new VillageUtilsManager(this);
		this.villageRemoveManager = new VillageRemoveManager(this);
		this.upgradeManager = new UpgradeManager(this);
		this.villageAnimationManager = new VillageAnimationManager(this, this.animationFile);
		this.teleportManager = new TeleportManager(this);
		this.permissionManager = new PermissionManager(this);
		this.villageGui = new VillageGUIManager(this);

		this.scoreboardManager = new ScoreboardManager(this);
		this.scoreboardManager.reloadScoreboard();

		this.tablistConfig = new TablistConfiguration(this);
		this.registerPlaceholders();
		this.tablistManager = new TablistManager(
				this,
				this.tablistConfig,
				this.tablistPlaceholdersService,
				Nms.getImplementations().getPlayerListAccessor()
		);

		this.villageAnimationManager.reload();
		if (WorldEditHook.isEnabled()) {
			this.villageBuildEditorManager = new VillageBuildEditorManager(this, this.buildEditorFile);
		}
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
			if (this.teleportManager != null) {
				this.teleportManager.shutdown();
			}
		});
		runShutdownStep("stopping scheduled tasks", () -> {
			if (this.tablistManager != null) {
				this.tablistManager.shutdown();
			}
			if (this.combatManager != null) {
				this.combatManager.shutdown();
			}
			if (this.rankingManager != null) {
				this.rankingManager.shutdown();
			}
			if (this.villageBuildEditorManager != null) {
				this.villageBuildEditorManager.shutdown();
			}
			if (this.villageAnimationManager != null) {
				this.villageAnimationManager.shutdown();
			}
			if (this.villageDataTaskHandler != null) {
				this.villageDataTaskHandler.stopHandler();
			}
			getServer().getScheduler().cancelTasks(this);
		});
		runShutdownStep("clearing runtime views", () -> {
			if (this.scoreboardManager != null) {
				this.scoreboardManager.clearBoards();
			}
		});
		runShutdownStep("unregistering recipes", () -> {
			if (this.craftingManager != null) {
				this.craftingManager.unRegisterRecipe();
			}
		});
		runShutdownStep("saving data", () -> {
			if (this.dataReady && this.dataloader != null) {
				this.dataloader.save(false);
			}
		});
		runShutdownStep("shutting down the database", () -> {
			if (this.dataManager != null) {
				this.dataManager.shutdown();
			}
		});
		runShutdownStep("clearing villages", () -> {
			if (this.villageManager != null) {
				this.villageManager.onDisable();
			}
		});
		runShutdownStep("unregistering PlaceholderAPI", () -> {
			if (isPlaceholder()) {
				this.placeholder.unregister();
			}
		});
		runShutdownStep("unregistering configured commands", () -> {
			if (this.configuredCommandRegistry != null) {
				this.configuredCommandRegistry.unregister();
			}
		});
		runShutdownStep("unregistering listeners", () -> HandlerList.unregisterAll(this));
		runShutdownStep("closing debug output", () -> getDebug().close());
		this.dataReady = false;
		this.runtimeHandlersRegistered = false;
		this.worldedit = false;
		instance = null;
	}

	@Override
	public synchronized void onDataLoad() {
		if (!Bukkit.isPrimaryThread()) {
			getServer().getScheduler().runTask(this, this::onDataLoad);
			return;
		}
		if (this.dataReady) {
			getLogger().warning("Ignoring duplicate data-load callback");
			return;
		}

		getDebug().debug("Loading data");
		try {
			this.dataHelper = new DataHelper(this);

			this.dataloader = new Dataloader(this);
			this.dataloader.load(this.getDataManager());
			this.dataReady = true;

			this.registerRuntimeHandlers();
			this.synchronizeOnlineUsers();
			this.villageDataTaskHandler = new VillageDataTaskHandler(this);
			this.villageDataTaskHandler.startHandler();
			this.villageAnimationManager.start();
			this.refreshOnlineAddons();
			getDebug().debug("Plugin enabled");
		} catch (RuntimeException exception) {
			getLogger().log(Level.SEVERE, "Could not finish loading village data", exception);
			emergencyStop();
		}
	}

	@Override
	public void onConfigReload() {
		getDebug().debug("Reloading Plugin..");
		this.messages.reload(Settings.LANGUAGE_MODE.getString(), true);
		if (!this.configurationManager.loadAll()) {
			getLogger().warning("One or more configuration files were invalid and kept their previous values");
		}
		this.villageAnimationManager.reload();
		this.commandLang.reloadArguments();
		if (this.configuredCommandRegistry != null) {
			this.configuredCommandRegistry.reload();
		}
		this.guiSettings.reload();
		this.scoreboardManager.reloadScoreboard();
		this.tablistConfig.reload();
		this.teleportManager.reload();
		if (!Settings.ADDONS_SPAWN_ENABLE.getBoolean()) {
			this.teleportManager.cancelTeleports(TeleportManager.TeleportType.SPAWN, true);
		}
		this.loadLevels();
		this.updateWorldEditState();
		if (this.upgradeManager != null) {
			this.upgradeManager.refreshWorldEditIntegration();
		}
		if (this.worldedit) {
			this.saveSchematics();
		}
		if (this.villageBuildEditorManager != null) {
			this.villageBuildEditorManager.reload();
		}
		this.refreshOnlineAddons();
		if (this.villageDataTaskHandler != null) {
			this.villageDataTaskHandler.reloadHandler();
		}
	}

	@Override
	public List<Config> getExtraConfig() {
		return this.configurationManager.getExtraConfigs();
	}

	@Override
	public void reloadConfig() {
		if (this.configurationManager.reloadMainConfig()) {
			this.onConfigReload();
		}
	}

	private void registerRuntimeHandlers() {
		if (this.runtimeHandlersRegistered)
			return;

		CommandManager commandManager = new CommandManager(this);
		commandManager.addCommand(new CommandCrafting(this, this.guiManager));

		this.configuredCommandRegistry = new ConfiguredCommandRegistry(this);
		this.configuredCommandRegistry.register();
		registerCommands("test", isDev() ? new CommandTest(this) : null);

		registerListeners(
				new JoinListener(this),
				new QuitListener(this),
				new BlockListener(this),
				new InteractListener(this),
				new MoveListener(this),
				new ChatListener(this),
				new InteractBlockListeners(this),
				new PlayerListeners(this),
				new VillageListener(this),
				new BlockItemListener(this),
				new QuestListener(this, this.questManager),
				new RankingListener(this.rankingManager),
				this.villageBuildEditorManager == null ? null : new BuildEditorListener(this.villageBuildEditorManager),
				new CombatListener(this.combatManager),
				WorldGuardHook.isEnabled() ? new CombatRegionListener(this.combatManager) : null
		);
		this.runtimeHandlersRegistered = true;
	}

	private void synchronizeOnlineUsers() {
		for (Player player : this.getServer().getOnlinePlayers()) {
			this.userManager.getOrCreate(player);
		}
	}

	private void updateWorldEditState() {
		boolean available = WorldEditHook.isEnabled()
				&& Settings.WORLDEDIT.getBoolean();
		if (available != this.worldedit) {
			getLogger().info("WorldEdit village builds " + (available ? "enabled" : "disabled"));
		}
		this.worldedit = available;
	}

	private void registerPlaceholders() {
		getDebug().debug("Registering tablist placeholders");
		this.rankPlaceholdersService = new RankPlaceholdersService(this.tablistConfig, this.userRankManager, this.villageRankManager);
		this.tablistPlaceholdersService = new TablistPlaceholdersService(this, this.rankPlaceholdersService);
	}

	private boolean loadLevels() {
		getDebug().debug("Loading levels from file");
		LevelManager loadedLevels = new LevelManager();
		for (String levelName : this.levelsFile.getKeys(false)) {
			ConfigurationSection levels = this.levelsFile.getConfigurationSection(levelName);
			if (levels == null || !levelName.toLowerCase().startsWith("level-")) {
				getLogger().warning("Ignoring invalid levels.yml section: " + levelName);
				continue;
			}

			try {
				int level = Integer.parseInt(levelName.substring(levelName.indexOf('-') + 1));
				if (level < 1 || level > LevelManager.MAX_LEVEL) {
					getLogger().warning("Ignoring level outside supported range 1-" + LevelManager.MAX_LEVEL + ": " + levelName);
					continue;
				}
				int costExperience = Math.max(0, levels.getInt("Cost-xp"));
				int costEconomy = Math.max(0, levels.getInt("Cost-eco"));
				int size = Math.max(1, levels.getInt("Size"));

				Map<XMaterial, Integer> materials = new LinkedHashMap<>();
				for (String materialEntry : levels.getStringList("Cost-item")) {
					String[] parts = materialEntry.split(":", 2);
					XMaterial material = parts.length == 2
							? CompatibleMaterial.getMaterial(parts[0].trim()).orElse(null) : null;
					if (material == null) {
						getLogger().warning("Ignoring invalid level material: " + materialEntry);
						continue;
					}
					int amount = Integer.parseInt(parts[1].trim());
					if (amount > 0) {
						materials.put(material, amount);
					}
				}
				loadedLevels.addLevel(level, costExperience, costEconomy, size, materials);
			} catch (NumberFormatException exception) {
				getLogger().log(Level.WARNING, "Ignoring invalid level definition: " + levelName, exception);
			}
		}

		if (!loadedLevels.isLevel(1)) {
			getLogger().severe("levels.yml must contain a valid level-1 section");
			return false;
		}

		int highestLevel = loadedLevels.getHighestLevel().getLevel();
		for (int level = 1; level <= highestLevel; level++) {
			if (!loadedLevels.isLevel(level)) {
				getLogger().severe("levels.yml is missing level-" + level + "; keeping the previous level configuration");
				return false;
			}
		}
		this.levelManager = loadedLevels;
		return true;
	}

	public void reloadLevels() {
		this.levelsFile.load();
		this.loadLevels();
	}

	private void refreshOnlineAddons() {
		this.scoreboardManager.synchronizeBoards();
		if (this.tablistManager != null) {
			this.tablistManager.reload();
		}
		if (this.combatManager != null) {
			this.combatManager.reload();
		}
		if (this.questManager != null) {
			this.questManager.reload();
		}
		if (this.logManager != null) {
			this.logManager.reload();
		}
		if (this.rankingManager != null) {
			this.rankingManager.reload();
		}
	}

    private void registerConfig() {
		getDebug().debug("Registering configs");
		commandLang = new CommandConfig(this);
		guiSettings = new GuiConfig(this);

		getDebug().debug("Setting configs");
		commandLang.setConfig();
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

	private void saveBundledLocales() {
		for (String localeName : List.of("pl_PL", "de_DE")) {
			Locale.saveDefaultLocale(this, localeName, localeName);
		}
	}

	public boolean isPlaceholder() {
		return placeholder != null;
	}
}
