package pl.kiosel.villages.config;

import pl.kiosel.rosacore.config.ConfigLoadResult;
import pl.kiosel.rosacore.config.ConfigValidator;
import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.validation.DevelopmentConfigValidator;
import pl.kiosel.villages.config.validation.GuiConfigValidator;
import pl.kiosel.villages.config.validation.LevelsConfigValidator;
import pl.kiosel.villages.config.validation.SpecializationConfigValidator;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;

public final class VillageConfigManager {

	private final AdvancedVillages plugin;
	private final Map<VillageConfigFile, RosaConfig> configurations = new EnumMap<>(VillageConfigFile.class);

	public VillageConfigManager(AdvancedVillages plugin) {
		this.plugin = plugin;
		for (VillageConfigFile file : VillageConfigFile.values()) {
			if (!file.isDeveloperOnly()) this.register(file);
		}
	}

	public synchronized void registerDeveloperConfigurations() {
		if (!this.plugin.isDev()) return;
		for (VillageConfigFile file : VillageConfigFile.values()) {
			if (file.isDeveloperOnly()) this.register(file);
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
		if (file.isDeveloperOnly() && !this.plugin.isDev()) {
			return true;
		}
		return this.reload(this.get(file), file.getPath());
	}

	private boolean reload(RosaConfig config, String name) {
		boolean hadPreviousConfiguration = config.isLoaded();
		ConfigLoadResult result = config.reload();
		if (result.isSuccess()) return true;

		this.plugin.getRosaLogger().warning(hadPreviousConfiguration
				? "Keeping the previous " + name + " because the new file is invalid"
				: "Could not load " + name + " because the file is invalid");
		result.getProblems().forEach(problem -> this.plugin.getRosaLogger().warning(
				(problem.getPath().isEmpty() ? name : name + " -> " + problem.getPath())
						+ ": " + problem.getMessage()));
		if (result.getCause() != null)
			this.plugin.getRosaLogger().log(Level.WARNING, "Could not reload " + name, result.getCause());
		return false;
	}

	private void register(VillageConfigFile file) {
		if (this.configurations.containsKey(file)) return;
		RosaConfig.Builder builder = RosaConfig.builder(this.plugin, file.getPath());
		ConfigValidator validator = this.validator(file);
		if (validator != null) builder.validator(validator);
		this.configurations.put(file, builder.build());
	}

	private ConfigValidator validator(VillageConfigFile file) {
		switch (file) {
			case LEVELS:
				return new LevelsConfigValidator(this.plugin);
			case GUIS:
				return new GuiConfigValidator();
			case SPECIALIZATION:
				return new SpecializationConfigValidator();
			case DEVELOPMENT:
				return new DevelopmentConfigValidator(this.plugin);
			default:
				return null;
		}
	}
}
