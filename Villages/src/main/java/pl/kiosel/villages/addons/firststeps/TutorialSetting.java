package pl.kiosel.villages.addons.firststeps;

import lombok.Getter;
import org.bukkit.Material;
import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.VillageConfigFile;

import java.io.File;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;

public enum TutorialSetting {

	LANGUAGE(Category.MAIN, "language", 10, Material.BOOK,
			VillageConfigFile.CONFIG, "settings.language", "en_US", TutorialSetting::languages),
	TIME_ZONE(Category.MAIN, "timezone", 12, Material.CLOCK,
			VillageConfigFile.CONFIG, "settings.time-zone", "Europe/Warsaw", TutorialSetting::timeZones),
	ECONOMY(Category.MAIN, "economy", 14, Material.GOLD_NUGGET,
			VillageConfigFile.CONFIG, "settings.economy", "Vault", TutorialSetting::economies),
	WORLDEDIT(Category.MAIN, "worldedit", 16, Material.WOODEN_AXE,
			VillageConfigFile.CONFIG, "settings.use-worldedit", false),

	SCOREBOARD(Category.ADDONS, "scoreboard", 10, Material.OAK_SIGN,
			VillageConfigFile.CONFIG, "addons.scoreboard", true),
	TABLIST(Category.ADDONS, "tablist", 11, Material.PLAYER_HEAD,
			VillageConfigFile.CONFIG, "addons.tablist", true),
	ANTILOGOUT(Category.ADDONS, "antilogout", 12, Material.IRON_SWORD,
			VillageConfigFile.CONFIG, "addons.antylogout", true),
	BUILD_EDITOR(Category.ADDONS, "build-editor", 13, Material.BRICKS,
			VillageConfigFile.BUILD_EDITOR, "enabled", true),
	SPAWN(Category.ADDONS, "spawn", 14, Material.COMPASS,
			VillageConfigFile.CONFIG, "addons.spawn", true),
	CHAT(Category.ADDONS, "chat", 15, Material.PAPER,
			VillageConfigFile.CONFIG, "chat.enabled", false),

	ANIMATIONS(Category.FEATURES, "animations", 9, Material.BLAZE_POWDER,
			VillageConfigFile.CONFIG, "addons.village-animations", true),
	DEVELOPMENT(Category.FEATURES, "development", 10, Material.EXPERIENCE_BOTTLE,
			VillageConfigFile.CONFIG, "addons.development", true,
			VillageConfigFile.DEVELOPMENT, "enabled"),
	LOGS(Category.FEATURES, "logs", 11, Material.BOOK,
			VillageConfigFile.LOGS, "enabled", true),
	QUESTS(Category.FEATURES, "quests", 12, Material.WRITABLE_BOOK,
			VillageConfigFile.CONFIG, "addons.quests", true),
	SPECIALIZATIONS(Category.FEATURES, "specializations", 13, Material.KNOWLEDGE_BOOK,
			VillageConfigFile.SPECIALIZATION, "enabled", true),
	RENT(Category.FEATURES, "rent", 14, Material.GOLD_INGOT,
			VillageConfigFile.VILLAGE, "rent.enabled", true),
	DIPLOMACY(Category.FEATURES, "diplomacy", 15, Material.PAPER,
			VillageConfigFile.VILLAGE, "diplomacy.enabled", true),
	RANKING(Category.FEATURES, "ranking", 16, Material.NETHER_STAR,
			VillageConfigFile.VILLAGE, "ranking.enabled", true);

	public enum Category {
		MAIN("main", "Main settings"),
		ADDONS("addons", "Addons"),
		FEATURES("features", "Village features");

		@Getter private final String id;
		@Getter private final String fallbackTitle;

		Category(String id, String fallbackTitle) {
			this.id = id;
			this.fallbackTitle = fallbackTitle;
		}
	}

	public enum ValueType {
		BOOLEAN("boolean"),
		STRING("string"),
		INTEGER("int"),
		LONG("long");

		@Getter private final String id;

		ValueType(String id) {
			this.id = id;
		}
	}

	@Getter
	private final Category category;
	@Getter
	private final String id;
	@Getter
	private final int slot;
	@Getter
	private final Material material;
	private final VillageConfigFile file;
	private final String path;
	@Getter
	private final ValueType valueType;
	private final Object fallback;
	private final VillageConfigFile secondaryFile;
	private final String secondaryPath;
	private final Function<AdvancedVillages, List<Option>> optionProvider;

	TutorialSetting(Category category, String id, int slot, Material material,
					VillageConfigFile file, String path, String fallback) {
		this(category, id, slot, material, file, path, fallback, null);
	}

	TutorialSetting(Category category, String id, int slot, Material material,
					VillageConfigFile file, String path, String fallback,
					Function<AdvancedVillages, List<Option>> optionProvider) {
		this(category, id, slot, material, file, path, ValueType.STRING, fallback,
				null, null, optionProvider);
	}

	TutorialSetting(Category category, String id, int slot, Material material,
					VillageConfigFile file, String path, boolean fallback) {
		this(category, id, slot, material, file, path, ValueType.BOOLEAN, fallback,
				null, null, null);
	}

