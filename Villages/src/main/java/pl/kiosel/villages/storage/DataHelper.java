package pl.kiosel.villages.storage;

import pl.kiosel.rosacore.database.DatabaseManager;
import pl.kiosel.rosacore.database.DatabaseTable;
import pl.kiosel.rosacore.database.DatabaseValues;
import pl.kiosel.rosacore.location.LocationUtils;
import pl.kiosel.rosacore.utils.TextUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.VEntity;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;

public class DataHelper {

	private static final DatabaseTable VILLAGES = DatabaseTable.named("villages");

	private final AdvancedVillages plugin;

	public DataHelper(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public void deleteVillage(Village village) {
		if (this.plugin.getQuestManager() != null) this.plugin.getQuestManager().delete(village);
		if (this.plugin.getDiplomacyManager() != null) this.plugin.getDiplomacyManager().removeVillage(village);
		if (this.plugin.getLogManager() != null) this.plugin.getLogManager().delete(village);
		if (this.plugin.getDevelopmentManager() != null) this.plugin.getDevelopmentManager().delete(village);
		if (this.plugin.getUpkeepManager() != null) this.plugin.getUpkeepManager().delete(village);

		this.database().delete(VILLAGES, "uuid", village.getUUID());
	}

	public void insertUser(User user) {
		DatabaseManager database = database();
		database.upsert(DatabaseTable.named("users"), DatabaseValues.create()
				.set("uuid", user.getUUID())
				.set("name", user.getName())
				.set("points", user.getRank().getPoints())
				.set("kills", user.getRank().getKills())
				.set("deaths", user.getRank().getDeaths())
				.set("assists", user.getRank().getAssists())
				.set("logouts", user.getRank().getLogouts())
				.set("permission", this.plugin.getRoleManager().serialize(user)), "uuid");

		this.plugin.getDebug().debug("Saved user: " + user.getName());
	}

	public void insertVillage(Village village) {
		String members = TextUtils.join(VEntity.names(village.getMembers()));
		DatabaseManager database = database();
		database.upsert(VILLAGES, DatabaseValues.create()
				.set("uuid", village.getUUID())
				.set("name", village.getName())
				.set("owner", village.getOwner().getName())
				.set("location", LocationUtils.convertLocationToString(village.getLocation().get()))
				.set("tp", LocationUtils.convertLocationToString(village.getHome().get()))
				.set("members", members)
				.set("pvp", village.hasPvPEnabled())
				.set("tnt", village.hasTntEnabled())
				.set("trails", village.isAnimationsEnabled())
				.set("points", village.getRank().getAveragePoints())
				.set("lives", village.getLives())
				.set("bank", village.getBank())
				.set("level", village.getLevel().getLevel())
				.set("effects_data", village.effectsBuyedToString())
				.set("effects_active", village.effectsActiveToString())
				.set("protection", village.getProtection())
				.set("tag", village.getTag()), "uuid");

		this.plugin.getDebug().debug("Saved village: " + village.getName());
	}

	private DatabaseManager database() {
		return this.plugin.getDataManager().getDatabase();
	}
}
