package pl.kiosel.villages.village;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import pl.kiosel.common.utils.LocationUtils;
import pl.kiosel.villages.Wioski;
import pl.kiosel.villages.config.GuiConfig;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static pl.kiosel.common.utils.ColorUtils.tl;
import static pl.kiosel.common.Item.create;

public class VillageManager {

	private final Wioski plugin;
	private final String villageTable;
	private final DataManager manager;
	private final VillageNameGenerator villageNameGenerator;

	public VillageManager(Wioski plugin) {
		this.plugin = plugin;
		this.villageTable = plugin.getDatabase().getTableVillage();
		this.manager = plugin.getPlayerDataManager();
		this.villageNameGenerator = new VillageNameGenerator();
	}

	public void saveVillageAsync(Village village) {
		new BukkitRunnable() {
			@Override
			public void run() {
				saveVillageSync(village);
			}
		}.runTaskAsynchronously(plugin);
	}

	public void saveVillageSync(Village village) {
		plugin.getDatabase().saveVillageSync(village);
	}

	public List<ItemStack> getTopVillage() {
		List<Village> villages = new ArrayList<>(manager.getVillages().values());
		villages.sort((v1, v2) -> Integer.compare(v2.getLevel(), v1.getLevel()));

		List<ItemStack> topPlots = new ArrayList<>();
		for (int i = 0; i < 9; i++) {
			if (i < villages.size()) {
				Village village = villages.get(i);
				List<String> lore = new ArrayList<>();
				lore.add("");
				lore.add(tl("&c* &b&lPoziom wioski &7" + village.getLevel()));
				lore.add("");
				lore.add(tl("&c* &b&lMiejsce &7#" + (i + 1)));
				lore.add(tl("&c* &b&lCzłonków &7" + village.getMembers().size()));
				lore.add("");
				lore.add(tl("&c* &b&lCzłonkowie:"));
				for (int j = 0; j < village.getMembers().size() && j <= 8; j++) {
					lore.add(tl("  &f- &7" + village.getMembers().get(j)));
				}
				lore.add("");

				ItemStack item = create(Material.PLAYER_HEAD, "&7#" + (i + 1) + " Działka: &b&n" + village.getOwner());
				SkullMeta meta = (SkullMeta) item.getItemMeta();
				meta.setOwner(village.getOwner());
				meta.setLore(lore);
				item.setItemMeta(meta);

				topPlots.add(item);
			} else {
				topPlots.add(create(Material.PLAYER_HEAD, "&7#" + (i + 1) + " Wioska: &4&l&nBRAK", "&cZa mało Wiosek"));
			}
		}
		return topPlots;
	}

