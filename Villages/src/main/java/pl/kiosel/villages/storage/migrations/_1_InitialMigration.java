package pl.kiosel.villages.storage.migrations;

import pl.kiosel.core.database.DataMigration;

import java.sql.*;

public class _1_InitialMigration extends DataMigration {

    public _1_InitialMigration() {
        super(1);
    }

    @Override
    public void migrate(Connection connection, String tablePrefix) throws SQLException {
        // Create villages table.
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "villages (" +
					"`uuid` VARCHAR(100) NOT NULL, " +
					"`name` VARCHAR(255) NOT NULL, " +
					"`owner` VARCHAR(255) NOT NULL, " +
					"`location` TEXT NOT NULL, " +
					"`tp` TEXT NOT NULL, " +
					"`members` TEXT NOT NULL, " +
					"`pvp` BOOLEAN NOT NULL, " +
					"`tnt` BOOLEAN NOT NULL, " +
					"`trails` BOOLEAN NOT NULL DEFAULT TRUE, " +
					"`lives` INT NOT NULL, " +
					"`bank` INT NOT NULL, " +
					"`level` INT NOT NULL, " +
					"`points` INT NOT NULL, " +
					"`effects_data` TEXT NOT NULL, " +
					"`effects_active` TEXT NOT NULL, " +
					"`protection` BIGINT NOT NULL, " +
					"`tag` VARCHAR(64) NOT NULL, " +
					"PRIMARY KEY (`uuid`));");
        }

		// Create users table.
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "users (" +
					"`uuid` VARCHAR(36) NOT NULL, " +
					"`name` VARCHAR(255) NOT NULL, " +
					"`points` INT NULL, " +
					"`kills` INT NULL, " +
					"`deaths` INT NULL, " +
					"`assists` INT NULL, " +
					"`logouts` INT NULL, " +
					"`permission` TEXT NULL, " +
					"PRIMARY KEY (`uuid`));");
        }

		//Create quest table
		try (Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "village_quests (" +
					"`village_uuid` VARCHAR(100) NOT NULL, " +
					"`daily_period` VARCHAR(16) NOT NULL, " +
					"`weekly_period` VARCHAR(16) NOT NULL, " +
					"`daily_progress` TEXT NOT NULL, " +
					"`weekly_progress` TEXT NOT NULL, " +
					"`daily_completed` TEXT NOT NULL, " +
					"`weekly_completed` TEXT NOT NULL, " +
					"`daily_active` TEXT NULL, " +
					"`weekly_active` TEXT NULL, " +
					"PRIMARY KEY (`village_uuid`));");
		}

		//Create logs table
		String table = tablePrefix + "village_logs";
		try (Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE IF NOT EXISTS " + table + " (" +
					"`id` VARCHAR(36) NOT NULL, " +
					"`village_uuid` VARCHAR(100) NOT NULL, " +
					"`type` VARCHAR(48) NOT NULL, " +
					"`actor_uuid` VARCHAR(36) NULL, " +
					"`actor_name` VARCHAR(64) NOT NULL, " +
					"`created_at` BIGINT NOT NULL, " +
					"`details` TEXT NOT NULL, " +
					"PRIMARY KEY (`id`));");

			if (!hasVillageTimeIndex(connection, table)) {
				statement.execute("CREATE INDEX village_logs_village_time_idx ON " + table + " (`village_uuid`, `created_at`);");
			}
		}
    }

	private boolean hasVillageTimeIndex(Connection connection, String table) throws SQLException {
		DatabaseMetaData metadata = connection.getMetaData();
		try (ResultSet indexes = metadata.getIndexInfo(connection.getCatalog(), null, table, false, false)) {
			while (indexes.next()) {
				if ("village_uuid".equalsIgnoreCase(indexes.getString("COLUMN_NAME"))) {
					return true;
				}
			}
		}
		return false;
	}
}
