package pl.kiosel.villages.addons.tablist;

import java.util.Arrays;
import java.util.Objects;

/** Rendered tablist content ready to be sent through MetaCore NMS. */
final class TablistView {

	private final String[] cells;
	private final String header;
	private final String footer;

	TablistView(String[] cells, String header, String footer) {
		this.cells = cells;
		this.header = header;
		this.footer = footer;
	}

	String[] getCells() {
		return this.cells;
	}

	String getHeader() {
		return this.header;
	}

	String getFooter() {
		return this.footer;
	}

	boolean hasSameContent(TablistView other) {
		return other != null
				&& Arrays.equals(this.cells, other.cells)
				&& Objects.equals(this.header, other.header)
				&& Objects.equals(this.footer, other.footer);
	}
}
