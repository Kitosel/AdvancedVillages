package pl.kiosel.villages.addons.scoreboard;

import org.bukkit.entity.Player;
import pl.kiosel.rosacore.scoreboard.RosaScoreboard;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageUtilsManager;

public class VillageScoreboard extends RosaScoreboard {

	private final AdvancedVillages plugin;
	private ScoreboardSnapshot snapshot;
	private long contentElapsedTicks;
	private long animationElapsedTicks;
	private int animationIndex;
	private boolean contentRendered;
	private boolean animationStarted;

	public VillageScoreboard(AdvancedVillages plugin, Player player, ScoreboardSnapshot snapshot) {
		super(plugin, player);
		this.plugin = plugin;
		apply(snapshot, false);
	}

	public synchronized void reload(ScoreboardSnapshot snapshot) {
		apply(snapshot, isShown());
	}

	@Override
	protected void onUpdate() {
		ScoreboardSnapshot current = this.snapshot;
		if (!this.contentRendered) {
			renderContent(current);
			this.contentRendered = true;
		} else {
			this.contentElapsedTicks += current.getRefreshIntervalTicks();
			if (this.contentElapsedTicks >= current.getUpdateIntervalTicks()) {
				this.contentElapsedTicks %= current.getUpdateIntervalTicks();
				renderContent(current);
			}
		}

		if (current.getTitleFrames().size() < 2) return;
		if (!this.animationStarted) {
			this.animationStarted = true;
			return;
		}

		this.animationElapsedTicks += current.getRefreshIntervalTicks();
		if (this.animationElapsedTicks < current.getAnimationIntervalTicks()) return;

		long steps = this.animationElapsedTicks / current.getAnimationIntervalTicks();
		this.animationElapsedTicks %= current.getAnimationIntervalTicks();
		this.animationIndex = (int) ((this.animationIndex + steps) % current.getTitleFrames().size());
		setTitle(current.getTitleFrames().get(this.animationIndex));
	}

	private void apply(ScoreboardSnapshot snapshot, boolean refresh) {
		this.snapshot = snapshot;
		this.contentElapsedTicks = 0L;
		this.animationElapsedTicks = 0L;
		this.animationIndex = 0;
		this.contentRendered = false;
		this.animationStarted = false;
		setPlaceholderApiEnabled(this.plugin.isPlaceholder());
		if (snapshot.isHideNumbers()) hideNumbers();
		else showNumbers();
		setUpdateInterval(snapshot.getRefreshIntervalTicks());
		setTitle(snapshot.getTitleFrames().get(0));
		if (refresh) refresh();
	}

	private void renderContent(ScoreboardSnapshot snapshot) {
		Player player = getPlayer();
		User user = this.plugin.getUserManager().findByUuid(player.getUniqueId()).orElse(null);
		if (user == null) {
			clearLines();
			return;
		}

		Village village = user.getPresentVillage();
		int lineIndex = 0;
		for (String line : snapshot.renderLines(player, user, village)) {
			String rendered = VillageUtilsManager.replaceWith(player, village, line).toText();
			if (this.plugin.isPlaceholder()) {
				rendered = this.plugin.getPlaceholder().replacePlaceholder(player, rendered);
			}
			setLine(lineIndex++, rendered);
		}

		while (lineIndex < MAX_LINES) clearLine(lineIndex++);
	}
}
