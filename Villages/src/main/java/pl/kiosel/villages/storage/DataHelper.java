package pl.kiosel.villages.storage;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import pl.kiosel.core.database.DataManager;
import pl.kiosel.core.utils.LocationUtils;
import pl.kiosel.dependencies.org.jooq.Record;
import pl.kiosel.dependencies.org.jooq.Result;
import pl.kiosel.dependencies.org.jooq.impl.DSL;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageMember;
import pl.kiosel.villages.enums.Permission;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataHelper {

	private final AdvancedVillages plugin;
	private final String prefix;

	public DataHelper(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.prefix = plugin.getDataManager().getTablePrefix();
	}

	public void loadData(DataManager dataManager) {
		List<Village> loadedVillages = new ArrayList<>();

		dataManager.getDatabaseConnector().connectDSL(dslContext -> {
			@NotNull
			Result<Record> resultsVillages = dslContext.select().from(dataManager.getTablePrefix() + "villages").fetch();
			resultsVillages.stream().iterator().forEachRemaining(record -> {
				String owner = record.get("owner").toString();
				Location location = LocationUtils.getLocationFromString(record.get("location").toString());
				Location tp = LocationUtils.getLocationFromString(record.get("tp").toString());
				String villageName = record.get("village_name").toString();
				String effectsData = record.get("effects_data").toString();
				String effectsActive = record.get("effects_active").toString();
				String intsData = record.get("ints_data").toString();
				String tag = record.get("tag").toString();
				List<UUID> members = getUUIDListFromResultSet(record, "members");

				Village village = new Village(owner, location, tp, members, villageName, effectsData, effectsActive, intsData, tag);
				plugin.getVillageDataManager().putVillage(villageName, village);
				loadedVillages.add(village);
			});

			@NotNull Result<Record> resultsUsers = dslContext.select().from(dataManager.getTablePrefix() + "users").fetch();
			resultsUsers.stream().iterator().forEachRemaining(record -> {
				String name = record.get("name").toString();
				UUID uuid = UUID.fromString(record.get("uuid").toString());
				Object vname = record.get("village_name");

				if (vname == null) return;
				String villageName = vname.toString();
				List<Permission> permissions = plugin.getDatabaseUserManager().getPermissionListFromResultSet(record, "village_permissions");

				if (villageName == null || villageName.isEmpty()) return;

				Village village = loadedVillages.stream()
						.filter(v -> v.getVillageName().equalsIgnoreCase(villageName))
						.findFirst()
						.orElse(null);

				if (village == null) {
					plugin.getDebug().debug("⚠ User " + name + " has not existing village: " + villageName);
					return;
				}

				VillageMember member = new VillageMember(uuid, village, permissions);
				plugin.getVillageDataManager().addPlayerMember(uuid, member);
			});
		});
	}

	public void setUserVillage(String village, String permission, String uuid) {
		plugin.getDataManager().getDatabaseConnector().connectDSL(dslContext -> {
			dslContext.update(DSL.table(prefix + "users"))
					.set(DSL.field("village_name"), village)
					.set(DSL.field("village_permissions"), permission)
					.where(DSL.field("uuid").eq(uuid))
					.execute();
		});
	}

	public void createVillage(Village village) {
		plugin.getDataManager().getDatabaseConnector().connectDSL(dslContext -> {
			dslContext.insertInto(DSL.table(prefix + "villages"))
					.columns(DSL.field("owner"),
							DSL.field("village_name"),
							DSL.field("location"),
							DSL.field("tp"),
							DSL.field("members"),
							DSL.field("effects_data"),
							DSL.field("effects_active"),
							DSL.field("ints_data"),
							DSL.field("settings"),
							DSL.field("tag"))
					.values(village.getOwner(), village.getVillageName(),
							LocationUtils.convertLocactionToString(village.getLocation()),
							LocationUtils.convertLocactionToString(village.getTeleport()),
							village.getOwnerUUID() + ";", village.effectsBuyedToString(),
							village.effectsActiveToString(), village.getInts(),
							village.getVillageSettings().convertToString(), village.getTag())
					.execute();
		});
	}

	public void deleteVillage(Village village) {
		plugin.getDataManager().getDatabaseConnector().connectDSL(dslContext -> {
			dslContext.deleteFrom(DSL.table(prefix + "villages"))
					.where(DSL.field("village_name").eq(village.getVillageName()))
					.execute();
		});
	}

	public void updateSettings(String village, String set) {
		plugin.getDataManager().getDatabaseConnector().connectDSL(dslContext -> {
			dslContext.update(DSL.table(prefix + "villages"))
					.set(DSL.field("settings"), set)
					.where(DSL.field("village_name").eq(village))
					.execute();
		});
	}

	public void createUserPlayer(String name, UUID uuid) {
		plugin.getDataManager().getDatabaseConnector().connectDSL(dslContext -> {
			dslContext.insertInto(DSL.table(prefix + "users"))
					.columns(DSL.field("name"), DSL.field("uuid"))
					.values(name, uuid.toString())
					.execute();
		});
	}

	public void deleteUserPlayer(UUID uuid) {
		plugin.getDataManager().getDatabaseConnector().connectDSL(dslContext -> {
			dslContext.deleteFrom(DSL.table(prefix + "users"))
					.where(DSL.field("uuid").eq(uuid.toString()))
					.execute();
		});
	}

	public void saveUser(VillageMember villageMember) {
		plugin.getDataManager().getDatabaseConnector().connectDSL(dslContext -> {
			dslContext.update(DSL.table(prefix + "users"))
					.set(DSL.field("village_name"), villageMember.getVillage().getVillageName())
					.set(DSL.field("village_permissions"), plugin.getPermissionManager().toString(villageMember.getPermissions()))
					.where(DSL.field("uuid").eq(villageMember.getUuid().toString()))
					.execute();
		});
	}

	public void updateMember(String members, String village_name) {
		plugin.getDataManager().getDatabaseConnector().connectDSL(dslContext -> {
			dslContext.update(DSL.table(prefix + "villages"))
					.set(DSL.field("members"), members)
					.where(DSL.field("village_name").eq(village_name))
					.execute();
		});
	}

	public void saveVillageSync(Village village) {
		String effects_data = village.effectsBuyedToString();
		String effects_active = village.effectsActiveToString();
		String ints_data = village.intsToString();

		plugin.getDataManager().getDatabaseConnector().connectDSL(dslContext -> {
			dslContext.update(DSL.table(prefix + "villages"))
					.set(DSL.field("tp"), LocationUtils.convertLocactionToString(village.getTeleport()))
					.set(DSL.field("effects_data"), effects_data)
					.set(DSL.field("effects_active"), effects_active)
					.set(DSL.field("ints_data"), ints_data)
					.set(DSL.field("tag"), village.getTag())
					.where(DSL.field("village_name").eq(village.getVillageName()))
					.execute();
		});
	}


	public List<UUID> getUUIDListFromResultSet(Record rs, String columnName) {
		List<UUID> uuidList = new ArrayList<>();
		String raw = rs.get(columnName).toString();

		if (raw == null || raw.isEmpty())
			return uuidList;

		for (String uuidStr : raw.split(";"))
			try {
				uuidList.add(UUID.fromString(uuidStr));
			} catch (IllegalArgumentException e) {
				plugin.getDebug().debug("Invalid UUID in collum " + columnName + ": " + uuidStr);
			}
		return uuidList;
	}

}