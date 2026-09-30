package pl.kiosel.villages.storage.migrations;

import pl.kiosel.rosacore.database.DatabaseMigration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class _2_FeaturesMigration extends DatabaseMigration {

	public _2_FeaturesMigration() {
		super(2);
	}

	@Override
	public void migrate(Connection connection, String tablePrefix) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			//upkeep table
			statement.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "village_upkeep (" +
					"`village_uuid` VARCHAR(100) NOT NULL, " +
					"`next_payment` BIGINT NOT NULL, " +
					"`missed_payments` INT NOT NULL, " +
					"`automatic_payment` BOOLEAN NOT NULL, " +
					"PRIMARY KEY (`village_uuid`));");

			//permission/roles table
			statement.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "village_role_permissions (" +
					"`village_uuid` VARCHAR(100) NOT NULL, " +
					"`role_id` VARCHAR(64) NOT NULL, " +
					"`permissions` TEXT NOT NULL, " +
					"PRIMARY KEY (`village_uuid`, `role_id`));");
		}
	}
}
