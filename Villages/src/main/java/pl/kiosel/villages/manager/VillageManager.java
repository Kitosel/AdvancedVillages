package pl.kiosel.villages.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.kiosel.core.utils.ColorUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiConfig;
import pl.kiosel.villages.settings.Settings;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageMember;

import javax.annotation.Nullable;
import java.util.*;

public class VillageManager {

	private final AdvancedVillages plugin;
	private final VillageDataManager manager;

	public VillageManager(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.manager = plugin.getVillageDataManager();
	}

	public void saveVillageAsync(Village village) {
		new BukkitRunnable() {
			@Override
			public void run() {
				plugin.getDataHelper().saveVillageSync(village);
			}
		}.runTaskAsynchronously(plugin);
	}

	public void createVillage(Village village) {
		plugin.getDataHelper().createVillage(village);
		UUID uuid = village.getOwnerUUID();

		manager.putVillage(village.getVillageName(), village);
		VillageMember member = manager.getVillageMember(uuid);
		if (member != null) member.setOwner(village.getOwner());
		manager.addPlayerMember(uuid, member);
		plugin.getDebug().debug("Created village: " + village.getVillageName());
	}

	public void deleteVillage(Village village) {
		plugin.getDataHelper().deleteVillage(village);
		plugin.getDebug().debug("Deleted village: " + village.getVillageName());
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

		StringBuilder members = new StringBuilder();
		for (UUID uuid : village.getMembers()) {
			members.append(uuid).append(";");
		}

		plugin.getDataHelper().updateMember(members.toString(), village.getVillageName());

		plugin.getDebug().debug("Added member to database: " + player.getName() + " -> " + village.getVillageName());
	}


	public void removeMember(Village village, Player member) {
		StringBuilder members = new StringBuilder();
		for (UUID s : village.getMembers())
			if (!s.toString().equalsIgnoreCase(member.getUniqueId().toString()))
				members.append(s).append(";");

		plugin.getDataHelper().updateMember(members.toString(), village.getVillageName());

		village.removeMember(member.getUniqueId());
		manager.removePlayerMember(member.getUniqueId());

		plugin.getDebug().debug("Removed member: " + member + " to village " + village.getVillageName());
	}

	public void destroyVillage(Village village, int hearth) {
		if (village.getLife() > 1) {
			village.setLife(village.getLife() - hearth);
			village.setProtection(true);
			plugin.getVillageRemoveManager().destroyVillage(village, true);
		} else {
			plugin.getVillageRemoveManager().removeVillage(village, true);
		}
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
		VillageDataManager dm = AdvancedVillages.getInstance().getVillageDataManager();
		VillageMember member = dm.getVillageMember(Bukkit.getOfflinePlayer(owner).getUniqueId());
		return member != null ? dm.getVillages().get(member.getOwner()) : null;
	}

	@Nullable
	public static Village getVillageByOwner(Player owner) {
		return getVillageByOwner(owner.getName());
	}

	@Nullable
	public static Village getVillageByOfflineOwner(String owner) {
		VillageDataManager dm = AdvancedVillages.getInstance().getVillageDataManager();
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
		World world = plugin.getServer().getWorld(Settings.SPAWN_WORLD.getString());
		double x = Settings.SPAWN_X.getDouble();
		double y = Settings.SPAWN_Y.getDouble();
		double z = Settings.SPAWN_Z.getDouble();
		Location vLoc = new Location(world, x, y, z);
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
		return ColorUtils.tl(string.replace("%village_level%", village == null ? "" : village.getLevel().getLevel()+"")
				.replace("%village_cost%", village == null ? "" : village.getLevel().getCostEconomy()+"")
				.replace("%village_teleport%", village == null ? "" : village.tpToString())
				.replace("%village_name%", village == null ? "" : village.getVillageName())
				.replace("%village_owner%", village == null ? "" : village.getOwner())
				.replace("%village_bank%", village == null ? "" : village.getBank()+"")
				.replace("%village_life%", village == null ? "" : village.getLife()+"")
				.replace("%village_size%", village == null ? "" : village.getLevel().getSize()+"")
				.replace("%village_tag%", village == null ? "" : village.isTag() ? village.getTag() : notag)
				.replace("%village_pvp%", village == null ? "" : village.getVillageSettings().isPvp() ? GuiConfig.on : GuiConfig.off)
				.replace("%village_tnt%", village == null ? "" : village.getVillageSettings().isTnt() ? GuiConfig.on : GuiConfig.off)
				.replace("%istagset%", village == null ? "" : village.isTag() ? tagnotset : tagset));
	}
}