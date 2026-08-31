package pl.kiosel.villages.storage;

import org.bukkit.plugin.Plugin;
import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.rosacore.database.DatabaseSettings;
import pl.kiosel.rosacore.database.DatabaseType;

import java.io.File;

public final class VillageDatabaseSettings {

	private static final String ROOT = "Connection Settings.";

	private VillageDatabaseSettings() {
	}

	public static DatabaseSettings read(Plugin plugin, RosaConfig config) {
		DatabaseType type = DatabaseType.match(config.getString(ROOT + "Type", "H2"));
		if (type == null) {
			throw new IllegalArgumentException("Unsupported database type in database.yml");
		}

		int poolSize = Math.max(1, config.getInt(ROOT + "Pool Size", DatabaseSettings.DEFAULT_POOL_SIZE));
		String username = config.getString(ROOT + "Username", "username");
		String password = config.getString(ROOT + "Password", "password");
		if (type == DatabaseType.H2) {
			File databaseFile = new File(plugin.getDataFolder(), plugin.getName().toLowerCase());
			return new DatabaseSettings(type, databaseFile, null, 0, null, username, password,
					poolSize, false, DatabaseSettings.DEFAULT_CONNECTION_TIMEOUT_MILLIS);
		}

		return new DatabaseSettings(
				type,
				null,
				config.getString(ROOT + "Hostname", "localhost"),
				config.getInt(ROOT + "Port", type.getDefaultPort()),
				config.getString(ROOT + "Database", "database"),
				username,
				password,
				poolSize,
				config.getBoolean(ROOT + "Use SSL", false),
				DatabaseSettings.DEFAULT_CONNECTION_TIMEOUT_MILLIS
		);
	}
}
