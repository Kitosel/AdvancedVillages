package pl.kiosel.villages.addons.scoreboard;

import fr.mrmicky.fastboard.FastBoard;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageUtilsManager;
import pl.kiosel.villages.settings.Settings;

import java.util.*;

import static pl.kiosel.core.utils.ColorUtils.tl;

public class ScoreboardManager {

	private final AdvancedVillages plugin;
	@Getter private final Map<UUID, FastBoard> boards = new HashMap<>();
	@Getter private final List<String> titleFrames = new ArrayList<>();
	@Setter @Getter private int animationIndex = 0;

	public ScoreboardManager(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public void reloadScoreboard() {
		this.titleFrames.clear();
		this.animationIndex = 0;
		setupTitleAnimation();
	}

	public void createBoard(Player player) {
		if (!Settings.ADDONS_SCOREBOARD_ENABLE.getBoolean() || boards.containsKey(player.getUniqueId())) {
			return;
		}
		FastBoard board = new FastBoard(player);
		boards.put(player.getUniqueId(), board);
		if (!this.titleFrames.isEmpty()) {
			board.updateTitle(this.titleFrames.get(0));
		}
		updateBoard(board);
	}

	public void removeBoard(Player player) {
		FastBoard board = boards.remove(player.getUniqueId());
		if (board != null) board.delete();
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
			FastBoard board = this.boards.get(player.getUniqueId());
			if (board != null) {
				if (!this.titleFrames.isEmpty())
					board.updateTitle(this.titleFrames.get(0));
				updateBoard(board);
			}
		}

		for (UUID playerId : new HashSet<>(this.boards.keySet())) {
			if (!onlinePlayers.contains(playerId)) {
				FastBoard board = this.boards.remove(playerId);
				if (board != null)
					board.delete();
			}
		}
	}

	public void clearBoards() {
		for (FastBoard board : this.boards.values())
			board.delete();
		this.boards.clear();
	}

	private void setupTitleAnimation() {
		if (plugin.getScoreboardHandler().scoreboardAnimationEnabled())
			for (String string : plugin.getScoreboardHandler().getTitles())
				this.titleFrames.add(tl(string));

		if (this.titleFrames.isEmpty())
			titleFrames.add(tl(plugin.getScoreboardHandler().scoreboardTitle()));
		for (String s : titleFrames)
			plugin.getDebug().debug("titleFrames: " + s);
	}

	public void updateBoard(FastBoard board) {
		Player player = board.getPlayer();
		User user = plugin.getUserManager().findByUuid(player.getUniqueId()).orNull();
		if (user == null)
			return;
		Village village = user.getPresentVillage();

		List<String> lines = new ArrayList<>();

		for (String line : plugin.getScoreboardHandler().getScore()) {
			line = VillageUtilsManager.replaceWith(player, village, line).toText();
			if (plugin.isPlaceholder())
				line = plugin.getPlaceholder().replacePlaceholder(player, line);
			lines.add(line);
		}
		board.updateLines(lines);
	}
}
