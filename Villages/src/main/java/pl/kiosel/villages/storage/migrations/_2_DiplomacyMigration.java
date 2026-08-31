package pl.kiosel.villages.storage.migrations;

import pl.kiosel.rosacore.database.DatabaseMigration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class _2_DiplomacyMigration extends DatabaseMigration {

	public _2_DiplomacyMigration() {
		super(2);
	}

	@Override
	public void migrate(Connection connection, String tablePrefix) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "village_alliances (" +
					"`id` VARCHAR(36) NOT NULL, " +
					"`first_uuid` VARCHAR(36) NOT NULL, " +
					"`second_uuid` VARCHAR(36) NOT NULL, " +
					"`created_at` BIGINT NOT NULL, " +
					"PRIMARY KEY (`id`));");

			statement.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "village_alliance_requests (" +
					"`id` VARCHAR(36) NOT NULL, " +
					"`sender_uuid` VARCHAR(36) NOT NULL, " +
					"`target_uuid` VARCHAR(36) NOT NULL, " +
					"`created_at` BIGINT NOT NULL, " +
					"`expires_at` BIGINT NOT NULL, " +
					"PRIMARY KEY (`id`));");

			statement.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "village_wars (" +
					"`id` VARCHAR(36) NOT NULL, " +
					"`attacker_uuid` VARCHAR(36) NOT NULL, " +
					"`defender_uuid` VARCHAR(36) NOT NULL, " +
					"`declared_at` BIGINT NOT NULL, " +
					"`starts_at` BIGINT NOT NULL, " +
					"`scheduled_ends_at` BIGINT NOT NULL, " +
					"`attacker_score` INT NOT NULL, " +
					"`defender_score` INT NOT NULL, " +
					"`ended_at` BIGINT NULL, " +
					"`cooldown_until` BIGINT NULL, " +
					"`winner_uuid` VARCHAR(36) NULL, " +
					"`end_reason` VARCHAR(32) NULL, " +
					"PRIMARY KEY (`id`));");
		}
	}
}
