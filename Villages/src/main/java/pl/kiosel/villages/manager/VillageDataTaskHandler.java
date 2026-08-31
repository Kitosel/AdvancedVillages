package pl.kiosel.villages.manager;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.DataSaveTask;

public class VillageDataTaskHandler {

	private final AdvancedVillages plugin;
	private volatile BukkitTask dataTask;

	public VillageDataTaskHandler(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public synchronized void startHandler() {
		plugin.getDebug().debug("Starting task handler");
		long dataInterval = 30L * 20L;

		if (isRunning()) {
			shutdown();
		}

		DataSaveTask dataSaveTask = new DataSaveTask(this.plugin.getDataloader(), false);
		this.dataTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
				this.plugin, dataSaveTask, dataInterval, dataInterval
		);
	}

	public synchronized void shutdown() {
		plugin.getDebug().debug("Stop task handler");
		if (this.dataTask != null) {
			this.dataTask.cancel();
			this.dataTask = null;
		}
	}

	public void reload() {
		plugin.getDebug().debug("Reload task handler");
		this.shutdown();
		this.startHandler();
	}

	private boolean isRunning() {
		return this.dataTask != null;
	}
}
