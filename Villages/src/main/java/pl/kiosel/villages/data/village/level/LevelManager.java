package pl.kiosel.villages.data.village.level;

import pl.kiosel.dependencies.com.cryptomorin.xseries.XMaterial;

import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class LevelManager {
	public static final int MAX_LEVEL = 10;

	private final NavigableMap<Integer, Level> registeredLevels = new TreeMap<>();

	public void addLevel(int level, int costExperience, int costEconomy, int size, Map<XMaterial, Integer> materials) {
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
}
