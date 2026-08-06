package pl.kiosel.villages.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import pl.kiosel.core.nms.playerlist.PlayerListConstants;
import pl.kiosel.core.nms.playerlist.SkinTexture;
import pl.kiosel.core.utils.NumberRange;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.tablist.TablistPage;
import pl.kiosel.villages.settings.Settings;

import java.util.*;

public final class TablistConfiguration {

	private static final int DEFAULT_UPDATE_INTERVAL = 20;
	private static final int MAX_UPDATE_INTERVAL = 20 * 60;

	private final AdvancedVillages plugin;
	private final Configuration file;
	private volatile Snapshot snapshot = Snapshot.empty();

	public TablistConfiguration(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.file = new Configuration(plugin, "addons/tablist.yml");
		this.file.saveDefaultConfig();
		this.reload();
	}

	public synchronized void reload() {
		this.file.saveDefaultConfig();
		this.file.reloadConfig();
		FileConfiguration config = this.file.getConfig();

		int updateInterval = clamp(
				config.getInt("update-interval-ticks", DEFAULT_UPDATE_INTERVAL),
				1,
				MAX_UPDATE_INTERVAL
		);
		Map<Integer, String> cells = readCells(config, "cells");
		List<TablistPage> pages = readPages(config, "animation.pages");
		Map<NumberRange, SkinTexture> textures = readTextures(config, "heads.textures");

		this.snapshot = new Snapshot(
				Settings.ADDONS_TABLIST_ENABLE.getBoolean(),
				config.getBoolean("animation.enabled", true) && !pages.isEmpty(),
				readText(config, "header", "&6Advanced&bVillages"),
				readText(config, "footer", ""),
				config.getInt("cells-ping", 0),
				config.getBoolean("fill-cells", true),
				updateInterval,
				config.getBoolean("use-relationship-colors", false),
				cells,
				pages,
				config.getBoolean("heads.enabled", false) ? textures : Collections.emptyMap()
		);
	}

	public boolean isEnabled() {
		return this.snapshot.enabled;
	}

	public boolean isAnimated() {
		return this.snapshot.animated;
	}

	public String getHeader() {
		return this.snapshot.header;
	}

	public String getFooter() {
		return this.snapshot.footer;
	}

	public int getCellsPing() {
		return this.snapshot.cellsPing;
	}

	public boolean shouldFillCells() {
		return this.snapshot.fillCells;
	}

	public int getUpdateInterval() {
		return this.snapshot.updateInterval;
	}

	public boolean shouldUseRelationshipColors() {
		return this.snapshot.useRelationshipColors;
	}

	public Map<Integer, String> getCells() {
		return this.snapshot.cells;
	}

	public List<TablistPage> getPages() {
		return this.snapshot.pages;
	}

	public Map<NumberRange, SkinTexture> getCellTextures() {
		return this.snapshot.cellTextures;
	}

	private Map<Integer, String> readCells(ConfigurationSection config, String path) {
		ConfigurationSection section = config.getConfigurationSection(path);
		if (section == null) {
			return Collections.emptyMap();
		}

		Map<Integer, String> cells = new LinkedHashMap<>();
		for (String key : section.getKeys(false)) {
			Integer index = parseCellIndex(key, path);
			if (index == null) {
				continue;
			}
			cells.put(index, section.getString(key, ""));
		}
		return cells;
	}

	private List<TablistPage> readPages(FileConfiguration config, String path) {
		ConfigurationSection section = config.getConfigurationSection(path);
		if (section == null) {
			return Collections.emptyList();
		}

		List<TablistPage> pages = new ArrayList<>();
		for (String key : section.getKeys(false)) {
			ConfigurationSection page = section.getConfigurationSection(key);
			if (page == null) {
				this.plugin.getLogger().warning("Ignoring invalid tablist page: " + key);
				continue;
			}

			double durationSeconds = Math.max(0.05D, page.getDouble("duration-seconds", 10D));
			long durationTicks = Math.max(1L, (long) Math.ceil(durationSeconds * 20D));
			pages.add(new TablistPage(
					durationTicks,
					readCells(page, "cells"),
					readOptionalText(page, "header"),
					readOptionalText(page, "footer")
			));
		}
		return pages;
	}

