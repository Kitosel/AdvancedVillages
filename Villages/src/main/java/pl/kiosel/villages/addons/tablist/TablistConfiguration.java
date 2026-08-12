package pl.kiosel.villages.addons.tablist;

import org.bukkit.configuration.ConfigurationSection;
import pl.kiosel.core.configuration.Config;
import pl.kiosel.core.nms.playerlist.PlayerListConstants;
import pl.kiosel.core.nms.playerlist.SkinTexture;
import pl.kiosel.core.utils.NumberRange;
import pl.kiosel.core.utils.NumberUtils;
import pl.kiosel.core.utils.TextUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.settings.Settings;

import java.util.*;

public final class TablistConfiguration {

	private static final int DEFAULT_UPDATE_INTERVAL = 20;
	private static final int MAX_UPDATE_INTERVAL = 20 * 60;

	private final AdvancedVillages plugin;
	private final Config file;
	private volatile TablistSnapshot snapshot;

	public TablistConfiguration(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.file = plugin.getTablistFile();
		this.reload();
	}

	public synchronized void reload() {
		int updateInterval = NumberUtils.clamp(
				this.file.getInt("update-interval-ticks", DEFAULT_UPDATE_INTERVAL),
				1,
				MAX_UPDATE_INTERVAL
		);
		Map<Integer, String> cells = readCells(this.file, "cells");
		List<TablistFrame> frames = readFrames(this.file, "animation.pages");
		Map<NumberRange, SkinTexture> textures = readTextures(this.file, "heads.textures");

		this.snapshot = new TablistSnapshot(
				Settings.ADDONS_TABLIST_ENABLE.getBoolean(),
				this.file.getBoolean("animation.enabled", true) && !frames.isEmpty(),
				TextUtils.readText(this.file, "header", "&6Advanced&bVillages"),
				TextUtils.readText(this.file, "footer", ""),
				this.file.getInt("cells-ping", 0),
				this.file.getBoolean("fill-cells", true),
				updateInterval,
				this.file.getBoolean("use-relationship-colors", false),
				cells,
				frames,
				this.file.getBoolean("heads.enabled", false) ? textures : Collections.emptyMap()
		);
	}

	public TablistSnapshot snapshot() {
		return this.snapshot;
	}

	public boolean isEnabled() {
		return this.snapshot.isEnabled();
	}

	public int getUpdateInterval() {
		return this.snapshot.getUpdateInterval();
	}

	public boolean shouldUseRelationshipColors() {
		return this.snapshot.shouldUseRelationshipColors();
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

	private List<TablistFrame> readFrames(ConfigurationSection config, String path) {
		ConfigurationSection section = config.getConfigurationSection(path);
		if (section == null) {
			return Collections.emptyList();
		}

		List<TablistFrame> frames = new ArrayList<>();
		for (String key : section.getKeys(false)) {
			ConfigurationSection page = section.getConfigurationSection(key);
			if (page == null) {
				this.plugin.getLogger().warning("Ignoring invalid tablist page: " + key);
				continue;
			}

			double durationSeconds = Math.max(0.05D, page.getDouble("duration-seconds", 10D));
			long durationTicks = Math.max(1L, (long) Math.ceil(durationSeconds * 20D));
			frames.add(new TablistFrame(
					durationTicks,
					readCells(page, "cells"),
					TextUtils.readOptionalText(page, "header"),
					TextUtils.readOptionalText(page, "footer")
			));
		}
		return frames;
	}

	private Map<NumberRange, SkinTexture> readTextures(ConfigurationSection config, String path) {
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
}
