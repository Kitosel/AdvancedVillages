package pl.kiosel.villages.manager.teleport;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.village.Village;

final class TeleportSession {

	private final TeleportManager.TeleportType type;
	private final Location destination;
	private final Village village;
	private final long delaySeconds;
	private final long cooldownSeconds;
	private final boolean cancelOnMove;
	private final int chargedCost;
	private final boolean showTitle;
	private final boolean animatedTitle;
	private final Lang title;
	private final Lang subtitle;
	private BukkitTask task;

	TeleportSession(TeleportManager.TeleportType type, Location destination, Village village,
	                long delaySeconds, long cooldownSeconds, boolean cancelOnMove,
	                int chargedCost, boolean showTitle, boolean animatedTitle,
	                Lang title, Lang subtitle) {
		this.type = type;
		this.destination = destination.clone();
		this.village = village;
		this.delaySeconds = delaySeconds;
		this.cooldownSeconds = cooldownSeconds;
		this.cancelOnMove = cancelOnMove;
		this.chargedCost = chargedCost;
		this.showTitle = showTitle;
		this.animatedTitle = animatedTitle;
		this.title = title;
		this.subtitle = subtitle;
	}

	TeleportManager.TeleportType getType() {
		return this.type;
	}

	Location getDestination() {
		return this.destination.clone();
	}

	Village getVillage() {
		return this.village;
	}

	long getDelaySeconds() {
		return this.delaySeconds;
	}

	long getCooldownSeconds() {
		return this.cooldownSeconds;
	}

	boolean isCancelOnMove() {
		return this.cancelOnMove;
	}

	int getChargedCost() {
		return this.chargedCost;
	}

	boolean shouldShowTitle() {
		return this.showTitle;
	}

	boolean isAnimatedTitle() {
		return this.animatedTitle;
	}

	Lang getTitle() {
		return this.title;
	}

	Lang getSubtitle() {
		return this.subtitle;
	}

	void setTask(BukkitTask task) {
		this.task = task;
	}

	void cancelTask() {
		if (this.task != null) {
			this.task.cancel();
			this.task = null;
		}
	}
}
