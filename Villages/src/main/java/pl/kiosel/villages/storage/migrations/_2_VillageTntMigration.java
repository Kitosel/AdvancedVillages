package pl.kiosel.villages.storage.migrations;

import pl.kiosel.core.database.DataMigration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class _2_VillageTntMigration extends DataMigration {

	public _2_VillageTntMigration() {
		super(2);
	}

	@Override
	public void migrate(Connection connection, String tablePrefix) throws SQLException {
		String tableName = tablePrefix + "villages";
		if (hasColumn(connection, tableName, "tnt")) {
			return;
		}

		try (Statement statement = connection.createStatement()) {
			statement.execute("ALTER TABLE " + tableName + " ADD COLUMN `tnt` BOOLEAN NOT NULL DEFAULT TRUE");
		}
	}

	private boolean hasColumn(Connection connection, String tableName, String columnName) throws SQLException {
		try (Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName + " WHERE 1 = 0")) {
			ResultSetMetaData metadata = resultSet.getMetaData();
			for (int index = 1; index <= metadata.getColumnCount(); index++) {
				if (metadata.getColumnName(index).equalsIgnoreCase(columnName)) {
					return true;
				}
			}
		}
		return false;
	}
}