	public void loadVillage() {
		new BukkitRunnable() {
			@Override
			public void run() {
				try (Connection con = plugin.getDatabase().getConnection();
					 PreparedStatement stmt = con.prepareStatement("SELECT * FROM `" + villageTable + "`");
					 ResultSet rs = stmt.executeQuery()) {

					manager.flush();

					while (rs.next()) {
						String owner = rs.getString("owner");
						Location location = LocationUtils.getLocationFromString(rs.getString("location"));
						Location tp = LocationUtils.getLocationFromString(rs.getString("tp"));
						String villageName = rs.getString("village_name");
						String effectsData = rs.getString("effects_data");
						String effectsActive = rs.getString("effects_active");
						String intsData = rs.getString("ints_data");
						String tag = rs.getString("tag");

						List<UUID> members = getUUIDListFromResultSet(rs, "members");

						Village village = new Village(owner, location, tp, members, villageName, effectsData, effectsActive, intsData, tag);
						manager.putVillage(villageName, village);

						plugin.getDebug().debug("Loaded village: " + villageName);
					}
					plugin.getLogger().info("Villages loaded successfully!");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(plugin);
	}

	public List<Village> loadVillagesSync() {
		List<Village> loadedVillages = new ArrayList<>();

		try (Connection con = plugin.getDatabase().getConnection();
			 PreparedStatement stmt = con.prepareStatement("SELECT * FROM `" + villageTable + "`");
			 ResultSet rs = stmt.executeQuery()) {

			manager.flush();

			while (rs.next()) {
				String owner = rs.getString("owner");
				Location location = LocationUtils.getLocationFromString(rs.getString("location"));
				Location tp = LocationUtils.getLocationFromString(rs.getString("tp"));
				String villageName = rs.getString("village_name");
				String effectsData = rs.getString("effects_data");
				String effectsActive = rs.getString("effects_active");
				String intsData = rs.getString("ints_data");
				String tag = rs.getString("tag");

				List<UUID> members = getUUIDListFromResultSet(rs, "members");

				Village village = new Village(owner, location, tp, members, villageName, effectsData, effectsActive, intsData, tag);
				manager.putVillage(villageName, village);
				loadedVillages.add(village);

				plugin.getDebug().debug("Loaded village: " + villageName);
			}
			plugin.getLogger().info("Villages loaded successfully! (" + loadedVillages.size() + ")");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return loadedVillages;
	}

	private List<UUID> getUUIDListFromResultSet(ResultSet rs, String columnName) throws SQLException {
		List<UUID> uuidList = new ArrayList<>();
		String raw = rs.getString(columnName);

		if (raw == null || raw.isEmpty()) {
			return uuidList;
		}

		for (String uuidStr : raw.split(";")) {
			try {
				uuidList.add(UUID.fromString(uuidStr));
			} catch (IllegalArgumentException e) {
				System.err.println("Nieprawidłowy UUID w kolumnie " + columnName + ": " + uuidStr);
			}
		}

		return uuidList;
	}

	public void createVillage(Player player, Location location) {
		new BukkitRunnable() {
			@Override
			public void run() {
				String owner = player.getName();
				String name = villageNameGenerator.getRandomName();
				Location blockLocation = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
				Location teleportLocation = new Location(location.getWorld(), location.getX() + 0.5, location.getY() + 1, location.getZ() + 0.5);
				List<UUID> members = new ArrayList<>(Collections.singletonList(player.getUniqueId()));
				String effectsData = "false;false;false;false;";
				String effectsActive = "false;false;false;false;";
				String intsData = "15;1;3;0"; // size level life bank
				String settings = VillageSettings.getDefaultString();
				String tag = "none";

				String query = "INSERT INTO `" + villageTable + "` " +
						"(owner, village_name, location, tp, members, effects_data, ints_data, settings, tag) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

				try (Connection con = plugin.getDatabase().getConnection();
					 PreparedStatement stmt = con.prepareStatement(query)) {

					stmt.setString(1, owner);
					stmt.setString(2, name);
					stmt.setString(3, LocationUtils.convertLocactionToString(blockLocation));
					stmt.setString(4, LocationUtils.convertLocactionToString(teleportLocation));
					stmt.setString(5, owner + ";");
					stmt.setString(6, effectsData);
					stmt.setString(7, intsData);
					stmt.setString(8, settings);
					stmt.setString(9, tag);
					stmt.executeUpdate();

					Village village = new Village(owner, blockLocation, teleportLocation, members, name, effectsData, effectsActive, intsData, tag);
					manager.putVillage(name, village);
					VillageMember member = manager.getVillageMember(player.getUniqueId());
					if (member != null) member.setOwner(owner);
					manager.addPlayerMember(player.getUniqueId(), member);
					plugin.getDebug().debug("Created village: " + name);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(plugin);
	}

	public void createVillage(Village village) {
		new BukkitRunnable() {
			@Override
			public void run() {
				String owner = village.getOwner();
				String name = village.getVillageName();
				Location blockLocation = village.getLocation();
				Location teleportLocation = village.getTeleport();
				List<UUID> members = village.getMembers();
				String effectsData = village.effectsBuyedToString();
				String effectsActive = village.effectsActiveToString();
				String intsData = village.intsToString();
				String settings = VillageSettings.getDefaultString();
				String tag = village.getTag();

				StringBuilder memberString = new StringBuilder();

				for (UUID mem : members) {
					memberString.append(mem).append(";");
				}

				String query = "INSERT INTO `" + villageTable + "` " +
						"(owner, village_name, location, tp, members, effects_data, effects_active, ints_data, settings, tag) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

				try (Connection con = plugin.getDatabase().getConnection();
					 PreparedStatement stmt = con.prepareStatement(query)) {

					stmt.setString(1, owner);
					stmt.setString(2, name);
					stmt.setString(3, LocationUtils.convertLocactionToString(blockLocation));
					stmt.setString(4, LocationUtils.convertLocactionToString(teleportLocation));
					stmt.setString(5, memberString.toString());
					stmt.setString(6, effectsData);
					stmt.setString(7, effectsActive);
					stmt.setString(8, intsData);
					stmt.setString(9, settings);
					stmt.setString(10, tag);
					stmt.executeUpdate();
					UUID uuid = Bukkit.getOfflinePlayer(owner).getUniqueId();

					manager.putVillage(name, village);
					VillageMember member = manager.getVillageMember(uuid);
					if (member != null) member.setOwner(owner);
					manager.addPlayerMember(uuid, member);
					plugin.getDebug().debug("Created village: " + name);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(plugin);
	}

	public void deleteVillage(Village village) {
		new BukkitRunnable() {
			@Override
			public void run() {
				String query = "DELETE FROM `" + villageTable + "` WHERE village_name=?";
				try (Connection con = plugin.getDatabase().getConnection();
					 PreparedStatement stmt = con.prepareStatement(query)) {
					plugin.getDebug().debug("Deleted village: " + village.getVillageName());

					stmt.setString(1, village.getVillageName());
					stmt.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(plugin);
	}

	public void addMember(Village village, Player player) {
		if (village.getMembers().contains(player.getUniqueId())) {
			plugin.getDebug().debug("Player " + player.getName() + " is already a member of " + village.getVillageName());
			return;
		}

		Bukkit.getScheduler().runTask(plugin, () -> {
			village.addMember(player.getUniqueId());
			plugin.getDebug().debug("Added member to memory: " + player.getName());
		});

		new BukkitRunnable() {
			@Override
			public void run() {
				try (Connection con = plugin.getDatabase().getConnection();
					 PreparedStatement stmt = con.prepareStatement("UPDATE `" + villageTable + "` SET members=? WHERE village_name=?")) {

					StringBuilder membersBuilder = new StringBuilder();
					for (UUID uuid : village.getMembers()) {
						membersBuilder.append(uuid).append(";");
					}

					stmt.setString(1, membersBuilder.toString());
					stmt.setString(2, village.getVillageName());
					stmt.executeUpdate();

					plugin.getDebug().debug("Added member to database: " + player.getName() + " -> " + village.getVillageName());
				} catch (SQLException e) {
					plugin.getLogger().severe("Failed to add member " + player.getName() + " to village " + village.getVillageName());
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(plugin);
	}


	public void removeMember(Village village, Player member) {
		new BukkitRunnable() {
			@Override
			public void run() {
				try (Connection con = plugin.getDatabase().getConnection();
					 PreparedStatement stmt = con.prepareStatement("UPDATE `" + villageTable + "` SET members=? WHERE village_name=?")) {

					StringBuilder members = new StringBuilder();
					for (UUID s : village.getMembers())
						if (!s.toString().equalsIgnoreCase(member.getUniqueId().toString()))
							members.append(s).append(";");

					stmt.setString(1, members.toString());
					stmt.setString(2, village.getVillageName());
					stmt.executeUpdate();

					village.removeMember(member.getUniqueId());
					manager.removePlayerMember(member.getUniqueId());

					plugin.getDebug().debug("Removed member: " + member + " to village " + village.getVillageName());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(plugin);
	}

	public boolean hasVillage(Player player) {
		VillageMember member = manager.getVillageMember(player.getUniqueId());
		return member != null && member.getOwner() != null;
	}

	@Nullable
	public Village getVillageAt(Location loc) {
		for (Village village : manager.getVillages().values())
			if (village.getVillageAt(loc) != null) return village;
		return null;
	}

	@Nullable
	public Village getVillageAt(Location loc, int customSize) {
		for (Village village : manager.getVillages().values())
			if (village.getVillageAt(loc, customSize) != null) return village;
		return null;
	}

	@Nullable
	public Village getVillageByName(String name) {
		return manager.getVillages().get(name);
	}

	@Nullable
	public static Village getVillageByOwner(String owner) {
		DataManager dm = Wioski.getInstance().getPlayerDataManager();
		VillageMember member = dm.getVillageMember(Bukkit.getOfflinePlayer(owner).getUniqueId());
		return member != null ? dm.getVillages().get(member.getOwner()) : null;
	}

	@Nullable
	public static Village getVillageByOwner(Player owner) {
		return getVillageByOwner(owner.getName());
	}

	@Nullable
	public static Village getVillageByOfflineOwner(String owner) {
		DataManager dm = Wioski.getInstance().getPlayerDataManager();
		for (Village village : dm.getVillages().values()) {
			if (village.getOwner().equalsIgnoreCase(owner.trim()) ||
					village.getMembers().stream().anyMatch(uuid ->
							uuid.toString().equalsIgnoreCase(Bukkit.getPlayer(owner).getUniqueId().toString()))) {
				return village;
			}
		}
		return null;
	}

	@Nullable
	public static Village getVillageByOfflineOwner(Player player) {
		return getVillageByOfflineOwner(player.getName());
	}

	public boolean isVillageNearby(Location location, int radius) {
		for (Village village : manager.getVillages().values()) {
			Location vLoc = village.getLocation();
			if (vLoc == null || vLoc.getWorld() == null) continue;
			if (!vLoc.getWorld().equals(location.getWorld())) continue;

			if (vLoc.distance(location) <= radius) {
				return true;
			}
		}
		return false;
	}

	public boolean isSpawnNearby(Location location, int radius) {
		Location vLoc = new Location(Bukkit.getWorld("world"), 0, 0, 0);
		if (vLoc.getWorld() == null) return false;
		if (!vLoc.getWorld().equals(location.getWorld())) return false;

		return vLoc.distance(location) <= radius;
	}

	public static List<String> replaceWithList(Village village, List<String> strings) {
		List<String> list = new ArrayList<>();
		for (String s : strings) {
			list.add(replaceWith(village, s));
		}
		return list;
	}

	public static String replaceWith(Village village, String string) {
		String notag = GuiConfig.no_tag;
		String tagset = GuiConfig.guis_village_setting_tag_set;
		String tagnotset = GuiConfig.guis_village_setting_tag_notset;
		return string.replace("%village_level%", village == null ? " " : village.getLevel()+"")
				.replace("%village_cost%", village == null ? " " : UpgradeManager.getCostForLevel(village.getLevel())+"")
				.replace("%village_teleport%", village == null ? " " : village.tpToString())
				.replace("%village_name%", village == null ? " " : village.getVillageName())
				.replace("%village_owner%", village == null ? " " : village.getOwner())
				.replace("%village_bank%", village == null ? " " : village.getBank()+"")
				.replace("%village_life%", village == null ? " " : village.getLife()+"")
				.replace("%village_size%", village == null ? " " : village.getSize()+"")
				.replace("%village_tag%", village == null ? " " : village.isTag() ? village.getTag() : notag)
				.replace("%village_pvp%", village == null ? " " : village.getVillageSettings().isPvp() ? GuiConfig.on : GuiConfig.off)
				.replace("%village_tnt%", village == null ? " " : village.getVillageSettings().isTnt() ? GuiConfig.on : GuiConfig.off)
				.replace("%istagset%", village == null ? " " : village.isTag() ? tagnotset : tagset);
	}
}