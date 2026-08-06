package pl.kiosel.villages.addons.scoreboard;

import fr.mrmicky.fastboard.FastBoard;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageUtilsManager;

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
		setupTitleAnimation();
	}

	public void createBoard(Player player) {
		FastBoard board = new FastBoard(player);
		boards.put(player.getUniqueId(), board);
		updateBoard(board);
	}

	public void removeBoard(Player player) {
		FastBoard board = boards.remove(player.getUniqueId());
		if (board != null) board.delete();
	}

	private void setupTitleAnimation() {
		if (plugin.getScoreboardHandler().scoreboardAnimationEnabled()) {
			for (String string : plugin.getScoreboardHandler().getTitles()) {
				this.titleFrames.add(tl(string));
			}
		} else {
			titleFrames.add(plugin.getScoreboardHandler().scoreboardTitle());
		}
		for (String s : titleFrames) {
			plugin.getDebug().debug("titleFrames: " + s);
		}
	}

	public void updateBoard(FastBoard board) {
		Player player = board.getPlayer();
		User user = plugin.getUserManager().findByUuid(player.getUniqueId()).get();
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