package pl.kiosel.villages.storage;

import java.sql.*;
import java.util.logging.Level;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;
import pl.kiosel.common.utils.LocationUtils;
import pl.kiosel.villages.Wioski;
import pl.kiosel.villages.config.Config;
import pl.kiosel.common.Callback;
import pl.kiosel.villages.village.Village;
import pl.kiosel.villages.village.VillageMember;

public abstract class Database {

	private boolean connected;

	@Getter	String tableVillage;
	@Getter String tableVillage_users;
	Wioski plugin;
	HikariDataSource dataSource;

	abstract HikariDataSource getDataSource();
	abstract String getQueryCreateTableVillage();
	abstract String getQueryCreateTableUsers();

	protected Database(Wioski plugin) {
		this.plugin = plugin;
	}

	public void connect(Callback<Integer> callback) {
		if (!Config.storage_mysql_table.matches("^([a-zA-Z0-9\\-_]+)?$")) {
			plugin.getLogger().severe("Database table contains illegal letters, using 'villages'.");
			Config.storage_mysql_table = "villages";
		}
		tableVillage = Config.storage_mysql_table;
		tableVillage_users = tableVillage + "_users";

		new BukkitRunnable() {
			@Override
			public void run() {
				shutdown();

				try {
					dataSource = getDataSource();
				} catch (Exception e) {
					if (callback != null) callback.onError(e);
					plugin.getDebug().debug(e);
					return;
				}

				if (dataSource == null) {
					Exception e = new IllegalStateException("Data source is null");
					if (callback != null) callback.onError(e);
					plugin.getDebug().debug(e);
					return;
				}

				try (Connection con = dataSource.getConnection()) {
					try (Statement s = con.createStatement()) {
						s.executeUpdate(getQueryCreateTableVillage());
					}
					try (Statement s = con.createStatement()) {
						s.executeUpdate(getQueryCreateTableUsers());
					}

					try (Statement s = con.createStatement()) {
						ResultSet rs = s.executeQuery("SELECT COUNT(id) FROM " + tableVillage);
						if (rs.next()) {
							int count = rs.getInt(1);
							connected = true;

							plugin.getDebug().debug("Initialized database with " + count + " entries");
							if (callback != null) callback.callSyncResult(count);
						} else {
							throw new SQLException("Count result set has no entries");
						}
					}
				} catch (SQLException e) {
					if (callback != null) callback.callSyncError(e);
					plugin.getLogger().severe("Failed to initialize or connect to database");
					plugin.getDebug().debug(e);
					connected = false;
				}
			}
		}.runTaskAsynchronously(plugin);
	}

	public void update(String sql) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!isConnected()) {
					plugin.getLogger().warning("Cannot execute update, database not connected.");
					return;
				}

				try (Connection con = dataSource.getConnection();
					 Statement stmt = con.createStatement()) {
					stmt.executeUpdate("UPDATE " + tableVillage + " " + sql);
				} catch (SQLException e) {
					plugin.getLogger().log(Level.SEVERE, "Failed to execute update: " + sql, e);
				}
			}
		}.runTaskAsynchronously(plugin);
	}

	public void saveVillageSync(Village village) {
		if (!isConnected()) {
			plugin.getLogger().warning("Cannot save village, database not connected.");
			return;
		}

		String effects_data = village.effectsBuyedToString();
		String effects_active = village.effectsActiveToString();
		String ints_data = village.intsToString();

		try (Connection connection = dataSource.getConnection();
			 PreparedStatement statement = connection.prepareStatement(
					 "UPDATE `" + tableVillage + "` SET `tp`=?, `effects_data`=?,`effects_active`=?, `ints_data`=?, `tag`=? WHERE `village_name`=?")) {

			statement.setString(1, LocationUtils.convertLocactionToString(village.getTeleport()));
			statement.setString(2, effects_data);
			statement.setString(3, effects_active);
			statement.setString(4, ints_data);
			statement.setString(5, village.getTag());
			statement.setString(6, village.getVillageName());
			statement.executeUpdate();
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "Failed to save village: " + village.getVillageName(), e);
		}
	}

	public void saveUserSync(VillageMember villageMember) {
		if (!isConnected()) {
			plugin.getLogger().warning("Cannot save village, database not connected.");
			return;
		}

		try (Connection connection = dataSource.getConnection();
			 PreparedStatement statement = connection.prepareStatement(
					 "UPDATE `" + tableVillage_users + "` SET `village_name`=?, `village_permissions`=? WHERE `uuid`=?")) {

			statement.setString(1, villageMember.getVillage().getVillageName());
			statement.setString(2, plugin.getPermissionManager().toString(villageMember.getPermissions()));
			statement.setString(3, villageMember.getUuid().toString());
			statement.executeUpdate();
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "Failed to save user: " + villageMember.getName(), e);
		}
	}

	public boolean isConnected() {
		return dataSource != null || connected;
	}

	public Connection getConnection() throws SQLException {
		if (!isConnected()) throw new SQLException("Datasource is not initialized");
		return dataSource.getConnection();
	}

	public void shutdown() {
		if (dataSource != null) {
			try {
				dataSource.close();
			} catch (Throwable t) {
				plugin.getLogger().log(Level.WARNING, "Error closing datasource", t);
			} finally {
				dataSource = null;
			}
		}
	}

	public enum DatabaseType {
		SQLITE, SQL, MYSQL
	}
}
