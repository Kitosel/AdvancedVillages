package pl.kiosel.villages.config;

import org.bukkit.configuration.InvalidConfigurationException;
import pl.kiosel.core.configuration.Config;
import pl.kiosel.villages.AdvancedVillages;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

public final class VillageConfigManager {

	private final AdvancedVillages plugin;
	private final Map<VillageConfigFile, Config> configurations = new EnumMap<>(VillageConfigFile.class);

	public VillageConfigManager(AdvancedVillages plugin) {
		this.plugin = plugin;
		for (VillageConfigFile file : VillageConfigFile.values()) {
			this.configurations.put(file, new Config(plugin, file.getPath()));
		}
	}

	public Config get(VillageConfigFile file) {
		Config config = this.configurations.get(file);
		if (config == null) {
			throw new IllegalArgumentException("Unregistered configuration: " + file);
		}
		return config;
	}

	public List<Config> getExtraConfigs() {
		return Collections.unmodifiableList(new ArrayList<>(this.configurations.values()));
	}

	public synchronized boolean loadAll() {
		boolean loaded = true;
		for (VillageConfigFile file : VillageConfigFile.values()) {
			loaded &= this.reload(file);
		}
		return loaded;
	}

	public synchronized boolean reloadMainConfig() {
		Config config = this.plugin.getCoreConfig();
		Config validation = new Config(config.getFile());
		if (!validation.load()) {
			this.plugin.getLogger().warning("Keeping the previous config.yml because the new file is invalid");
			return false;
		}

		config.clearConfig(false);
		return config.load();
	}

	public synchronized boolean reload(VillageConfigFile file) {
		Config config = this.get(file);
		if (!this.ensureFile(file, config)) {
			return false;
		}

		Config validation = new Config(config.getFile());
		if (!validation.load()) {
			this.plugin.getLogger().warning("Keeping the previous " + file.getPath() + " because the new file is invalid");
			return false;
		}

		Config defaults = this.loadBundledDefaults(file);
		config.clearConfig(true);
		if (defaults != null) {
			config.setDefaults(defaults);
		}
		return config.load();
	}

	private boolean ensureFile(VillageConfigFile file, Config config) {
		if (config.getFile().isFile()) {
			return true;
		}

		try {
			this.plugin.saveResource(file.getPath(), false);
			return true;
		} catch (IllegalArgumentException exception) {
			this.plugin.getLogger().log(Level.SEVERE,
					"Missing bundled configuration resource: " + file.getPath(), exception);
			return false;
		}
	}

	private Config loadBundledDefaults(VillageConfigFile file) {
		try (InputStream stream = this.plugin.getResource(file.getPath())) {
			if (stream == null) {
				return null;
			}

			Config defaults = new Config();
			defaults.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
			return defaults;
		} catch (IOException | InvalidConfigurationException exception) {
			this.plugin.getLogger().log(Level.WARNING,
					"Could not load defaults for " + file.getPath(), exception);
			return null;
		}
	}
}
