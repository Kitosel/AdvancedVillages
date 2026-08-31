package pl.kiosel.villages.data.village.level;

import org.bukkit.configuration.ConfigurationSection;
import pl.kiosel.rosacore.compatibility.ZMaterial;
import pl.kiosel.villages.AdvancedVillages;

import java.util.*;

public class LevelManager {
	public static final int MAX_LEVEL = 10;

	private final NavigableMap<Integer, Level> registeredLevels = new TreeMap<>();
	private final AdvancedVillages plugin;

	public LevelManager(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public void addLevel(int level, int costExperience, int costEconomy, int size, Map<ZMaterial, Integer> materials) {
		if (level < 1 || level > MAX_LEVEL) {
			throw new IllegalArgumentException("Village level must be between 1 and " + MAX_LEVEL);
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

	public void clear() {
		this.registeredLevels.clear();
	}

	public boolean loadLevels() {
		plugin.getDebug().debug("Loading levels from file");
		for (String levelName : plugin.getLevelsFile().getKeys(false)) {
			ConfigurationSection levels = plugin.getLevelsFile().getConfigurationSection(levelName);
			if (levels == null || !levelName.toLowerCase().startsWith("level-")) {
				plugin.getRosaLogger().warning("Ignoring invalid levels.yml section: " + levelName);
				continue;
			}

			try {
				int level = Integer.parseInt(levelName.substring(levelName.indexOf('-') + 1));
				if (level < 1 || level > LevelManager.MAX_LEVEL) {
					plugin.getRosaLogger().warning("Ignoring level outside supported range 1-" + LevelManager.MAX_LEVEL + ": " + levelName);
					continue;
				}
				int costExperience = Math.max(0, levels.getInt("Cost-xp"));
				int costEconomy = Math.max(0, levels.getInt("Cost-eco"));
				int size = Math.max(1, levels.getInt("Size"));

				Map<ZMaterial, Integer> materials = new LinkedHashMap<>();
				for (String materialEntry : levels.getStringList("Cost-item")) {
					String[] parts = materialEntry.split(":", 2);
					ZMaterial material = parts.length == 2
							? ZMaterial.match(parts[0].trim()).orElse(null) : null;
					if (material == null) {
						plugin.getRosaLogger().warning("Ignoring invalid level material: " + materialEntry);
						continue;
					}
					int amount = Integer.parseInt(parts[1].trim());
					if (amount > 0) {
						materials.put(material, amount);
					}
				}
				addLevel(level, costExperience, costEconomy, size, materials);
			} catch (NumberFormatException exception) {
				plugin.getRosaLogger().log(java.util.logging.Level.WARNING, "Ignoring invalid level definition: " + levelName, exception);
			}
		}

		if (!isLevel(1)) {
			plugin.getRosaLogger().severe("levels.yml must contain a valid level-1 section");
			return false;
		}

		int highestLevel = getHighestLevel().getLevel();
		for (int level = 1; level <= highestLevel; level++) {
			if (!isLevel(level)) {
				plugin.getRosaLogger().severe("levels.yml is missing level-" + level + "; keeping the previous level configuration");
				return false;
			}
		}
		return true;
	}

	public void reloadLevels() {
		plugin.getLevelsFile().load();
		this.loadLevels();
	}
}
