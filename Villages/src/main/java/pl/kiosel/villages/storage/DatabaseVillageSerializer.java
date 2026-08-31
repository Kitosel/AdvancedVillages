package pl.kiosel.villages.storage;

import pl.kiosel.rosacore.location.LocationUtils;
import pl.kiosel.rosacore.utils.TextUtils;
import pl.kiosel.rosacore.utils.TimeUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.UserManager;
import pl.kiosel.villages.data.village.Village;

import java.sql.ResultSet;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public final class DatabaseVillageSerializer {

    private DatabaseVillageSerializer() {
    }

    public static Optional<Village> deserialize(ResultSet resultSet) {
        if (resultSet == null) {
            return Optional.empty();
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
            UserManager userManager = plugin.getUserManager();

            if (name == null) {
				plugin.getRosaLogger().severe("Cannot deserialize village, caused by: name is null");
                return Optional.empty();
            }

            if (tag == null) {
				plugin.getRosaLogger().severe("Cannot deserialize village: " + name + ", caused by: tag is null");
                return Optional.empty();
            }

            if (os == null) {
				plugin.getRosaLogger().severe("Cannot deserialize village: " + name + ", caused by: owner is null");
                return Optional.empty();
            }

            UUID uuid = UUID.randomUUID();
            if (!TextUtils.isEmpty(id)) {
                uuid = UUID.fromString(id);
            }

            Optional<User> ownerOption = userManager.findByName(os);
            if (ownerOption.isEmpty()) {
				plugin.getRosaLogger().severe("Cannot deserialize village! Caused by: owner (user instance) doesn't exist");
                return Optional.empty();
            }

            Set<User> members = new HashSet<>();
            if (membersString != null && !membersString.isEmpty()) {
                members = userManager.findByNames(TextUtils.fromString(membersString));
            }

            if (protection == null) {
				plugin.getRosaLogger().severe("Cannot deserialize village: " + name + ", caused by: protection is null");
                return Optional.empty();
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
			AdvancedVillages.getInstance().getRosaLogger().log(Level.WARNING,
					"Could not deserialize village (id: " + id + ", name: " + name + ")", exception);
        }

        return Optional.empty();
    }

    public static void serialize(Village village) {
		AdvancedVillages plugin = AdvancedVillages.getInstance();
		long changeVersion = village.getChangeVersion();
		plugin.getDataHelper().insertVillage(village);
		village.markUnchanged(changeVersion);
    }

	public static void delete(Village village) {
		AdvancedVillages plugin = AdvancedVillages.getInstance();
		plugin.getDataManager().executeUpdate(
				"DELETE FROM " + plugin.getDataloader().getVillagesTable() + " WHERE uuid = ?",
				village.getUUID().toString());
    }

    public static void updatePoints(Village village) {
		AdvancedVillages plugin = AdvancedVillages.getInstance();
		plugin.getDataManager().executeUpdate(
				"UPDATE " + plugin.getDataloader().getVillagesTable() + " SET points = ? WHERE uuid = ?",
				village.getRank().getAveragePoints(), village.getUUID().toString());
    }

}
