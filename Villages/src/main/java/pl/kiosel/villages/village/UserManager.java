package pl.kiosel.villages.village;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.kiosel.common.Callback;
import pl.kiosel.villages.Wioski;
import pl.kiosel.villages.enums.Permission;

import java.sql.*;
import java.util.*;

public class UserManager {

	private final Wioski plugin;
	private final String villageTableUsers;

	public UserManager(Wioski plugin) {
		this.plugin = plugin;
		this.villageTableUsers = plugin.getDatabase().getTableVillage_users();
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
		plugin.getDatabase().saveUserSync(villageMember);
	}

	public void createUser(Player player) {
		new BukkitRunnable() {
			@Override
			public void run() {
				String query = "INSERT INTO `" + villageTableUsers + "` (name, uuid) VALUES (?, ?)";

				try (Connection con = plugin.getDatabase().getConnection();
					 PreparedStatement stmt = con.prepareStatement(query)) {

					stmt.setString(1, player.getName());
					stmt.setString(2, player.getUniqueId().toString());
					stmt.executeUpdate();

					plugin.getDebug().debug("Created user: " + player.getName());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(plugin);
	}

	public void isUserInDatabase(Player player, Callback<Boolean> callback) {
		isUserInDatabase(player.getUniqueId(), callback);
	}

	public void deleteUser(Player player) {
		new BukkitRunnable() {
			@Override
			public void run() {
				String query = "DELETE FROM `" + villageTableUsers + "` WHERE uuid=?";
				try (Connection con = plugin.getDatabase().getConnection();
					 PreparedStatement stmt = con.prepareStatement(query)) {

					stmt.setString(1, player.getUniqueId().toString());
					stmt.executeUpdate();
					plugin.getDebug().debug("Deleted user: " + player.getName());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(plugin);
	}

	public void addUserToVillage(VillageMember villageMember, Player player, boolean owner) {
		addUserToVillage(villageMember.getVillage(), player, owner);
	}

	public void addUserToVillage(Village village, Player player, boolean owner) {
		new BukkitRunnable() {
			@Override
			public void run() {
				String query = "UPDATE `" + villageTableUsers + "` SET village_name=?,village_permissions=? WHERE uuid=?";
				try (Connection con = plugin.getDatabase().getConnection();
					 PreparedStatement stmt = con.prepareStatement(query)) {
					List<Permission> permissions;

					if (owner) {
						permissions = new ArrayList<>();
						permissions.add(Permission.OWNER);
					} else {
						permissions = plugin.getPermissionManager().getDefaultPermission();
					}

					stmt.setString(1, village.getVillageName());
					stmt.setString(2, plugin.getPermissionManager().toString(permissions));
					stmt.setString(3, player.getUniqueId().toString());
					stmt.executeUpdate();

					plugin.getPlayerDataManager().addPlayerMember(player.getUniqueId(), new VillageMember(player, village, permissions));
					plugin.getDebug().debug("Added user " + player.getName() + " to village " + village.getVillageName());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(plugin);
	}

	public void removeUserFromVillage(Village village, UUID uuid) {
		new BukkitRunnable() {
			@Override
			public void run() {
				String query = "UPDATE `" + villageTableUsers + "` SET village_name=NULL, village_permissions=NULL WHERE uuid=?";
				try (Connection con = plugin.getDatabase().getConnection();
					 PreparedStatement stmt = con.prepareStatement(query)) {

					stmt.setString(1, uuid.toString());
					stmt.executeUpdate();
					plugin.getDebug().debug("Removed user " + uuid.toString() + " from village " + village.getVillageName());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(plugin);
	}

	public void isUserInDatabase(UUID uuid, Callback<Boolean> callback) {
		new BukkitRunnable() {
			@Override
			public void run() {
				boolean exists = false;
				String query = "SELECT 1 FROM `" + villageTableUsers + "` WHERE uuid=? LIMIT 1";

				try (Connection con = plugin.getDatabase().getConnection();
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

				try (Connection con = plugin.getDatabase().getConnection();
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

	public int loadUsersSync(List<Village> loadedVillages) {
		int loaded = 0;
		try (Connection con = plugin.getDatabase().getConnection();
			 PreparedStatement stmt = con.prepareStatement("SELECT * FROM `" + villageTableUsers + "`");
			 ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				String name = rs.getString("name");
				UUID uuid = UUID.fromString(rs.getString("uuid"));
				String villageName = rs.getString("village_name");
				List<Permission> permissions = getPermissionListFromResultSet(rs, "village_permissions");

				if (villageName == null || villageName.isEmpty()) continue;

				Village village = loadedVillages.stream()
						.filter(v -> v.getVillageName().equalsIgnoreCase(villageName))
						.findFirst()
						.orElse(null);

				if (village == null) {
					plugin.getDebug().debug("⚠ User " + name + " ma nieistniejącą wioskę: " + villageName);
					continue;
				}

				VillageMember member = new VillageMember(uuid, village, permissions);
				plugin.getPlayerDataManager().addPlayerMember(uuid, member);
				loaded++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return loaded;
	}

	private List<Permission> getPermissionListFromResultSet(ResultSet rs, String columnName) throws SQLException {
		List<Permission> permissions = new ArrayList<>();
		String raw = rs.getString(columnName);

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