package pl.kiosel.villages.storage;

import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;

import java.sql.ResultSet;
import java.util.Optional;

public final class DatabaseUserSerializer {

    private DatabaseUserSerializer() {
    }

    public static Optional<User> deserialize(ResultSet resultSet) {
        if (resultSet == null) {
            return Optional.empty();
        }

        try {
            String uuid = resultSet.getString("uuid");
            String name = resultSet.getString("name");
            int points = resultSet.getInt("points");
            int kills = resultSet.getInt("kills");
            int deaths = resultSet.getInt("deaths");
            int assists = resultSet.getInt("assists");
            int logouts = resultSet.getInt("logouts");
			String permissions = resultSet.getString("permission");

            Object[] values = new Object[8];
            values[0] = uuid;
            values[1] = name;
            values[2] = points;
            values[3] = kills;
            values[4] = deaths;
            values[5] = assists;
            values[6] = logouts;
			values[7] = permissions;

            return DeserializationUtils.deserializeUser(AdvancedVillages.getInstance().getUserManager(), values);
        } catch (Exception exception) {
			AdvancedVillages.getInstance().getRosaLogger().warning("Could not deserialize user: " + exception.getMessage());
        }

        return Optional.empty();
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
