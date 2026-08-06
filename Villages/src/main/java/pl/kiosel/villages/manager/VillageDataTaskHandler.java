package pl.kiosel.villages.manager;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.antylogout.CombatTask;
import pl.kiosel.villages.addons.scoreboard.ScoreboardAnimationTask;
import pl.kiosel.villages.addons.scoreboard.ScoreboardUpdateTask;
import pl.kiosel.villages.addons.tablist.TablistBroadcastTask;
import pl.kiosel.villages.data.DataSaveTask;
import pl.kiosel.villages.settings.Settings;

public class VillageDataTaskHandler {

	private final AdvancedVillages plugin;
	private volatile BukkitTask dataTask;
	private volatile BukkitTask combatTask;
	private volatile BukkitTask tablistTask;
	private volatile BukkitTask scoreboardUpdateTask;
	private volatile BukkitTask scoreboardAnimationTask;

	public VillageDataTaskHandler(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public synchronized void startHandler() {
		plugin.getDebug().debug("Starting task handler");
		long dataInterval = 30L * 20L;
		long combatInterval = 20L;
		long tablistInterval = plugin.getTablistConfig().getUpdateInterval();
		long scoreboardUpdateInterval = 40L;
		long scoreboardAnimationInterval = Math.max(1L, (long) plugin.getScoreboardHandler().getAnimationSpeed() * 20L);

		if (isRunning()) {
			stopHandler();
		}

		DataSaveTask dataSaveTask = new DataSaveTask(this.plugin.getDataloader(), false);
		this.dataTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
				this.plugin, dataSaveTask, dataInterval, dataInterval
		);

		if (Settings.ADDONS_ANTYLOGOUT_ENABLE.getBoolean()) {
			CombatTask combatUpdateTask = new CombatTask(this.plugin.getCombatManager(), this.plugin.getCombatConfig());
			this.combatTask = Bukkit.getScheduler().runTaskTimer(
					this.plugin, combatUpdateTask, combatInterval, combatInterval
			);
		} else {
			this.plugin.getCombatManager().clear();
		}

		if (this.plugin.getTablistConfig().isEnabled()) {
			TablistBroadcastTask tablistBroadcastTask = new TablistBroadcastTask(this.plugin);
			this.tablistTask = Bukkit.getScheduler().runTaskTimer(
					this.plugin, tablistBroadcastTask, tablistInterval, tablistInterval
			);
		}

		if (Settings.ADDONS_SCOREBOARD_ENABLE.getBoolean()) {
			ScoreboardUpdateTask scoreboardUpdate = new ScoreboardUpdateTask(this.plugin);
			this.scoreboardUpdateTask = Bukkit.getScheduler().runTaskTimer(
					this.plugin, scoreboardUpdate, scoreboardUpdateInterval, scoreboardUpdateInterval
			);

			if (this.plugin.getScoreboardHandler().scoreboardAnimationEnabled()) {
				ScoreboardAnimationTask scoreboardAnimation = new ScoreboardAnimationTask(this.plugin);
				this.scoreboardAnimationTask = Bukkit.getScheduler().runTaskTimer(
						this.plugin, scoreboardAnimation, scoreboardAnimationInterval, scoreboardAnimationInterval
				);
			}
		}
	}

	public synchronized void stopHandler() {
		plugin.getDebug().debug("Stop task handler");
		if (this.dataTask != null) {
			this.dataTask.cancel();
			this.dataTask = null;
		}
		if (this.combatTask != null) {
			this.combatTask.cancel();
			this.combatTask = null;
		}
		if (this.tablistTask != null) {
			this.tablistTask.cancel();
			this.tablistTask = null;
		}
		if (this.scoreboardUpdateTask != null) {
			this.scoreboardUpdateTask.cancel();
			this.scoreboardUpdateTask = null;
		}
		if (this.scoreboardAnimationTask != null) {
			this.scoreboardAnimationTask.cancel();
			this.scoreboardAnimationTask = null;
		}
	}

	public void reloadHandler() {
		plugin.getDebug().debug("Reload task handler");
		this.stopHandler();
		this.startHandler();
	}

	private boolean isRunning() {
		return this.dataTask != null
				|| this.combatTask != null
				|| this.tablistTask != null
				|| this.scoreboardUpdateTask != null
				|| this.scoreboardAnimationTask != null;
	}
}
