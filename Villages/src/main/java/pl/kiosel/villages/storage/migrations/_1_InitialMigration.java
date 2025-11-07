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
					"PRIMARY KEY (`id`));");
        }

		// Create users table.
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "users (" +
					"`id` INT NOT NULL AUTO_INCREMENT, " +
					"`name` VARCHAR(255) NOT NULL, " +
					"`uuid` VARCHAR(255) NOT NULL, " +
					"`village_name` VARCHAR(255) NULL, " +
					"`village_permissions` VARCHAR(255) NULL, " +
					"PRIMARY KEY (`id`));");
        }
    }
}