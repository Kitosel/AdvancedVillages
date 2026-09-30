package pl.kiosel.villages.config.gui;

import lombok.Getter;

@Getter
public final class GuiMenuConfig {

	private final String id;
	private final String title;
	private final int rows;
	private final int backSlot;
	private final int requiredLevel;

	GuiMenuConfig(String id, String title, int rows, int backSlot, int requiredLevel) {
		this.id = id;
		this.title = title;
		this.rows = rows;
		this.backSlot = backSlot;
		this.requiredLevel = requiredLevel;
	}

	public int getSize() {
		return this.rows * 9;
	}

	public int validSlot(int requested, int fallback) {
		int safeFallback = Math.max(0, Math.min(this.getSize() - 1, fallback));
		return requested >= 0 && requested < this.getSize() ? requested : safeFallback;
	}
}
