package pl.kiosel.villages.manager;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import pl.kiosel.core.thread.MetaTask;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.antylogout.CombatTask;
import pl.kiosel.villages.addons.scoreboard.ScoreboardAnimationTask;
import pl.kiosel.villages.addons.scoreboard.ScoreboardUpdateTask;
import pl.kiosel.villages.addons.tablist.TablistBroadcastTask;
import pl.kiosel.villages.data.DataSaveTask;

public class VillageDataTaskHandler {

	private final AdvancedVillages plugin;
	private volatile BukkitTask dataTask;
	private volatile BukkitTask combatTask;
	private volatile BukkitTask tablistTask;
	private volatile MetaTask scoreboardUpdateTask;
	private volatile MetaTask scoreboardAnimationTask;

	public VillageDataTaskHandler(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public void startHandler() {
		plugin.getDebug().debug("Starting task handler");
		long dataInterval = 30L * 20L;
		long combatInterval = 20L;
		long tablistInterval = plugin.getTablistConfig().updateInterval;
		long scoreboardUpdateInterval = 40L;
		long scoreboardAnimationInterval = plugin.getScoreboardHandler().getAnimationSpeed() * 20L;

		if (this.dataTask == null) {
			this.dataTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, () -> {
				this.plugin.scheduleMetaTasks(new DataSaveTask(this.plugin.getDataloader(), false));
			}, dataInterval, dataInterval);
		} else {
			this.dataTask.cancel();
		}

		if (this.combatTask == null) {
			this.combatTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, () -> {
				this.plugin.scheduleMetaTasks(new CombatTask(this.plugin.getCombatCache(), this.plugin.getCombatConfig()));
			}, combatInterval, combatInterval);
		} else {
			this.combatTask.cancel();
		}

		if (this.tablistTask == null) {
			this.tablistTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, () -> {
				this.plugin.scheduleMetaTasks(new TablistBroadcastTask(plugin));
			}, 20L, tablistInterval);
		} else {
			this.tablistTask.cancel();
		}

		if (this.scoreboardUpdateTask == null) {
			this.scoreboardUpdateTask = new ScoreboardUpdateTask(plugin);

			Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, () -> {
				this.plugin.scheduleMetaTasks(scoreboardUpdateTask);
			}, scoreboardUpdateInterval, scoreboardUpdateInterval);
		}

		if (this.scoreboardAnimationTask == null) {
			this.scoreboardAnimationTask = new ScoreboardAnimationTask(plugin);
			Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
				this.plugin.scheduleMetaTasks(scoreboardAnimationTask);
			}, scoreboardAnimationInterval, scoreboardAnimationInterval);
		}
	}

	public void stopHandler() {
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
	}

	public void reloadHandler() {
		plugin.getDebug().debug("Reload task handler");
		this.stopHandler();
		this.startHandler();
	}
}