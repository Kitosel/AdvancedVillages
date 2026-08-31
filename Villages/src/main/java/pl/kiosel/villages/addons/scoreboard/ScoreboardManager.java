package pl.kiosel.villages.addons.scoreboard;

import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.Settings;

import java.util.*;
import java.util.logging.Level;

public class ScoreboardManager {

	private final AdvancedVillages plugin;
	private final Map<UUID, VillageScoreboard> boards = new LinkedHashMap<>();
	private ScoreboardSnapshot snapshot;

	public ScoreboardManager(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.snapshot = plugin.getScoreboardHandler().snapshot();
	}

	public void reload() {
		if (!Settings.ADDONS_SCOREBOARD_ENABLE.getBoolean()) return;
		this.snapshot = this.plugin.getScoreboardHandler().snapshot();

		for (VillageScoreboard board : new ArrayList<>(this.boards.values())) {
			if (board.isClosed()) continue;
			board.reload(this.snapshot);
		}
	}

	public void createBoard(Player player) {
		if (player == null || !player.isOnline() || !Settings.ADDONS_SCOREBOARD_ENABLE.getBoolean()) return;

		VillageScoreboard current = this.boards.get(player.getUniqueId());
		if (current != null && !current.isClosed()) return;
		if (current != null) this.boards.remove(player.getUniqueId());

		VillageScoreboard board = new VillageScoreboard(this.plugin, player, this.snapshot);
		try {
			board.show();
			this.boards.put(player.getUniqueId(), board);
		} catch (RuntimeException exception) {
			board.close();
			this.plugin.getRosaLogger().log(Level.WARNING, "Could not show the scoreboard to " + player.getName(), exception);
		}
	}

	public void removeBoard(Player player) {
		if (player == null) return;
		VillageScoreboard board = this.boards.remove(player.getUniqueId());
		if (board != null) board.close();
	}

	public void synchronizeBoards() {
		if (!Settings.ADDONS_SCOREBOARD_ENABLE.getBoolean()) {
			clearBoards();
			return;
		}

		Set<UUID> onlinePlayers = new HashSet<>();
		for (Player player : this.plugin.getServer().getOnlinePlayers()) {
			onlinePlayers.add(player.getUniqueId());
			createBoard(player);
		}

		for (UUID playerId : new HashSet<>(this.boards.keySet())) {
			if (onlinePlayers.contains(playerId)) continue;
			VillageScoreboard board = this.boards.remove(playerId);
			if (board != null) board.close();
		}
	}

	public void clearBoards() {
		for (VillageScoreboard board : new ArrayList<>(this.boards.values())) board.close();
		this.boards.clear();
	}
}
