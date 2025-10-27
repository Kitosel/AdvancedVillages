package pl.kiosel.villages.storage;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.bukkit.scheduler.BukkitRunnable;

import pl.kiosel.villages.Wioski;
import pl.kiosel.villages.config.Config;

public class MySQL extends Database {

    public MySQL(Wioski plugin) {
        super(plugin);
    }

    @Override
    HikariDataSource getDataSource() {
        HikariConfig config = new HikariConfig();
		config.setMaximumPoolSize(10);
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s?autoReconnect=true&useSSL=false&serverTimezone=UTC",
                Config.storage_mysql_host, Config.storage_mysql_port, Config.storage_mysql_database));
        config.setUsername(Config.storage_mysql_user);
        config.setPassword(Config.storage_mysql_pass);
        config.setConnectionTestQuery("SELECT 1");

        return new HikariDataSource(config);
    }

	public void ping() {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!isConnected()) {
					plugin.getLogger().warning("Cannot ping MySQL, data source is not connected.");
					return;
				}

				try (Connection con = dataSource.getConnection();
					 Statement s = con.createStatement()) {
					plugin.getDebug().debug("Pinging MySQL server...");
					s.execute("/* ping */ SELECT 1");
				} catch (SQLException ex) {
					plugin.getLogger().severe("Failed to ping MySQL server. Reconnecting...");
					plugin.getDebug().debug("Failed to ping MySQL server:", ex);
					connect(null); // ponowne połączenie
				}
			}
		}.runTaskAsynchronously(plugin);
	}

	@Override
	String getQueryCreateTableVillage() {
		return "CREATE TABLE IF NOT EXISTS `" + tableVillage + "` (" +
				"`id` INT NOT NULL AUTO_INCREMENT, " +
				"`owner` VARCHAR(255) NOT NULL, " +
				"`village_name` VARCHAR(255) NOT NULL, " +
				"`location` TEXT NOT NULL, " +
				"`tp` TEXT NOT NULL, " +
				"`members` TEXT NOT NULL, " +
				"`effects_data` TEXT NOT NULL, " +
				"`effects_active` TEXT NOT NULL, " +
				"`ints_data` TEXT NOT NULL, " +
				"`settings` TEXT NOT NULL, " +
				"`tag` TEXT NOT NULL, " +
				"PRIMARY KEY (`id`)) ENGINE=InnoDB;";
	}

	@Override
	String getQueryCreateTableUsers() {
		return "CREATE TABLE IF NOT EXISTS `" + tableVillage_users + "` (" +
				"`id` INT NOT NULL AUTO_INCREMENT, " +
				"`name` VARCHAR(255) NOT NULL, " +
				"`uuid` VARCHAR(255) NOT NULL, " +
				"`village_name` VARCHAR(255) NULL, " +
				"`village_permissions` VARCHAR(255) NULL, " +
				"PRIMARY KEY (`id`)) ENGINE=InnoDB;";
	}
}