	TutorialSetting(Category category, String id, int slot, Material material,
					VillageConfigFile file, String path, int fallback) {
		this(category, id, slot, material, file, path, ValueType.INTEGER, fallback,
				null, null, null);
	}

	TutorialSetting(Category category, String id, int slot, Material material,
					VillageConfigFile file, String path, long fallback) {
		this(category, id, slot, material, file, path, ValueType.LONG, fallback,
				null, null, null);
	}

	TutorialSetting(Category category, String id, int slot, Material material,
					VillageConfigFile file, String path, boolean fallback,
					VillageConfigFile secondaryFile, String secondaryPath) {
		this(category, id, slot, material, file, path, ValueType.BOOLEAN, fallback,
				secondaryFile, secondaryPath, null);
	}

	TutorialSetting(Category category, String id, int slot, Material material,
					VillageConfigFile file, String path, ValueType valueType, Object fallback,
					VillageConfigFile secondaryFile, String secondaryPath,
					Function<AdvancedVillages, List<Option>> optionProvider) {
		this.category = category;
		this.id = id;
		this.slot = slot;
		this.material = material;
		this.file = file;
		this.path = path;
		this.valueType = valueType;
		this.fallback = fallback;
		this.secondaryFile = secondaryFile;
		this.secondaryPath = secondaryPath;
		this.optionProvider = optionProvider;
	}

	public Object read(AdvancedVillages plugin) {
		RosaConfig config = config(plugin, this.file);
		switch (this.valueType) {
			case BOOLEAN:
				boolean enabled = config.getBoolean(this.path, (Boolean) this.fallback);
				if (this.secondaryFile == null) return enabled;
				return enabled && config(plugin, this.secondaryFile)
						.getBoolean(this.secondaryPath, (Boolean) this.fallback);
			case INTEGER:
				return config.getInt(this.path, (Integer) this.fallback);
			case LONG:
				return config.getLong(this.path, (Long) this.fallback);
			case STRING:
			default:
				return config.getString(this.path, (String) this.fallback);
		}
	}

	public Optional<Object> parse(String input) {
		String value = input == null ? "" : input.trim();
		if (value.isEmpty()) return Optional.empty();
		try {
			switch (this.valueType) {
				case INTEGER:
					return Optional.of(Integer.parseInt(value));
				case LONG:
					return Optional.of(Long.parseLong(value));
				case STRING:
					return Optional.of(value);
				case BOOLEAN:
				default:
					return Optional.empty();
			}
		} catch (NumberFormatException ignored) {
			return Optional.empty();
		}
	}

	public String display(Object value) {
		return value == null ? "" : String.valueOf(value);
	}

	public boolean hasOptions() {
		return this.optionProvider != null;
	}

	public List<Option> options(AdvancedVillages plugin) {
		if (this.optionProvider == null) return Collections.emptyList();
		return Collections.unmodifiableList(new ArrayList<>(this.optionProvider.apply(plugin)));
	}

	public void write(AdvancedVillages plugin, Object value, Set<VillageConfigFile> touchedFiles) {
		if (!this.accepts(value)) {
			throw new IllegalArgumentException("Invalid " + this.valueType.getId()
					+ " value for tutorial setting " + this.id);
		}
		config(plugin, this.file).set(this.path, value);
		touchedFiles.add(this.file);
		if (this.secondaryFile != null) {
			config(plugin, this.secondaryFile).set(this.secondaryPath, value);
			touchedFiles.add(this.secondaryFile);
		}
	}

	private boolean accepts(Object value) {
		switch (this.valueType) {
			case BOOLEAN:
				return value instanceof Boolean;
			case INTEGER:
				return value instanceof Integer;
			case LONG:
				return value instanceof Long;
			case STRING:
			default:
				return value instanceof String && !((String) value).trim().isEmpty();
		}
	}

	private static RosaConfig config(AdvancedVillages plugin, VillageConfigFile file) {
		return plugin.getConfigurationManager().get(file);
	}

	private static List<Option> languages(AdvancedVillages plugin) {
		TreeSet<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		File directory = new File(plugin.getDataFolder(), "locales");
		File[] files = directory.listFiles((ignored, name) -> name.toLowerCase(Locale.ROOT).endsWith(".lang"));
		if (files != null) {
			for (File file : files) {
				String name = file.getName();
				names.add(name.substring(0, name.length() - ".lang".length()));
			}
		}
		if (names.isEmpty()) names.add("en_US");
		List<Option> options = new ArrayList<>(names.size());
		for (String name : names) options.add(new Option(name, true));
		return options;
	}

	private static List<Option> timeZones(AdvancedVillages plugin) {
		TreeSet<String> names = new TreeSet<>(ZoneId.getAvailableZoneIds());
		List<Option> options = new ArrayList<>(names.size());
		for (String name : names) options.add(new Option(name, true));
		return options;
	}

	private static List<Option> economies(AdvancedVillages plugin) {
		List<Option> options = new ArrayList<>();
		for (String name : plugin.getHookManager().getEconomy().getSupportedNames()) {
			options.add(new Option(name, plugin.getHookManager().getEconomy().isAvailable(name)));
		}
		return options;
	}

	public static final class Option {
		@Getter private final String value;
		@Getter private final boolean available;

		private Option(String value, boolean available) {
			this.value = value;
			this.available = available;
		}
	}
}
