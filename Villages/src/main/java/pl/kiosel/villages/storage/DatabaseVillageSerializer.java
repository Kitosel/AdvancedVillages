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

    public static void deserialize(ResultSet resultSet) {
        if (resultSet == null) {
            return;
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
                return;
            }

            if (tag == null) {
				plugin.getRosaLogger().severe("Cannot deserialize village: " + name + ", caused by: tag is null");
                return;
            }

            if (os == null) {
				plugin.getRosaLogger().severe("Cannot deserialize village: " + name + ", caused by: owner is null");
                return;
            }

            UUID uuid = UUID.randomUUID();
            if (!TextUtils.isEmpty(id)) {
                uuid = UUID.fromString(id);
            }

            Optional<User> ownerOption = userManager.findByName(os);
            if (ownerOption.isEmpty()) {
				plugin.getRosaLogger().severe("Cannot deserialize village! Caused by: owner (user instance) doesn't exist");
                return;
            }

            Set<User> members = new HashSet<>();
            if (membersString != null && !membersString.isEmpty()) {
                members = userManager.findByNames(TextUtils.fromString(membersString));
            }

            if (protection == null) {
				plugin.getRosaLogger().severe("Cannot deserialize village: " + name + ", caused by: protection is null");
                return;
            }

            if (lives == 0) {
                lives = 3;
            }

			VillageData data = new VillageData(
					uuid,
					name,
					ownerOption.get(),
					LocationUtils.getLocationFromString(loc),
					LocationUtils.getLocationFromString(tp),
					members,
					pvp,
					lives,
					bank,
					level,
					effects_data,
					effects_active,
					protection,
					tag,
					tnt,
					trails
			);
			DeserializationUtils.deserializeVillage(plugin.getVillageManager(), data);
		} catch (Exception exception) {
			AdvancedVillages.getInstance().getRosaLogger().log(Level.WARNING,
					"Could not deserialize village (id: " + id + ", name: " + name + ")", exception);
        }

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
