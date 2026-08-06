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
import pl.kiosel.core.hooks.EconomyManager;
import pl.kiosel.core.hooks.WorldGuardHook;
import pl.kiosel.core.input.Licenses;
import pl.kiosel.core.nms.Nms;
import pl.kiosel.dependencies.com.cryptomorin.xseries.XMaterial;
import pl.kiosel.villages.addons.antylogout.CombatCache;
import pl.kiosel.villages.addons.antylogout.CombatConfig;
import pl.kiosel.villages.addons.antylogout.CombatListener;
import pl.kiosel.villages.addons.antylogout.CombatRegionListener;
import pl.kiosel.villages.addons.placeholder.PlaceholderManager;
import pl.kiosel.villages.addons.scoreboard.ScoreboardHandler;
import pl.kiosel.villages.addons.scoreboard.ScoreboardManager;
import pl.kiosel.villages.addons.tablist.IndividualPlayerList;
import pl.kiosel.villages.addons.tablist.services.TablistPlaceholdersService;
import pl.kiosel.villages.addons.tablist.services.service.BasicPlaceholdersService;
import pl.kiosel.villages.addons.tablist.services.service.PlayerPlaceholdersService;
import pl.kiosel.villages.addons.tablist.services.service.TimePlaceholdersService;
import pl.kiosel.villages.api.VillageAPI;
import pl.kiosel.villages.commands.CommandCrafting;
import pl.kiosel.villages.commands.CommandTest;
import pl.kiosel.villages.commands.CommandVillage;
import pl.kiosel.villages.config.CommandConfig;
import pl.kiosel.villages.config.Configuration;
import pl.kiosel.villages.config.GuiConfig;
import pl.kiosel.villages.config.TablistConfiguration;
import pl.kiosel.villages.data.rank.DefaultTops;
import pl.kiosel.villages.data.rank.placeholders.RankPlaceholdersService;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.UserManager;
import pl.kiosel.villages.data.user.UserRankManager;
import pl.kiosel.villages.data.user.placeholders.UserPlaceholdersService;
import pl.kiosel.villages.data.village.VillageManager;
import pl.kiosel.villages.data.village.VillageRankManager;
import pl.kiosel.villages.data.village.handler.BlockItemListener;
import pl.kiosel.villages.data.village.handler.InteractBlockListeners;
import pl.kiosel.villages.data.village.handler.PlayerListeners;
import pl.kiosel.villages.data.village.handler.VillageListener;
import pl.kiosel.villages.data.village.level.LevelManager;
import pl.kiosel.villages.data.village.placeholders.VillagePlaceholdersService;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.listeners.*;
import pl.kiosel.villages.manager.*;
import pl.kiosel.villages.settings.BlacklistHandler;
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

public final class AdvancedVillages extends MetaPlugin {

	private final Config levelsFile = new Config(this, "levels.yml");
	@Getter private final Config commandFile = new Config(this, "command.yml");
	@Getter private final Config combatFile = new Config(this, "antylogout.yml");

    @Getter private static AdvancedVillages instance;
    @Getter private VillageAPI api;
    @Getter private VillageUtilsManager villageUtilsManager;
	@Getter private PermissionManager permissionManager;
	@Getter private VillageRemoveManager villageRemoveManager;
    @Getter private InviteManager inviteManager;
    @Getter private UpgradeManager upgradeManager;
	@Getter private TeleportManager teleportManager;
	@Getter private ScoreboardManager scoreboardManager;
	@Getter private DataHelper dataHelper;
	@Getter private LevelManager levelManager;
	@Getter private CraftingManager craftingManager;

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

	@Getter private CombatCache combatCache;
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

		getDebug().debug("Registering Economy...");
		EconomyManager.load();

		getDebug().debug("Setup main config");
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

		this.userManager = new UserManager(this);
		this.villageManager = new VillageManager();
		this.villageRankManager = new VillageRankManager();
		this.villageRankManager.register(DefaultTops.defaultVillageTops(this.villageManager));
		this.userRankManager = new UserRankManager();
		this.userRankManager.register(DefaultTops.defaultUserTops(this.userManager));

		getDebug().debug("Set Village api");
		this.api = new VillageAPI(this);

		initDatabase(new _1_InitialMigration());

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
		this.teleportManager = new TeleportManager(this);
		this.permissionManager = new PermissionManager(this);
		this.villageGui = new VillageGUIManager(this);

		this.scoreboardManager = new ScoreboardManager(this);
		this.scoreboardManager.reloadScoreboard();

		this.tablistConfig = new TablistConfiguration();
		this.registerPlaceholders();
		this.loadLevels();
		this.loadCombat();

		this.craftingManager = new CraftingManager(this);
		this.craftingManager.createRecipe();
		this.craftingManager.registerRecipe();

		this.combatConfig = new CombatConfig(this);
		this.combatCache = new CombatCache();

		CommandManager commandManager = new CommandManager(this);
		commandManager.addCommand(new CommandCrafting(this, this.guiManager));

		registerCommands("village",
				new CommandVillage(this),
				isDev() ? new CommandTest(this) : null
		);

