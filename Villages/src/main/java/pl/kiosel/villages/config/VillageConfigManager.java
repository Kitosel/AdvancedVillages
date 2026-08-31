package pl.kiosel.villages.config;

import pl.kiosel.rosacore.config.ConfigLoadResult;
import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.villages.AdvancedVillages;

import java.util.EnumMap;
import java.util.Map;

public final class VillageConfigManager {

	private final AdvancedVillages plugin;
	private final Map<VillageConfigFile, RosaConfig> configurations = new EnumMap<>(VillageConfigFile.class);

	public VillageConfigManager(AdvancedVillages plugin) {
		this.plugin = plugin;
		for (VillageConfigFile file : VillageConfigFile.values()) {
			this.configurations.put(file, new RosaConfig(plugin, file.getPath()));
		}
	}

	public RosaConfig get(VillageConfigFile file) {
		RosaConfig config = this.configurations.get(file);
		if (config == null) {
			throw new IllegalArgumentException("Unregistered configuration: " + file);
		}
		return config;
	}

	public synchronized boolean loadAll() {
		boolean loaded = true;
		for (VillageConfigFile file : VillageConfigFile.values()) {
			loaded &= this.reload(file);
		}
		return loaded;
	}

	public synchronized boolean reloadMainConfig() {
		RosaConfig config = this.plugin.getCoreConfig();
		return this.reload(config, "config.yml");
	}

	public synchronized boolean reload(VillageConfigFile file) {
		return this.reload(this.get(file), file.getPath());
	}

	private boolean reload(RosaConfig config, String name) {
		ConfigLoadResult result = config.reload();
		if (result.isSuccess()) return true;

		this.plugin.getRosaLogger().warning("Keeping the previous " + name + " because the new file is invalid");
		result.getProblems().forEach(problem -> this.plugin.getRosaLogger().warning(
				(problem.getPath().isEmpty() ? name : problem.getPath()) + ": " + problem.getMessage()));
		if (result.getCause() != null)
			this.plugin.getRosaLogger().log(java.util.logging.Level.WARNING,
					"Could not reload " + name, result.getCause());
		return false;
	}
}
