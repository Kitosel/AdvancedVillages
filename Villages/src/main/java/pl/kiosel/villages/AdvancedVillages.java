package pl.kiosel.villages;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import panda.std.Option;
import pl.kiosel.core.MetaCore;
import pl.kiosel.core.MetaPlugin;
import pl.kiosel.core.commands.CommandManager;
import pl.kiosel.core.compatibility.CompatibleMaterial;
import pl.kiosel.core.configuration.Config;
import pl.kiosel.core.dependencies.de.tr7zw.nbtapi.NBT;
import pl.kiosel.core.gui.GuiManager;
import pl.kiosel.core.hooks.WorldGuardHook;
import pl.kiosel.core.hooks.economy.EconomyHook;
import pl.kiosel.core.input.Licenses;
import pl.kiosel.core.nms.Nms;
import pl.kiosel.dependencies.com.cryptomorin.xseries.XMaterial;
import pl.kiosel.villages.addons.antylogout.CombatConfig;
import pl.kiosel.villages.addons.antylogout.CombatManager;
import pl.kiosel.villages.addons.antylogout.listener.CombatListener;
import pl.kiosel.villages.addons.antylogout.listener.CombatRegionListener;
import pl.kiosel.villages.addons.buildeditor.BuildEditorListener;
import pl.kiosel.villages.addons.buildeditor.VillageBuildEditorManager;
import pl.kiosel.villages.addons.placeholder.PlaceholderManager;
import pl.kiosel.villages.addons.scoreboard.ScoreboardHandler;
import pl.kiosel.villages.addons.scoreboard.ScoreboardManager;
import pl.kiosel.villages.addons.spawn.CommandSpawn;
import pl.kiosel.villages.addons.spawn.SpawnListener;
import pl.kiosel.villages.addons.spawn.SpawnManager;
import pl.kiosel.villages.addons.tablist.PlayerList;
import pl.kiosel.villages.addons.tablist.TablistPlaceholdersService;
import pl.kiosel.villages.addons.trials.VillageAnimationManager;
import pl.kiosel.villages.api.VillageAPI;
import pl.kiosel.villages.commands.CommandCrafting;
import pl.kiosel.villages.commands.CommandTest;
import pl.kiosel.villages.commands.CommandVillage;
import pl.kiosel.villages.config.CommandConfig;
import pl.kiosel.villages.config.Configuration;
import pl.kiosel.villages.config.GuiConfig;
import pl.kiosel.villages.config.TablistConfiguration;
import pl.kiosel.villages.data.rank.DefaultTops;
import pl.kiosel.villages.data.rank.RankPlaceholdersService;
import pl.kiosel.villages.data.user.User;
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
import pl.kiosel.villages.settings.BlacklistHandler;
import pl.kiosel.villages.settings.Settings;
import pl.kiosel.villages.storage.DataHelper;
import pl.kiosel.villages.storage.Dataloader;
import pl.kiosel.villages.storage.migrations._1_InitialMigration;
import pl.kiosel.villages.storage.migrations._2_VillageTntMigration;
import pl.kiosel.villages.storage.migrations._3_VillageAnimationsMigration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class AdvancedVillages extends MetaPlugin {

	@Getter private final Config levelsFile = new Config(this, "levels.yml");
	private final Config buildEditorFile = new Config(this, "addons/build-editor.yml");
	private final Config villageAnimationFile = new Config(this, "addons/animation.yml");
	@Getter private final Config commandFile = new Config(this, "command.yml");
	@Getter private final Config spawnFile = new Config(this, "addons/spawn.yml");
	@Getter private final Config combatFile = new Config(this, "addons/antilogout.yml");
	@Getter private final Config scoreboardFile = new Config(this, "addons/scoreboard.yml");

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
	@Getter private SpawnManager spawnManager;
	@Getter private VillageRemoveManager villageRemoveManager;
	@Getter private VillageAnimationManager villageAnimationManager;
	@Getter private VillageBuildEditorManager villageBuildEditorManager;

	@Getter private Configuration guiConfig;
	@Getter private CommandConfig commandLang;
	private GuiConfig g_config;

	@Getter private VillageGUIManager villageGui;
	@Getter private GuiManager guiManager = new GuiManager(this);

	@Getter private boolean worldedit;
	@Getter private PlaceholderManager placeholder;

	@Getter private BlacklistHandler blacklistHandler;
	@Getter private ScoreboardHandler scoreboardHandler;

	@Getter private TablistConfiguration tablistConfig;
	@Getter private TablistPlaceholdersService tablistPlaceholdersService;
	@Getter private RankPlaceholdersService rankPlaceholdersService;

	@Getter private UserManager userManager;
	@Getter private VillageManager villageManager;
	@Getter private VillageRankManager villageRankManager;
	@Getter private UserRankManager userRankManager;

	@Getter private Dataloader dataloader;
	private VillageDataTaskHandler villageDataTaskHandler;

	@Getter private CombatManager combatManager;
	@Getter private CombatConfig combatConfig;

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

		this.economy = getHookManager().getEconomyHookRegistry().getActive().orElseThrow(() ->
				new IllegalStateException("No supported economy plugin found"));

		setLocale(Settings.LANGUAGE_MODE.getString(), false);

		this.registerConfig();
		this.loadConfigs();
		this.loadLevels();

		if (!isDev()) {
			getDebug().debug("Checking license");
			if (!(new Licenses(Settings.LICENSE.getString(), this).register())) {
				emergencyStop();
				return;
			}
		}
		if (!NBT.preloadApi()) {
			getDebug().debug("NBT-API wasn't initialized properly, disabling the plugin");
			emergencyStop();
			return;
		}
		getDebug().debug("Enabling Plugin");

		this.blacklistHandler = new BlacklistHandler(this);
		this.scoreboardHandler = new ScoreboardHandler(this);

		this.userManager = new UserManager(this);
		this.villageManager = new VillageManager();
		this.villageRankManager = new VillageRankManager();
		this.villageRankManager.register(DefaultTops.defaultVillageTops(this.villageManager));
		this.userRankManager = new UserRankManager();
		this.userRankManager.register(DefaultTops.defaultUserTops(this.userManager));

		getDebug().debug("Set Village api");
		this.api = new VillageAPI(this);

		initDatabase(new _1_InitialMigration(), new _2_VillageTntMigration(), new _3_VillageAnimationsMigration());

		if(getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			getDebug().debug("Hooked PlaceholderApi");
			this.placeholder = new PlaceholderManager(this);
			this.placeholder.register();
		}
		if(getServer().getPluginManager().isPluginEnabled("WorldEdit") && Settings.WORLDEDIT.getBoolean()) {
			getDebug().debug("Hooked WorldEdit");
			this.worldedit = true;
		}

		if (this.worldedit)
			saveSchematics();

		this.inviteManager = new InviteManager(this);
		this.villageUtilsManager = new VillageUtilsManager(this);
		this.villageRemoveManager = new VillageRemoveManager(this);
		this.upgradeManager = new UpgradeManager(this);
		this.villageAnimationManager = new VillageAnimationManager(this, this.villageAnimationFile);
		this.teleportManager = new TeleportManager(this);
		this.permissionManager = new PermissionManager(this);
		this.villageGui = new VillageGUIManager(this);

		this.scoreboardManager = new ScoreboardManager(this);
		this.scoreboardManager.reloadScoreboard();

		this.tablistConfig = new TablistConfiguration(this);
		this.registerPlaceholders();

		this.villageAnimationManager.reload();
		if (getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
			this.villageBuildEditorManager = new VillageBuildEditorManager(this, this.buildEditorFile);
		}
		this.craftingManager = new CraftingManager(this);
		this.craftingManager.createRecipe();
		this.craftingManager.registerRecipe();

		this.combatConfig = new CombatConfig(this);
		this.combatManager = new CombatManager();

		this.spawnManager = new SpawnManager(this);

		CommandManager commandManager = new CommandManager(this);
		commandManager.addCommand(new CommandCrafting(this, this.guiManager));

		registerCommands("village",
				new CommandVillage(this),
				new CommandSpawn(this),
				isDev() ? new CommandTest(this) : null
		);

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
				new SpawnListener(this),
				this.villageBuildEditorManager == null ? null : new BuildEditorListener(this.villageBuildEditorManager),
				new CombatListener(this, this.combatManager, this.combatConfig),
				WorldGuardHook.isEnabled() ? new CombatRegionListener(this.combatManager, this.combatConfig) : null
		);
		getDebug().debug("Plugin enabled");
	}

	@Override
	public void onPluginDisable() {
		getDebug().debug("Disabling Plugin");

		runShutdownStep("canceling pending teleports", () -> {
			if (this.spawnManager != null) {
				this.spawnManager.cancelAll(true);
			}
			if (this.teleportManager != null) {
				this.teleportManager.shutdown();
			}
		});
		runShutdownStep("stopping scheduled tasks", () -> {
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
		runShutdownStep("unregistering recipes", () -> {
			if (this.craftingManager != null) {
				this.craftingManager.unRegisterRecipe();
			}
		});
		runShutdownStep("saving data", () -> {
			if (this.dataloader != null) {
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
		runShutdownStep("unregistering listeners", () -> HandlerList.unregisterAll(this));
		runShutdownStep("closing debug output", () -> getDebug().close());
	}

	@Override
	public void onDataLoad() {
		getDebug().debug("Loading data");
		this.dataHelper = new DataHelper(this);

		this.dataloader = new Dataloader(this);
		this.dataloader.load(this.getDataManager());

		this.villageDataTaskHandler = new VillageDataTaskHandler(this);
		this.villageDataTaskHandler.startHandler();
		this.villageAnimationManager.start();
		this.refreshOnlineAddons();
	}

	@Override
	public void onConfigReload() {
		getDebug().debug("Reloading Plugin..");
		this.setLocale(Settings.LANGUAGE_MODE.getString(), true);
		this.loadConfigs();
		this.villageAnimationManager.reload();
		this.commandLang.setConfig();
		this.g_config.setConfig();
		this.blacklistHandler.reload();
		this.scoreboardManager.reloadScoreboard();
		this.tablistConfig.reload();
		this.spawnManager.reload();
		if (!Settings.ADDONS_SPAWN_ENABLE.getBoolean()) {
			this.spawnManager.cancelAll(true);
		}
		this.loadLevels();
		if (getServer().getPluginManager().isPluginEnabled("WorldEdit") && Settings.WORLDEDIT.getBoolean()) {
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
		return List.of(this.spawnFile, this.levelsFile, this.combatFile, this.commandFile,
				this.villageAnimationFile, this.buildEditorFile);
	}

	private void registerPlaceholders() {
		getDebug().debug("Registering tablist placeholders");
		this.rankPlaceholdersService = new RankPlaceholdersService(this.tablistConfig, this.userRankManager, this.villageRankManager);
		this.tablistPlaceholdersService = new TablistPlaceholdersService(this, this.rankPlaceholdersService);
	}

	private void loadConfigs() {
		getDebug().debug("Loading combat file");
		if (!this.combatFile.getFile().exists()) {
			this.saveResource("addons/antilogout.yml", false);
		}
		this.combatFile.load();

		getDebug().debug("Loading scoreboard file");
		if (!this.scoreboardFile.getFile().exists()) {
			this.saveResource("addons/scoreboard.yml", false);
		}
		this.scoreboardFile.load();

		getDebug().debug("Loading spawn file");
		if (!this.spawnFile.getFile().exists()) {
			this.saveResource("addons/spawn.yml", false);
		}
		this.spawnFile.load();

		getDebug().debug("Loading village animation file");
		if (!this.villageAnimationFile.getFile().exists()) {
			this.saveResource("addons/animation.yml", false);
		}
		this.villageAnimationFile.load();

		getDebug().debug("Loading build editor file");
		if (!this.buildEditorFile.getFile().exists()) {
			this.saveResource("addons/build-editor.yml", false);
		}
		this.buildEditorFile.load();

		getDebug().debug("Loading levels file");
		if (!this.levelsFile.getFile().exists()) {
			this.saveResource("levels.yml", false);
		}
		this.levelsFile.load();
	}

	private void loadLevels() {
		getDebug().debug("Loading levels from file");
		this.levelManager = new LevelManager();
		this.levelManager.clear();
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
							? CompatibleMaterial.getMaterial(parts[0].trim()).orElse(null)
							: null;
					if (material == null) {
						getLogger().warning("Ignoring invalid level material: " + materialEntry);
						continue;
					}
					int amount = Integer.parseInt(parts[1].trim());
					if (amount > 0) {
						materials.put(material, amount);
					}
				}
				this.levelManager.addLevel(level, costExperience, costEconomy, size, materials);
			} catch (NumberFormatException exception) {
				getLogger().log(Level.WARNING, "Ignoring invalid level definition: " + levelName, exception);
			}
		}
	}

	public void reloadLevels() {
		this.levelsFile.load();
		this.loadLevels();
	}

	private void refreshOnlineAddons() {
		this.scoreboardManager.synchronizeBoards();

		for (Player player : this.getServer().getOnlinePlayers()) {
			Option<User> userOption = this.userManager.findByPlayer(player);
			if (userOption.isEmpty()) {
				continue;
			}
			User user = userOption.get();

			if (!this.tablistConfig.isEnabled()) {
				user.getCache().setPlayerList(null);
				continue;
			}
			PlayerList playerList = getIndividualPlayerList(user);
			playerList.send();
			user.getCache().setPlayerList(playerList);
		}
	}

    private void registerConfig() {
		getDebug().debug("Registering configs");
		guiConfig = new Configuration(this, "guis.yml");

        guiConfig.saveDefaultConfig();

		commandLang = new CommandConfig(this);
        g_config = new GuiConfig(this);

		getDebug().debug("Setting configs");
		commandLang.setConfig();
        g_config.setConfig();
    }

	public @NotNull PlayerList getIndividualPlayerList(User user) {
		return new PlayerList(
				this, user,
				Nms.getImplementations().getPlayerListAccessor(),
				getMetaServer(),
				this.tablistConfig
		);
	}

	private void saveSchematics() {
		getDebug().debug("Saving schematics");
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

	private void saveBundledLocales() {
		for (String localeName : List.of("pl_PL", "de_DE")) {
			pl.kiosel.core.locale.Locale.saveDefaultLocale(this, localeName, localeName);
		}
	}

	public boolean isPlaceholder() {
		return placeholder != null;
	}
}
