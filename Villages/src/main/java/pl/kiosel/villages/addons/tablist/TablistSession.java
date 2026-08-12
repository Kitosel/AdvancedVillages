package pl.kiosel.villages.addons.tablist;

import org.bukkit.entity.Player;
import pl.kiosel.core.nms.playerlist.SkinTexture;
import pl.kiosel.villages.data.user.User;

import java.util.Collections;
import java.util.List;

final class TablistSession {

	private final User user;
	private final pl.kiosel.core.nms.playerlist.PlayerList transport;
	private final TablistRenderer renderer;
	private final TablistSnapshot snapshot;
	private final String[] baseCells;
	private final SkinTexture[] textures;
	private final List<TablistFrame> frames;

	private int frameIndex;
	private long frameTicks;
	private TablistView lastView;

	TablistSession(User user, pl.kiosel.core.nms.playerlist.PlayerList transport,
	               TablistRenderer renderer, TablistSnapshot snapshot) {
		this.user = user;
		this.transport = transport;
		this.renderer = renderer;
		this.snapshot = snapshot;
		this.baseCells = snapshot.createBaseCells();
		this.textures = snapshot.copyTextures();
		this.frames = snapshot.getFrames();
	}

	void sendInitial(Player player) {
		this.renderAndSend(player);
	}

	void tick(Player player) {
		this.advanceAnimation();
		this.renderAndSend(player);
	}

	void clear(Player player) {
		this.transport.clear(player);
		this.lastView = null;
	}

	private void advanceAnimation() {
		if (!this.snapshot.isAnimated()) {
			return;
		}

		this.frameTicks += this.snapshot.getUpdateInterval();
		long duration = this.frames.get(this.frameIndex).getDurationTicks();
		while (this.frameTicks >= duration) {
			this.frameTicks -= duration;
			this.frameIndex = (this.frameIndex + 1) % this.frames.size();
			duration = this.frames.get(this.frameIndex).getDurationTicks();
		}
	}

	private void renderAndSend(Player player) {
		TablistFrame frame = this.snapshot.isAnimated() ? this.frames.get(this.frameIndex) : null;
		TablistView view = this.renderer.render(this.snapshot, this.baseCells, frame, this.user, player);
		if (view.hasSameContent(this.lastView)) {
			return;
		}

		this.transport.send(
				player,
				view.getCells(),
				view.getHeader(),
				view.getFooter(),
				this.textures,
				this.snapshot.getCellPing(),
				Collections.emptySet()
		);
		this.lastView = view;
	}
}
