package pl.kiosel.villages.storage;

import lombok.Getter;
import pl.kiosel.rosacore.database.DatabaseTable;
import pl.kiosel.rosacore.database.DatabaseValues;
import pl.kiosel.villages.AdvancedVillages;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public final class VillageRoleStorage {

	private static final DatabaseTable TABLE = DatabaseTable.named("village_role_permissions");

	private final AdvancedVillages plugin;

	public VillageRoleStorage(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.ensureTable();
	}

	private void ensureTable() {
		this.plugin.getDataManager().withSession(session -> session.executeUpdate(
				"CREATE TABLE IF NOT EXISTS " + session.tableName(TABLE) + " ("
						+ "`village_uuid` VARCHAR(100) NOT NULL, "
						+ "`role_id` VARCHAR(64) NOT NULL, "
						+ "`permissions` TEXT NOT NULL, "
						+ "PRIMARY KEY (`village_uuid`, `role_id`))"));
	}

	public List<StoredRole> load() {
		List<StoredRole> roles = new ArrayList<>();
		this.plugin.getDataManager().withSession(session ->
				session.queryEach("SELECT * FROM " + session.tableName(TABLE), row -> {
					String villageId = row.getString("village_uuid");
					try {
						roles.add(new StoredRole(UUID.fromString(villageId),
								row.getString("role_id"), row.getString("permissions")));
					} catch (RuntimeException exception) {
						this.plugin.getRosaLogger().log(Level.WARNING,
								"Ignoring invalid role permissions for village " + villageId, exception);
					}
				})
		);
		return roles;
	}

	public void save(UUID villageId, String roleId, String permissions) {
		this.plugin.getDataManager().getDatabase().upsert(TABLE, DatabaseValues.create()
				.set("village_uuid", villageId)
				.set("role_id", roleId)
				.set("permissions", permissions == null ? "" : permissions), "village_uuid", "role_id");
	}

	public void delete(UUID villageId, String roleId) {
		this.plugin.getDataManager().withSession(session -> session.executeUpdate(
				"DELETE FROM " + session.tableName(TABLE) + " WHERE village_uuid = ? AND role_id = ?",
				villageId.toString(),
				roleId
		));
	}

	public void delete(UUID villageId) {
		this.plugin.getDataManager().getDatabase().delete(TABLE, "village_uuid", villageId);
	}

	@Getter
	public static final class StoredRole {

		private final UUID villageId;
		private final String roleId;
		private final String permissions;

		private StoredRole(UUID villageId, String roleId, String permissions) {
			this.villageId = villageId;
			this.roleId = roleId;
			this.permissions = permissions;
		}
	}
}
