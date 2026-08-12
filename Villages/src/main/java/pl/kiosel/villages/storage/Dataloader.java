package pl.kiosel.villages.storage;

import lombok.Getter;
import pl.kiosel.core.data.element.SQLBasicUtils;
import pl.kiosel.core.data.element.SQLTable;
import pl.kiosel.core.data.element.SQLType;
import pl.kiosel.core.database.DataManager;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.UserValidator;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageManager;

import java.util.logging.Level;

public class Dataloader {

	@Getter private final SQLTable usersTable;
	@Getter private final SQLTable villagesTable;
	private final AdvancedVillages plugin;

	public Dataloader(AdvancedVillages plugin) {
		this.plugin = plugin;

		this.usersTable = new SQLTable(plugin.getDataManager().getTablePrefix()+"users");
		this.villagesTable = new SQLTable(plugin.getDataManager().getTablePrefix()+"villages");

		prepareTables();
	}

	public void prepareTables() {
		this.usersTable.add("uuid", SQLType.VARCHAR, 36, true);
		this.usersTable.add("name", SQLType.TEXT, true);
		this.usersTable.add("points", SQLType.INT, true);
		this.usersTable.add("kills", SQLType.INT, true);
		this.usersTable.add("deaths", SQLType.INT, true);
		this.usersTable.add("assists", SQLType.INT, true);
		this.usersTable.add("logouts", SQLType.INT, true);
		this.usersTable.add("permission", SQLType.TEXT);
		this.usersTable.setPrimaryKey("uuid");

		this.villagesTable.add("uuid", SQLType.VARCHAR, 100, true);
		this.villagesTable.add("name", SQLType.TEXT, true);
		this.villagesTable.add("owner", SQLType.TEXT, true);
		this.villagesTable.add("location", SQLType.TEXT, true);
		this.villagesTable.add("tp", SQLType.TEXT, true);
		this.villagesTable.add("members", SQLType.TEXT, true);
		this.villagesTable.add("pvp", SQLType.BOOLEAN, true);
		this.villagesTable.add("tnt", SQLType.BOOLEAN, true);
		this.villagesTable.add("trails", SQLType.BOOLEAN, true);
		this.villagesTable.add("lives", SQLType.INT, true);
		this.villagesTable.add("bank", SQLType.INT, true);
		this.villagesTable.add("level", SQLType.INT, true);
		this.villagesTable.add("points", SQLType.INT, true);
		this.villagesTable.add("effects_data", SQLType.TEXT, true);
		this.villagesTable.add("effects_active", SQLType.TEXT, true);
		this.villagesTable.add("protection", SQLType.BIGINT);
		this.villagesTable.add("tag", SQLType.VARCHAR, 64, true);
		this.villagesTable.setPrimaryKey("uuid");
	}

	public void load(DataManager connection) {
		this.loadUsers(connection);
		this.loadVillage(connection);
		this.plugin.getQuestManager().load();
		this.plugin.getLogManager().load();
	}

	public void loadUsers(DataManager connection) {
		SQLBasicUtils.getSelectAll(connection, usersTable).executeQuery(result -> {
			while (result.next()) {
				String userName = result.getString("name");

				if (UserValidator.validateUsername(userName) != UserValidator.NameResult.VALID) {
					plugin.getDebug().debug("Skipping loading of user '" + userName + "' - name is invalid");
					continue;
				}
				DatabaseUserSerializer.deserialize(result);
			}
		});
		plugin.getDebug().debug("Loaded users: " + this.plugin.getUserManager().countUsers());
	}

	public void loadVillage(DataManager connection) {
		plugin.getDebug().debug("Loading villages");
		VillageManager villageManager = this.plugin.getVillageManager();

		SQLBasicUtils.getSelectAll(connection, villagesTable).executeQuery(resultAll -> {
			while (resultAll.next()) {
				plugin.getDebug().debug("Loading " + resultAll.getString("name"));
				DatabaseVillageSerializer.deserialize(resultAll);
			}
		});

		villageManager.getVillages().stream()
				.filter(village -> village.getOwner() == null)
				.forEach(village -> villageManager.deleteVillage(plugin, village));

		plugin.getDebug().debug("Loaded villages: " + villageManager.countVillage());
	}

	public synchronized void save(boolean ignoreNotChanged) {
		for (User user : this.plugin.getUserManager().getUsers()) {
			if (!ignoreNotChanged || user.wasChanged()) {
				try {
					DatabaseUserSerializer.serialize(user);
				} catch (RuntimeException exception) {
					this.plugin.getLogger().log(Level.SEVERE,
							"Could not save user " + user.getUUID(), exception);
				}
			}
		}

		for (Village village : this.plugin.getVillageManager().getVillages()) {
			if (!ignoreNotChanged || village.wasChanged()) {
				try {
					DatabaseVillageSerializer.serialize(village);
				} catch (RuntimeException exception) {
					this.plugin.getLogger().log(Level.SEVERE,
							"Could not save village " + village.getUUID(), exception);
				}
			}
		}

		this.plugin.getQuestManager().save(ignoreNotChanged);
		this.plugin.getLogManager().save();
	}
}
