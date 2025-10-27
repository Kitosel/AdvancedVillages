package pl.kiosel.villages.models;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import pl.kiosel.villages.Wioski;
import pl.kiosel.villages.village.Village;

import java.util.List;

public class DataLoader {

	private final Wioski plugin;

	public DataLoader(Wioski plugin) {
		this.plugin = plugin;
	}

	public void loadAllData() {
		plugin.getLogger().info("=====================================");
		plugin.getLogger().info("       Loading village data...       ");

		plugin.getDebug().debug("Loading village...");
		new BukkitRunnable() {
			@Override
			public void run() {
				long start = System.currentTimeMillis();

				plugin.getLogger().info("➡ Loading villages...");
				plugin.getVillageManager().loadVillagesSync();

				int villagesLoaded = plugin.getPlayerDataManager().getVillages().size();
				plugin.getLogger().info("Loaded " + villagesLoaded + " villages (" +
						(System.currentTimeMillis() - start) + "ms)");

				loadUsersWithProgress();
			}
		}.runTaskAsynchronously(plugin);
	}

	private void loadUsersWithProgress() {
		plugin.getDebug().debug("Loading users...");
		new BukkitRunnable() {
			@Override
			public void run() {
				plugin.getLogger().info("➡ Loading users...");

				long start = System.currentTimeMillis();
				List<Village> villages = plugin.getPlayerDataManager().getVillageAsList();

				int usersLoaded = plugin.getUserManager().loadUsersSync(villages);
				long took = System.currentTimeMillis() - start;

				new BukkitRunnable() {
					@Override
					public void run() {
						plugin.getLogger().info("Loaded " + usersLoaded + " users (" + took + "ms)");
						plugin.getLogger().info("     All data has been loaded!");
						plugin.getLogger().info("=====================================");
					}
				}.runTask(plugin);
			}
		}.runTaskAsynchronously(plugin);
	}


}