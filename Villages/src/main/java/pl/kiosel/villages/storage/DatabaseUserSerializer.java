package pl.kiosel.villages.storage;

import panda.std.Option;
import pl.kiosel.core.CoreLogger;
import pl.kiosel.core.data.element.SQLBasicUtils;
import pl.kiosel.core.data.element.SQLNamedStatement;
import pl.kiosel.core.data.element.SQLTable;
import pl.kiosel.core.database.DataManager;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;

import java.sql.ResultSet;

public final class DatabaseUserSerializer {

    private DatabaseUserSerializer() {
    }

    public static Option<User> deserialize(ResultSet resultSet) {
        if (resultSet == null) {
            return Option.none();
        }

        try {
            String uuid = resultSet.getString("uuid");
            String name = resultSet.getString("name");
            int points = resultSet.getInt("points");
            int kills = resultSet.getInt("kills");
            int deaths = resultSet.getInt("deaths");
            int assists = resultSet.getInt("assists");
            int logouts = resultSet.getInt("logouts");

            Object[] values = new Object[7];
            values[0] = uuid;
            values[1] = name;
            values[2] = points;
            values[3] = kills;
            values[4] = deaths;
            values[5] = assists;
            values[6] = logouts;

            return DeserializationUtils.deserializeUser(AdvancedVillages.getInstance().getUserManager(), values);
        } catch (Exception exception) {
            CoreLogger.getInstance().warning("Could not deserialize user" + exception);
        }

        return Option.none();
    }

    public static void serialize(User user) {
		AdvancedVillages plugin = AdvancedVillages.getInstance();
		plugin.getDataHelper().insertUser(user);
        user.markUnchanged();
    }

    public static void updatePoints(User user) {
		AdvancedVillages plugin = AdvancedVillages.getInstance();
		DataManager connection = plugin.getDataManager();
		SQLTable userstable = plugin.getDataloader().getUsersTable();
        SQLNamedStatement statement = SQLBasicUtils.getUpdate(connection, userstable, userstable.getSQLElement("points").orNull());

        statement.set("points", user.getRank().getPoints());
        statement.set("uuid", user.getUUID().toString());
        statement.executeUpdate();
    }

}
