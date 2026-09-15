package pl.kiosel.villages.storage;

import org.bukkit.plugin.Plugin;
import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.rosacore.database.DatabaseSettings;
import pl.kiosel.rosacore.database.DatabaseType;

import java.io.File;

public final class VillageDatabaseSettings {

	private VillageDatabaseSettings() {
	}

	public static DatabaseSettings read(Plugin plugin, RosaConfig config) {
		DatabaseType type = DatabaseType.match(config.getString("Type", "H2"));
		if (type == null) {
			throw new IllegalArgumentException("Unsupported database type in database.yml");
		}

		int poolSize = Math.max(1, config.getInt("Pool Size", DatabaseSettings.DEFAULT_POOL_SIZE));
		String username = config.getString("Username", "username");
		String password = config.getString("Password", "password");
		if (type == DatabaseType.H2) {
			File databaseFile = new File(plugin.getDataFolder(), plugin.getName().toLowerCase());
			return new DatabaseSettings(type, databaseFile, null, 0, null, username, password,
					poolSize, false, DatabaseSettings.DEFAULT_CONNECTION_TIMEOUT_MILLIS);
		}

		return new DatabaseSettings(
				type,
				null,
				config.getString("Hostname", "localhost"),
				config.getInt("Port", type.getDefaultPort()),
				config.getString("Database", "database"),
				username,
				password,
				poolSize,
				config.getBoolean("Use-SSL", false),
				DatabaseSettings.DEFAULT_CONNECTION_TIMEOUT_MILLIS
		);
	}
}
