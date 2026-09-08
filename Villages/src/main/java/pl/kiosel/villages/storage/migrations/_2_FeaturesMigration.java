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
			//quests table
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

			//logs table
			statement.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "village_logs (" +
					"`id` VARCHAR(36) NOT NULL, " +
					"`village_uuid` VARCHAR(100) NOT NULL, " +
					"`type` VARCHAR(48) NOT NULL, " +
					"`actor_uuid` VARCHAR(36) NULL, " +
					"`actor_name` VARCHAR(64) NOT NULL, " +
					"`created_at` BIGINT NOT NULL, " +
					"`details` TEXT NOT NULL, " +
					"PRIMARY KEY (`id`), " +
					"INDEX `village_logs_village_time_idx` (`village_uuid`, `created_at`));");

			//development table
			statement.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "village_development (" +
					"`village_uuid` VARCHAR(100) NOT NULL, " +
					"`unlocked_nodes` TEXT NOT NULL, " +
					"PRIMARY KEY (`village_uuid`));");

			//upkeep table
			statement.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "village_upkeep (" +
					"`village_uuid` VARCHAR(100) NOT NULL, " +
					"`next_payment` BIGINT NOT NULL, " +
					"`missed_payments` INT NOT NULL, " +
					"PRIMARY KEY (`village_uuid`));");

			//alliances table
			statement.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "village_alliances (" +
					"`id` VARCHAR(36) NOT NULL, " +
					"`first_uuid` VARCHAR(36) NOT NULL, " +
					"`second_uuid` VARCHAR(36) NOT NULL, " +
					"`created_at` BIGINT NOT NULL, " +
					"PRIMARY KEY (`id`));");

			//wars table
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
