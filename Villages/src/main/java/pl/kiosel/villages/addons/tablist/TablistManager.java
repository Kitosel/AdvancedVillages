package pl.kiosel.villages.addons.tablist;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import pl.kiosel.rosacore.nms.api.tablist.TabList;
import pl.kiosel.rosacore.nms.api.tablist.TabListService;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public final class TablistManager {

	private final AdvancedVillages plugin;
	private final TablistConfiguration configuration;
	private final TabListService tabLists;
	private final TablistRenderer renderer;
	private final Map<UUID, TablistSession> sessions = new HashMap<>();

	private TablistSnapshot snapshot;
	private BukkitTask updateTask;

	public TablistManager(AdvancedVillages plugin, TablistConfiguration configuration,
	                     TablistPlaceholdersService placeholders,
	                     TabListService tabLists) {
		this.plugin = plugin;
		this.configuration = configuration;
		this.tabLists = tabLists;
		this.renderer = new TablistRenderer(plugin, placeholders);
		this.snapshot = configuration.snapshot();
	}

	public void reload() {
		this.stopTask();
		this.clearSessions();
		this.snapshot = this.configuration.snapshot();
		if (!this.snapshot.isEnabled()) {
			return;
		}

		for (Player player : Bukkit.getOnlinePlayers()) {
			this.plugin.getUserManager().findByPlayer(player).ifPresent(user -> this.open(player, user));
		}
		long interval = this.snapshot.getUpdateInterval();
		this.updateTask = Bukkit.getScheduler().runTaskTimer(
				this.plugin, this::tick, interval, interval
		);
	}

	public void handleJoin(Player player, User user) {
		if (!this.snapshot.isEnabled()) {
			return;
		}
		this.close(player, true);
		this.open(player, user);
	}

	public void handleQuit(Player player) {
		this.sessions.remove(player.getUniqueId());
		this.tabLists.clear(player);
	}

	public void shutdown() {
		this.stopTask();
		this.clearSessions();
	}

	private void tick() {
		Iterator<Map.Entry<UUID, TablistSession>> iterator = this.sessions.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, TablistSession> entry = iterator.next();
			Player player = Bukkit.getPlayer(entry.getKey());
			if (player == null || !player.isOnline()) {
				iterator.remove();
				continue;
			}

			try {
				entry.getValue().tick(player);
			} catch (RuntimeException exception) {
				iterator.remove();
				this.plugin.getRosaLogger().log(Level.WARNING,
						"Disabling tablist for " + player.getName() + " after an update error", exception);
				tryClear(entry.getValue(), player);
			}
		}
	}

	private void open(Player player, User user) {
		TablistSession session = null;
		try {
			this.tabLists.clear(player);
			TabList transport = this.tabLists.create(player);
			session = new TablistSession(
					user,
					transport,
					this.renderer,
					this.snapshot
			);
			session.sendInitial(player);
			this.sessions.put(player.getUniqueId(), session);
		} catch (RuntimeException exception) {
			if (session != null) {
				this.tryClear(session, player);
			}
			this.plugin.getRosaLogger().log(Level.WARNING,
					"Could not create tablist for " + player.getName(), exception);
		}
	}

	private void close(Player player, boolean clear) {
		TablistSession session = this.sessions.remove(player.getUniqueId());
		if (clear && session != null) {
			tryClear(session, player);
		}
	}

	private void clearSessions() {
		for (Map.Entry<UUID, TablistSession> entry : this.sessions.entrySet()) {
			Player player = Bukkit.getPlayer(entry.getKey());
			if (player != null && player.isOnline()) {
				tryClear(entry.getValue(), player);
			}
		}
		this.sessions.clear();
	}

	private void stopTask() {
		if (this.updateTask != null) {
			this.updateTask.cancel();
			this.updateTask = null;
		}
	}

	private void tryClear(TablistSession session, Player player) {
		try {
			this.tabLists.clear(player);
		} catch (RuntimeException exception) {
			this.plugin.getRosaLogger().log(Level.FINE,
					"Could not clear tablist for " + player.getName(), exception);
		}
	}
}