		registerListeners(
				new JoinListener(this),
				new QuitListener(this),
				new BlockListener(this),
				new InventoryListener(this),
				new InteractListener(this),
				new MoveListener(this),
				new ChatListener(this),
				new InteractBlockListeners(this),
				new PlayerListeners(this),
				new VillageListener(this),
				new BlockItemListener(this),
				new CombatListener(this, this.combatCache, this.combatConfig),
				WorldGuardHook.isEnabled() ? new CombatRegionListener(this.combatCache, this.combatConfig) : null
		);
		print("Villages enabled");
		getDebug().debug("Plugin enabled");

		this.villageDataTaskHandler = new VillageDataTaskHandler(this);
		this.villageDataTaskHandler.startHandler();
	}

	@Override
	public void onPluginDisable() {
		getDebug().debug("Disabling Plugin");
		try {
			this.craftingManager.unRegisterRecipe();
			this.dataloader.save(false);
			this.dataManager.shutdown();
			this.villageManager.onDisable();
			this.villageDataTaskHandler.stopHandler();

			if (isPlaceholder())
				this.placeholder.unregister();
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
		getDebug().debug("Loading data");
		this.dataHelper = new DataHelper(this);

		this.dataloader = new Dataloader(this);
		this.dataloader.load(this.getDataManager());
	}

	@Override
	public void onConfigReload() {
		getDebug().debug("Reloading Plugin..");
		this.setLocale(getConfig().getString("settings.language"), true);
		this.locale.reloadMessages();
		this.g_config.setConfig();
		this.blacklistHandler.reload();
		this.scoreboardHandler.reload();
		this.scoreboardManager.reloadScoreboard();
		this.villageDataTaskHandler.reloadHandler();
		this.loadLevels();
		this.loadCombat();

		for (Player player : this.getServer().getOnlinePlayers()) {
			Option<User> userOption = this.userManager.findByPlayer(player);
			if (userOption.isEmpty()) {
				continue;
			}
			User user = userOption.get();

			if (!Settings.ADDONS_TABLIST_ENABLE.getBoolean()) {
				continue;
			}
			user.getCache().setPlayerList(getIndividualPlayerList(user));
		}
	}

	@Override
	public List<Config> getExtraConfig() {
		return List.of(this.levelsFile, this.combatFile, this.commandFile);
	}

	private void registerPlaceholders() {
		getDebug().debug("Registering tablist placeholders");
		BasicPlaceholdersService basicPlaceholdersService = new BasicPlaceholdersService();
		basicPlaceholdersService.register(this, "simple", BasicPlaceholdersService.createSimplePlaceholders(this));

		TimePlaceholdersService timePlaceholdersService = new TimePlaceholdersService();
		timePlaceholdersService.register(this, "time", TimePlaceholdersService.createTimePlaceholders());

		PlayerPlaceholdersService playerPlaceholdersService = new PlayerPlaceholdersService();
		playerPlaceholdersService.register(this, "player", PlayerPlaceholdersService.createPlayerPlaceholders());

		UserPlaceholdersService userPlaceholdersService = new UserPlaceholdersService();
		userPlaceholdersService.register(this, "user", UserPlaceholdersService.createUserPlaceholders());
		userPlaceholdersService.register(this, "world", UserPlaceholdersService.createPlayerPlaceholders());

		VillagePlaceholdersService villagePlaceholdersService = new VillagePlaceholdersService();
		villagePlaceholdersService.register(this, "village", VillagePlaceholdersService.createSimplePlaceholders());
		villagePlaceholdersService.register(this, "villageRank", VillagePlaceholdersService.createVillagePlaceholders(this));

		rankPlaceholdersService = new RankPlaceholdersService(tablistConfig, userRankManager, villageRankManager);

		this.tablistPlaceholdersService = new TablistPlaceholdersService(this,
				basicPlaceholdersService,
				timePlaceholdersService,
				playerPlaceholdersService,
				userPlaceholdersService,
				villagePlaceholdersService
		);
	}

	private void loadCombat() {
		getDebug().debug("Loading combat file");
		if (!this.combatFile.getFile().exists()) {
			this.saveResource("antylogout.yml", false);
		}
		this.combatFile.load();
	}

	private void loadLevels() {
		getDebug().debug("Loading levels file");
		if (!this.levelsFile.getFile().exists()) {
			this.saveResource("levels.yml", false);
		}
		this.levelsFile.load();

		getDebug().debug("Loading levels from file");
		this.levelManager = new LevelManager();
		this.levelManager.clear();
		for (String levelName : this.levelsFile.getKeys(false)) {
			ConfigurationSection levels = this.levelsFile.getConfigurationSection(levelName);

			if (levels != null) {
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

	public @NotNull IndividualPlayerList getIndividualPlayerList(User user) {
		return new IndividualPlayerList(
				this, user,
				Nms.getImplementations().getPlayerListAccessor(),
				getMetaServer(),
				this.getTablistConfig().cells,
				this.getTablistConfig().header,
				this.getTablistConfig().footer,
				this.getTablistConfig().animated,
				this.getTablistConfig().pages,
				this.getTablistConfig().heads.textures,
				this.getTablistConfig().cellsPing,
				this.getTablistConfig().fillCells
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

	public boolean isPlaceholder() {
		return placeholder != null;
	}

	private void print(String s) {
		getServer().getConsoleSender().sendMessage("[Villages] " + s);
	}
}