package pl.kiosel.villages.manager;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.kiosel.core.database.Callback;
import pl.kiosel.dependencies.org.jooq.Record;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.Permission;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageMember;

import java.sql.*;
import java.util.*;

public class DatabaseUserManager {

	private final AdvancedVillages plugin;
	private final String villageTableUsers;

	public DatabaseUserManager(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.villageTableUsers = plugin.getDataManager().getTablePrefix()+"users";
	}

	public void saveUsersAsync(VillageMember villageMember) {
		new BukkitRunnable() {
			@Override
			public void run() {
				saveUsersSync(villageMember);
			}
		}.runTaskAsynchronously(plugin);
	}

	public void saveUsersSync(VillageMember villageMember) {
		plugin.getDataHelper().saveUser(villageMember);
	}

	public void isUserInDatabase(Player player, Callback<Boolean> callback) {
		isUserInDatabase(player.getUniqueId(), callback);
	}

	public void addUserToVillage(VillageMember villageMember, Player player, boolean owner) {
		addUserToVillage(villageMember.getVillage(), player, owner);
	}

	public void addUserToVillage(Village village, Player player, boolean owner) {
		new BukkitRunnable() {
			@Override
			public void run() {
				List<Permission> permissions;

				if (owner) {
					permissions = new ArrayList<>();
					permissions.add(Permission.OWNER);
				} else {
					permissions = plugin.getPermissionManager().getDefaultPermission();
				}
				plugin.getDataHelper().setUserVillage(village.getVillageName(), plugin.getPermissionManager().toString(permissions), player.getUniqueId().toString());

				plugin.getVillageDataManager().addPlayerMember(player.getUniqueId(), new VillageMember(player, village, permissions));
				plugin.getDebug().debug("Added user " + player.getName() + " to village " + village.getVillageName());
			}
		}.runTaskAsynchronously(plugin);
	}

	public void removeUserFromVillage(Village village, UUID uuid) {
		new BukkitRunnable() {
			@Override
			public void run() {
				plugin.getDataHelper().setUserVillage(null, null, uuid.toString());
				plugin.getDebug().debug("Removed user " + uuid.toString() + " from village " + village.getVillageName());
			}
		}.runTaskAsynchronously(plugin);
	}

	public void isUserInDatabase(UUID uuid, Callback<Boolean> callback) {
		new BukkitRunnable() {
			@Override
			public void run() {
				boolean exists = false;
				String query = "SELECT 1 FROM `" + villageTableUsers + "` WHERE uuid=? LIMIT 1";

				try (Connection con = plugin.getDataManager().getDatabaseConnector().getConnection();
					 PreparedStatement stmt = con.prepareStatement(query)) {

					stmt.setString(1, uuid.toString());
					try (ResultSet rs = stmt.executeQuery()) {
						exists = rs.next();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

				boolean finalExists = exists;
				new BukkitRunnable() {
					@Override
					public void run() {
						callback.onResult(finalExists);
					}
				}.runTask(plugin);
			}
		}.runTaskAsynchronously(plugin);
	}

	public void getUser(UUID uuid, Callback<VillageMember> callback) {
		new BukkitRunnable() {
			@Override
			public void run() {
				VillageMember member = null;
				String query = "SELECT name, village_name, village_permissions FROM `" + villageTableUsers + "` WHERE uuid=? LIMIT 1";

				try (Connection con = plugin.getDataManager().getDatabaseConnector().getConnection();
					 PreparedStatement stmt = con.prepareStatement(query)) {

					stmt.setString(1, uuid.toString());
					try (ResultSet rs = stmt.executeQuery()) {
						if (rs.next()) {
							String name = rs.getString("name");
							String villageName = rs.getString("village_name");
							String permsRaw = rs.getString("village_permissions");

							plugin.getLogger().info("DEBUG -> name=" + name + ", village_name=" + villageName + ", perms=" + permsRaw);

							List<Permission> permissions = new ArrayList<>();
							if (permsRaw != null && !permsRaw.isEmpty()) {
								for (String p : permsRaw.split(";")) {
									try {
										permissions.add(Permission.valueOf(p.trim()));
									} catch (IllegalArgumentException e) {
										plugin.getLogger().warning("Nieprawidłowy PERM: " + p);
									}
								}
							}

							Village village = null;
							if (villageName != null && !villageName.isEmpty()) {
								village = plugin.getVillageManager().getVillageByName(villageName);
							}

							if (village != null) {
								member = new VillageMember(uuid, village, permissions);
							} else {
								plugin.getLogger().warning("⚠️ Wioska " + villageName + " nie znaleziona dla gracza " + name);
							}

						} else {
							plugin.getLogger().warning("⚠️ Użytkownik " + uuid + " nie znaleziony w bazie!");
						}
					}

				} catch (SQLException e) {
					e.printStackTrace();
				}

				VillageMember finalMember = member;
				new BukkitRunnable() {
					@Override
					public void run() {
						callback.callSyncResult(finalMember);
					}
				}.runTask(plugin);
			}
		}.runTaskAsynchronously(plugin);
	}

//	public int loadUsersSync(List<Village> loadedVillages) {
//		int loaded = 0;
//		try (Connection con = plugin.getDatabase().getConnection();
//			 PreparedStatement stmt = con.prepareStatement("SELECT * FROM `" + villageTableUsers + "`");
//			 ResultSet rs = stmt.executeQuery()) {
//
//			while (rs.next()) {
//				String name = rs.getString("name");
//				UUID uuid = UUID.fromString(rs.getString("uuid"));
//				String villageName = rs.getString("village_name");
//				List<Permission> permissions = getPermissionListFromResultSet(rs, "village_permissions");
//
//				if (villageName == null || villageName.isEmpty()) continue;
//
//				Village village = loadedVillages.stream()
//						.filter(v -> v.getVillageName().equalsIgnoreCase(villageName))
//						.findFirst()
//						.orElse(null);
//
//				if (village == null) {
//					plugin.getDebug().debug("⚠ User " + name + " ma nieistniejącą wioskę: " + villageName);
//					continue;
//				}
//
//				VillageMember member = new VillageMember(uuid, village, permissions);
//				plugin.getVillageDataManager().addPlayerMember(uuid, member);
//				loaded++;
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return loaded;
//	}

	public List<Permission> getPermissionListFromResultSet(Record rs, String columnName) {
		List<Permission> permissions = new ArrayList<>();
		String raw = rs.get(columnName).toString();

		if (raw == null || raw.isEmpty()) return permissions;

		for (String s : raw.split(";")) {
			try {
				permissions.add(Permission.valueOf(s.trim()));
			} catch (IllegalArgumentException e) {
				plugin.getLogger().warning("Nieprawidłowy PERM w kolumnie " + columnName + ": " + s);
			}
		}
		return permissions;
	}
}