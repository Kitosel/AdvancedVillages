package pl.kiosel.villages.data.village.level;

import org.bukkit.configuration.ConfigurationSection;
import pl.kiosel.rosacore.compatibility.ZMaterial;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.VillageConfigFile;

import java.io.File;
import java.util.*;

public class OutpostLevelManager {
	public static final int MAX_LEVEL = 5;

	private final NavigableMap<Integer, Level> registeredLevels = new TreeMap<>();
	private final AdvancedVillages plugin;

	public OutpostLevelManager(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public void addLevel(int level, int costExperience, int costEconomy, int size, Map<ZMaterial, Integer> materials) {
		if (level < 1 || level > MAX_LEVEL) {
			throw new IllegalArgumentException("Outpost level must be between 1 and " + MAX_LEVEL);
		}
		this.registeredLevels.put(level, new Level(level, costExperience, costEconomy, size, materials));
	}

	public Level getLevel(int level) {
		return this.registeredLevels.get(level);
	}

	public Level getLowestLevel() {
		return this.registeredLevels.firstEntry().getValue();
	}

	public Level getHighestLevel() {
		return this.registeredLevels.lastEntry().getValue();
	}

	public boolean isLevel(int level) {
		return this.registeredLevels.containsKey(level);
	}

	public Map<Integer, Level> getLevels() {
		return Collections.unmodifiableMap(this.registeredLevels);
	}

	public String getSchematicName(int level) {
		String configured = this.plugin.getOutpostFile().getString("levels." + level + ".schematic",
				"outpost_" + level);
		String name = configured == null ? "" : configured.trim();
		if (name.endsWith(".schem")) name = name.substring(0, name.length() - 6);
		return name.matches("[A-Za-z0-9_-]+") ? name : "outpost_" + level;
	}

	public File getSchematicFile(int level) {
		String name = this.getSchematicName(level);
		return new File(this.plugin.getDataFolder(), "schematics/outposts/" + name + ".schem");
	}

	public boolean hasSchematic(int level) {
		return this.plugin.isWorldedit() && this.getSchematicFile(level).isFile();
	}

	public void clear() {
		this.registeredLevels.clear();
	}

	public boolean loadLevels() {
		plugin.getDebug().debug("Loading outpost levels from file");

		ConfigurationSection section = plugin.getOutpostFile().snapshot().getConfigurationSection("levels");
		if (section == null) {
			plugin.getRosaLogger().severe("outposts.yml must contain a levels section");
			return false;
		}

		NavigableMap<Integer, Level> loadedLevels = new TreeMap<>();
		for (String levelName : section.getKeys(false)) {
			ConfigurationSection levelSection = section.getConfigurationSection(levelName);
			if (levelSection == null) {
				plugin.getRosaLogger().warning("Ignoring invalid outpost level section: " + levelName);
				continue;
			}

			try {
				String number = levelName.toLowerCase(Locale.ROOT).startsWith("level-")
						? levelName.substring(levelName.indexOf('-') + 1) : levelName;
				int level = Integer.parseInt(number);
				if (level < 1 || level > OutpostLevelManager.MAX_LEVEL) {
					plugin.getRosaLogger().warning("Ignoring outpost level outside supported range 1-"
							+ OutpostLevelManager.MAX_LEVEL + ": " + levelName);
					continue;
				}

				int costExperience = Math.max(0, levelSection.getInt("Cost-xp"));
				int costEconomy = Math.max(0, levelSection.getInt("Cost-eco"));
				int size = Math.max(1, levelSection.getInt("region-size"));

				Map<ZMaterial, Integer> materials = new LinkedHashMap<>();
				for (String materialEntry : levelSection.getStringList("Cost-item")) {
					String[] parts = materialEntry.split(":", 2);
					ZMaterial material = parts.length == 2
							? ZMaterial.match(parts[0].trim()).orElse(null) : null;

					if (material == null || material.resolveForItem().isEmpty()) {
						plugin.getRosaLogger().warning("Invalid outpost level material: " + materialEntry);
						return false;
					}

					int amount = Integer.parseInt(parts[1].trim());
					if (amount > 0) {
						materials.put(material, amount);
					}
				}
				loadedLevels.put(level, new Level(level, costExperience, costEconomy, size, materials));
			} catch (NumberFormatException exception) {
				plugin.getRosaLogger().log(java.util.logging.Level.WARNING,
						"Ignoring invalid outpost level definition: " + levelName, exception);
				return false;
			}
		}

		if (!loadedLevels.containsKey(1)) {
			plugin.getRosaLogger().severe("outposts.yml must contain a valid level 1 section");
			return false;
		}

		int highestLevel = loadedLevels.lastKey();
		for (int level = 1; level <= highestLevel; level++) {
			if (!loadedLevels.containsKey(level)) {
				plugin.getRosaLogger().severe("outposts.yml is missing level " + level
						+ "; keeping the previous outpost level configuration");
				return false;
			}
		}
		this.registeredLevels.clear();
		this.registeredLevels.putAll(loadedLevels);
		return true;
	}

	public boolean reloadLevels() {
		return plugin.getConfigurationManager().reload(VillageConfigFile.OUTPOSTS) && this.loadLevels();
	}
}
