package pl.kiosel.villages.storage.migrations;

import pl.kiosel.core.database.DataMigration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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
    }
}
