package pl.kiosel.villages.storage.migrations;

import pl.kiosel.rosacore.database.DatabaseMigration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class _5_OutpostsMigration extends DatabaseMigration {

	public _5_OutpostsMigration() {
		super(5);
	}

	@Override
	public void migrate(Connection connection, String tablePrefix) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "village_outposts (" +
					"`uuid` VARCHAR(36) NOT NULL, " +
					"`village_uuid` VARCHAR(100) NOT NULL, " +
					"`name` VARCHAR(64) NOT NULL, " +
					"`location` TEXT NOT NULL, " +
					"`level` INT NOT NULL, " +
					"`created_at` BIGINT NOT NULL, " +
					"PRIMARY KEY (`uuid`));");
		}
	}
}
