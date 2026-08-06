package pl.kiosel.villages.storage;

import pl.kiosel.core.utils.LocationUtils;
import pl.kiosel.core.utils.TextUtils;
import pl.kiosel.dependencies.org.jooq.impl.DSL;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.Entity;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;

import java.util.*;

public class DataHelper {

	private final AdvancedVillages plugin;
	private final String prefix;

	public DataHelper(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.prefix = plugin.getDataManager().getTablePrefix();
	}

	public void deleteVillage(Village village) {
		plugin.getDataManager().getDatabaseConnector().connectDSL(dslContext -> {
			dslContext.deleteFrom(DSL.table(prefix + "villages"))
					.where(DSL.field("village_name").eq(village.getName()))
					.execute();
		});
	}

	public void insertUser(User user) {
		plugin.getDataManager().getDatabaseConnector().connectDSL(dsl -> {
			String table = prefix + "users";
			String uuid = user.getUUID().toString();

			boolean exists = dsl.fetchExists(
					dsl.selectOne()
							.from(table)
							.where(DSL.field("uuid").eq(uuid))
			);

			if (exists) {
				dsl.update(DSL.table(table))
						.set(DSL.field("name"), user.getName())
						.set(DSL.field("points"), user.getRank().getPoints())
						.set(DSL.field("kills"), user.getRank().getKills())
						.set(DSL.field("deaths"), user.getRank().getDeaths())
						.set(DSL.field("assists"), user.getRank().getAssists())
						.set(DSL.field("logouts"), user.getRank().getLogouts())
						.set(DSL.field("permission"), plugin.getPermissionManager().toString(user.getPermissions()))
						.where(DSL.field("uuid").eq(uuid))
						.execute();

				plugin.getDebug().debug("Updated user: " + user.getName());
			} else {
				dsl.insertInto(DSL.table(table))
						.set(DSL.field("uuid"), uuid)
						.set(DSL.field("name"), user.getName())
						.set(DSL.field("points"), user.getRank().getPoints())
						.set(DSL.field("kills"), user.getRank().getKills())
						.set(DSL.field("deaths"), user.getRank().getDeaths())
						.set(DSL.field("assists"), user.getRank().getAssists())
						.set(DSL.field("logouts"), user.getRank().getLogouts())
						.set(DSL.field("permission"), plugin.getPermissionManager().toString(user.getPermissions()))
						.execute();

				plugin.getDebug().debug("Created new user: " + user.getName());
			}
		});
	}

	public void insertVillage(Village village) {
		String members = TextUtils.join(Entity.names(village.getMembers()));
		String effects_data = village.effectsBuyedToString();
		String effects_active = village.effectsActiveToString();

		plugin.getDataManager().getDatabaseConnector().connectDSL(dsl -> {
			String table = prefix + "villages";
			String uuid = village.getUUID().toString();

			boolean exists = dsl.fetchExists(
					dsl.selectOne()
							.from(table)
							.where(DSL.field("uuid").eq(uuid))
			);

			if (exists) {
				dsl.update(DSL.table(table))
						.set(DSL.field("name"), village.getName())
						.set(DSL.field("owner"), village.getOwner().getName())
						.set(DSL.field("location"), LocationUtils.convertLocactionToString(village.getLocation().get()))
						.set(DSL.field("tp"), LocationUtils.convertLocactionToString(village.getHome().get()))
						.set(DSL.field("members"), members)
						.set(DSL.field("pvp"), village.hasPvPEnabled())
						.set(DSL.field("points"), village.getRank().getAveragePoints())
						.set(DSL.field("lives"), village.getLives())
						.set(DSL.field("bank"), village.getBank())
						.set(DSL.field("level"), village.getLevel().getLevel())
						.set(DSL.field("effects_data"), effects_data)
						.set(DSL.field("effects_active"), effects_active)
						.set(DSL.field("protection"), village.getProtection().toEpochMilli())
						.set(DSL.field("tag"), village.getTag())
						.where(DSL.field("uuid").eq(uuid))
						.execute();

				plugin.getDebug().debug("Updated village: " + village.getName());
			} else {
				dsl.insertInto(DSL.table(table))
						.set(DSL.field("uuid"), village.getUUID().toString())
						.set(DSL.field("name"), village.getName())
						.set(DSL.field("owner"), village.getOwner().getName())
						.set(DSL.field("location"), LocationUtils.convertLocactionToString(village.getLocation().get()))
						.set(DSL.field("tp"), LocationUtils.convertLocactionToString(village.getHome().get()))
						.set(DSL.field("members"), members)
						.set(DSL.field("pvp"), village.hasPvPEnabled())
						.set(DSL.field("points"), village.getRank().getAveragePoints())
						.set(DSL.field("lives"), village.getLives())
						.set(DSL.field("bank"), village.getBank())
						.set(DSL.field("level"), village.getLevel().getLevel())
						.set(DSL.field("effects_data"), effects_data)
						.set(DSL.field("effects_active"), effects_active)
						.set(DSL.field("protection"), village.getProtection().toEpochMilli())
						.set(DSL.field("tag"), village.getTag())
						.execute();
				plugin.getDebug().debug("Created new village: " + village.getName());
			}
		});
	}
}