package pl.kiosel.villages.storage;

import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;

import java.sql.ResultSet;
import java.util.UUID;

public final class DatabaseUserSerializer {

    private DatabaseUserSerializer() {
    }

    public static void deserialize(ResultSet resultSet) {
        if (resultSet == null) {
            return;
        }

		try {
			UserData data = new UserData(
					UUID.fromString(resultSet.getString("uuid")),
					resultSet.getString("name"),
					resultSet.getInt("points"),
					resultSet.getInt("kills"),
					resultSet.getInt("deaths"),
					resultSet.getInt("assists"),
					resultSet.getString("role"),
					resultSet.getString("specialization"),
					resultSet.getLong("specialization_changed_at")
			);
			DeserializationUtils.deserializeUser(AdvancedVillages.getInstance().getUserManager(), data);
		} catch (Exception exception) {
			AdvancedVillages.getInstance().getRosaLogger().warning("Could not deserialize user: " + exception.getMessage());
        }

	}

    public static void serialize(User user) {
		AdvancedVillages plugin = AdvancedVillages.getInstance();
		long changeVersion = user.getChangeVersion();
		plugin.getDataHelper().insertUser(user);
		user.markUnchanged(changeVersion);
    }

    public static void updatePoints(User user) {
		AdvancedVillages plugin = AdvancedVillages.getInstance();
		plugin.getDataManager().executeUpdate(
				"UPDATE " + plugin.getDataloader().getUsersTable() + " SET points = ? WHERE uuid = ?",
				user.getRank().getPoints(), user.getUUID().toString());
    }

}
