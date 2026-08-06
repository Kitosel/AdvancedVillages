package pl.kiosel.villages.addons.scoreboard;

import fr.mrmicky.fastboard.FastBoard;
import pl.kiosel.core.thread.MetaTask;
import pl.kiosel.core.utils.ColorUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.settings.Settings;

import java.util.List;

public class ScoreboardAnimationTask implements MetaTask {

	private final ScoreboardManager scoreboardManager;

	public ScoreboardAnimationTask(AdvancedVillages plugin) {
		this.scoreboardManager = plugin.getScoreboardManager();
	}

	@Override
	public void execute() {
		if (!Settings.ADDONS_SCOREBOARD_ENABLE.getBoolean()) return;

		List<String> frames = scoreboardManager.getTitleFrames();
		if (frames == null || frames.isEmpty()) return;

		int index = scoreboardManager.getAnimationIndex();
		int next = (index + 1) % frames.size();
		scoreboardManager.setAnimationIndex(next);

		String title = ColorUtils.tl(frames.get(next));

		for (FastBoard board : scoreboardManager.getBoards().values()) {
			board.updateTitle(title);

//			if (plugin.isDev()) {
//				board.getPlayer().sendMessage("Frame index: " + next + " | Title: " + title);
//			}
		}
	}

	@Override
	public Type getType() {
		return Type.SYNC;
	}
}
