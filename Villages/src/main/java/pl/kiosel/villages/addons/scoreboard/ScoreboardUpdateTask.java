package pl.kiosel.villages.addons.scoreboard;

import fr.mrmicky.fastboard.FastBoard;
import pl.kiosel.core.thread.MetaTask;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.settings.Settings;

public class ScoreboardUpdateTask implements MetaTask {

	private final ScoreboardManager scoreboardManager;

	public ScoreboardUpdateTask(AdvancedVillages plugin) {
		this.scoreboardManager = plugin.getScoreboardManager();
	}

	@Override
	public void execute() {
		if (!Settings.ADDONS_SCOREBOARD_ENABLE.getBoolean()) return;

		for (FastBoard board : scoreboardManager.getBoards().values()) {
			scoreboardManager.updateBoard(board);
//				if (plugin.isDev())
//					board.getPlayer().sendMessage("updated scoreboard");
		}
	}

	@Override
	public Type getType() {
		return Type.SYNC;
	}
}
