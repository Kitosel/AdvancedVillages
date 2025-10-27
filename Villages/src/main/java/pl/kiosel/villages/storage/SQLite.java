package pl.kiosel.villages.storage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import pl.kiosel.villages.Wioski;

public class SQLite extends Database {

	public SQLite(Wioski plugin) {
		super(plugin);
	}

	@Override
	HikariDataSource getDataSource() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			plugin.getLogger().severe("Failed to initialize SQLite driver");
			plugin.getDebug().debug(e);
			return null;
		}

		File folder = plugin.getDataFolder();
		if (!folder.exists()) folder.mkdirs();

		File dbFile = new File(folder, "village.db");
		if (!dbFile.exists()) {
			try {
				dbFile.createNewFile();
			} catch (IOException ex) {
				plugin.getLogger().severe("Failed to create database file");
				plugin.getDebug().debug(ex);
				return null;
			}
		}

		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
		config.setConnectionTestQuery("SELECT 1");

		return new HikariDataSource(config);
	}

	public void vacuum() {
		try (Connection con = dataSource.getConnection();
			 Statement s = con.createStatement()) {
			s.executeUpdate("VACUUM");
			plugin.getDebug().debug("Vacuumed SQLite database");
		} catch (SQLException ex) {
			plugin.getLogger().warning("Failed to vacuum database");
			plugin.getDebug().debug(ex);
		}
	}

	@Override
	String getQueryCreateTableVillage() {
		return "CREATE TABLE IF NOT EXISTS `" + tableVillage + "` (" +
				"`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"`owner` TEXT NOT NULL, " +
				"`village_name` TEXT NOT NULL, " +
				"`location` TEXT NOT NULL, " +
				"`tp` TEXT NOT NULL, " +
				"`members` TEXT NOT NULL, " +
				"`effects_data` TEXT NOT NULL, " +
				"`effects_active` TEXT NOT NULL, " +
				"`ints_data` TEXT NOT NULL, " +
				"`settings` TEXT NOT NULL, " +
				"`tag` TEXT NOT NULL" +
				");";
	}

	@Override
	String getQueryCreateTableUsers() {
		return "CREATE TABLE IF NOT EXISTS `" + tableVillage_users + "` (" +
				"`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"`name` TEXT NOT NULL, " +
				"`uuid` TEXT NOT NULL, " +
				"`village_name` TEXT NULL, " +
				"`village_permissions` TEXT NULL " +
				");";
	}
}
