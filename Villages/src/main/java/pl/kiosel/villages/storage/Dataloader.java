package pl.kiosel.villages.storage;

import lombok.Getter;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.UserValidator;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageManager;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;

public class Dataloader {

	@Getter private final String usersTable;
	@Getter private final String villagesTable;
	private final AdvancedVillages plugin;

	public Dataloader(AdvancedVillages plugin) {
		this.plugin = plugin;

		this.usersTable = plugin.getDataManager().getTablePrefix() + "users";
		this.villagesTable = plugin.getDataManager().getTablePrefix() + "villages";
	}

	public void load() {
		this.loadUsers();
		this.loadVillage();
		this.plugin.getQuestManager().load();
		this.plugin.getLogManager().load();
		this.plugin.getDiplomacyManager().load();
		this.plugin.getDevelopmentManager().load();
		this.plugin.getUpkeepManager().load();
	}

	public void loadUsers() {
		this.plugin.getDataManager().withConnection(connection -> {
			try (Statement statement = connection.createStatement();
				 ResultSet result = statement.executeQuery("SELECT * FROM " + this.usersTable)) {
				while (result.next()) {
					String userName = result.getString("name");
					if (UserValidator.validateUsername(userName) != UserValidator.NameResult.VALID) {
						plugin.getDebug().debug("Skipping loading of user '" + userName + "' - name is invalid");
						continue;
					}
					DatabaseUserSerializer.deserialize(result);
				}
			}
		});
		plugin.getDebug().debug("Loaded users: " + this.plugin.getUserManager().countUsers());
	}

	public void loadVillage() {
		plugin.getDebug().debug("Loading villages");
		VillageManager villageManager = this.plugin.getVillageManager();

		this.plugin.getDataManager().withConnection(connection -> {
			try (Statement statement = connection.createStatement();
				 ResultSet result = statement.executeQuery("SELECT * FROM " + this.villagesTable)) {
				while (result.next()) {
					plugin.getDebug().debug("Loading " + result.getString("name"));
					DatabaseVillageSerializer.deserialize(result);
				}
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
					this.plugin.getRosaLogger().log(Level.SEVERE,
							"Could not save user " + user.getUUID(), exception);
				}
			}
		}

		for (Village village : this.plugin.getVillageManager().getVillages()) {
			if (!ignoreNotChanged || village.wasChanged()) {
				try {
					DatabaseVillageSerializer.serialize(village);
				} catch (RuntimeException exception) {
					this.plugin.getRosaLogger().log(Level.SEVERE,
							"Could not save village " + village.getUUID(), exception);
				}
			}
		}

		this.plugin.getQuestManager().save(ignoreNotChanged);
		this.plugin.getLogManager().save();
		this.plugin.getDiplomacyManager().save(ignoreNotChanged);
		this.plugin.getDevelopmentManager().save(ignoreNotChanged);
		this.plugin.getUpkeepManager().save(ignoreNotChanged);
	}
}
