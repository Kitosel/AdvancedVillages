package pl.kiosel.villages.storage;

import panda.std.Option;
import pl.kiosel.core.CoreLogger;
import pl.kiosel.core.data.element.SQLBasicUtils;
import pl.kiosel.core.data.element.SQLNamedStatement;
import pl.kiosel.core.data.element.SQLTable;
import pl.kiosel.core.database.DataManager;
import pl.kiosel.core.utils.LocationUtils;
import pl.kiosel.core.utils.TextUtils;
import pl.kiosel.core.utils.TimeUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.UserManager;
import pl.kiosel.villages.data.village.Village;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public final class DatabaseVillageSerializer {

    private DatabaseVillageSerializer() {
    }

    public static Option<Village> deserialize(ResultSet resultSet) {
        if (resultSet == null) {
            return Option.none();
        }

        String id = null;
        String name = null;

        try {
            id = resultSet.getString("uuid");
            name = resultSet.getString("name");
			String os = resultSet.getString("owner");
			String loc = resultSet.getString("location");
			String tp = resultSet.getString("tp");
            String membersString = resultSet.getString("members");
			boolean pvp = resultSet.getBoolean("pvp");
			boolean tnt = resultSet.getBoolean("tnt");
			boolean trails = resultSet.getBoolean("trails");
			int lives = resultSet.getInt("lives");
			int bank = resultSet.getInt("bank");
			int level = resultSet.getInt("level");
			String effects_data = resultSet.getString("effects_data");
			String effects_active = resultSet.getString("effects_active");
            Instant protection = TimeUtils.positiveOrNullInstant(resultSet.getLong("protection"));
            String tag = resultSet.getString("tag");

            AdvancedVillages plugin = AdvancedVillages.getInstance();
            CoreLogger logger = CoreLogger.getInstance();
            UserManager userManager = plugin.getUserManager();

            if (name == null) {
                logger.severe("Cannot deserialize village, caused by: name is null");
                return Option.none();
            }

            if (tag == null) {
                logger.severe("Cannot deserialize village: " + name + ", caused by: tag is null");
                return Option.none();
            }

            if (os == null) {
                logger.severe("Cannot deserialize village: " + name + ", caused by: owner is null");
                return Option.none();
            }

            UUID uuid = UUID.randomUUID();
            if (!TextUtils.isEmpty(id)) {
                uuid = UUID.fromString(id);
            }

            Option<User> ownerOption = userManager.findByName(os);
            if (ownerOption.isEmpty()) {
                logger.severe("Cannot deserialize village! Caused by: owner (user instance) doesn't exist");
                return Option.none();
            }

            Set<User> members = new HashSet<>();
            if (membersString != null && !membersString.isEmpty()) {
                members = userManager.findByNames(TextUtils.fromString(membersString));
            }

            if (protection == null) {
                logger.severe("Cannot deserialize village: " + name + ", caused by: protection is null");
                return Option.none();
            }

            if (lives == 0) {
                lives = 3;
            }

			Object[] values = new Object[16];
            values[0] = uuid;
            values[1] = name;
            values[2] = ownerOption.get();
			values[3] = LocationUtils.getLocationFromString(loc);
			values[4] = LocationUtils.getLocationFromString(tp);
            values[5] = members;
            values[6] = pvp;
            values[7] = lives;
			values[8] = bank;
			values[9] = level;
			values[10] = effects_data;
            values[11] = effects_active;
            values[12] = protection;
            values[13] = tag;
			values[14] = tnt;
			values[15] = trails;

            return DeserializationUtils.deserializeVillage(plugin.getVillageManager(), values);
        } catch (Exception exception) {
			AdvancedVillages.getInstance().getLogger().log(Level.WARNING,
					"Could not deserialize village (id: " + id + ", name: " + name + ")", exception);
        }

        return Option.none();
    }

    public static void serialize(Village village) {
		AdvancedVillages plugin = AdvancedVillages.getInstance();
		long changeVersion = village.getChangeVersion();
		plugin.getDataHelper().insertVillage(village);
		village.markUnchanged(changeVersion);
    }

    public static void delete(Village village, DataManager connection, SQLTable villageTable) throws SQLException {
        SQLNamedStatement statement = SQLBasicUtils.getDelete(connection, villageTable);

        statement.set("uuid", village.getUUID().toString());
        statement.executeUpdate();
    }

    public static void updatePoints(Village village) {
		AdvancedVillages plugin = AdvancedVillages.getInstance();
		DataManager connection = plugin.getDataManager();
		SQLTable villageTable = plugin.getDataloader().getVillagesTable();
        SQLNamedStatement statement = SQLBasicUtils.getUpdate(connection, villageTable, villageTable.getSQLElement("points").orNull());

        statement.set("points", village.getRank().getAveragePoints());
        statement.set("uuid", village.getUUID().toString());
        statement.executeUpdate();
    }

}