	private Map<NumberRange, SkinTexture> readTextures(FileConfiguration config, String path) {
		ConfigurationSection section = config.getConfigurationSection(path);
		if (section == null) {
			return Collections.emptyMap();
		}

		Map<NumberRange, SkinTexture> textures = new LinkedHashMap<>();
		for (String key : section.getKeys(false)) {
			ConfigurationSection texture = section.getConfigurationSection(key);
			if (texture == null) {
				continue;
			}

			String value = texture.getString("value", "").trim();
			String signature = texture.getString("signature", "").trim();
			if (value.isEmpty() || signature.isEmpty()) {
				this.plugin.getLogger().warning("Ignoring incomplete tablist head texture for cells " + key);
				continue;
			}

			NumberRange range = parseTextureRange(key);
			if (range != null) {
				textures.put(range, new SkinTexture(value, signature));
			}
		}
		return textures;
	}

	private NumberRange parseTextureRange(String value) {
		String[] boundaries = value.split("-", -1);
		if (boundaries.length < 1 || boundaries.length > 2) {
			return invalidTextureRange(value);
		}

		try {
			int minimum = Integer.parseInt(boundaries[0]);
			int maximum = boundaries.length == 1 ? minimum : Integer.parseInt(boundaries[1]);
			if (minimum < 1 || maximum > PlayerListConstants.DEFAULT_CELL_COUNT || minimum > maximum) {
				return invalidTextureRange(value);
			}
			return new NumberRange(minimum, maximum);
		} catch (NumberFormatException exception) {
			return invalidTextureRange(value);
		}
	}

	private NumberRange invalidTextureRange(String value) {
		this.plugin.getLogger().warning("Ignoring invalid tablist head range '" + value + "'; valid cells are 1-"
				+ PlayerListConstants.DEFAULT_CELL_COUNT);
		return null;
	}

	private Integer parseCellIndex(String value, String path) {
		try {
			int index = Integer.parseInt(value);
			if (index < 1 || index > PlayerListConstants.DEFAULT_CELL_COUNT) {
				throw new NumberFormatException();
			}
			return index;
		} catch (NumberFormatException exception) {
			this.plugin.getLogger().warning("Ignoring invalid tablist cell '" + value + "' in " + path
					+ "; valid cells are 1-" + PlayerListConstants.DEFAULT_CELL_COUNT);
			return null;
		}
	}

	private static String readText(ConfigurationSection config, String path, String fallback) {
		String value = readOptionalText(config, path);
		return value == null ? fallback : value;
	}

	private static String readOptionalText(ConfigurationSection config, String path) {
		if (!config.contains(path)) {
			return null;
		}
		if (config.isList(path)) {
			return String.join("\n", config.getStringList(path));
		}
		return config.getString(path, "");
	}

	private static int clamp(int value, int minimum, int maximum) {
		return Math.max(minimum, Math.min(maximum, value));
	}

	private static final class Snapshot {

		private final boolean enabled;
		private final boolean animated;
		private final String header;
		private final String footer;
		private final int cellsPing;
		private final boolean fillCells;
		private final int updateInterval;
		private final boolean useRelationshipColors;
		private final Map<Integer, String> cells;
		private final List<TablistPage> pages;
		private final Map<NumberRange, SkinTexture> cellTextures;

		private Snapshot(boolean enabled, boolean animated, String header, String footer, int cellsPing,
						 boolean fillCells, int updateInterval, boolean useRelationshipColors,
						 Map<Integer, String> cells, List<TablistPage> pages,
						 Map<NumberRange, SkinTexture> cellTextures) {
			this.enabled = enabled;
			this.animated = animated;
			this.header = header;
			this.footer = footer;
			this.cellsPing = cellsPing;
			this.fillCells = fillCells;
			this.updateInterval = updateInterval;
			this.useRelationshipColors = useRelationshipColors;
			this.cells = Collections.unmodifiableMap(new LinkedHashMap<>(cells));
			this.pages = Collections.unmodifiableList(new ArrayList<>(pages));
			this.cellTextures = Collections.unmodifiableMap(new LinkedHashMap<>(cellTextures));
		}

		private static Snapshot empty() {
			return new Snapshot(true, false, "", "", 0, true, DEFAULT_UPDATE_INTERVAL,
					false, Collections.emptyMap(), Collections.emptyList(), Collections.emptyMap());
		}
	}
}
