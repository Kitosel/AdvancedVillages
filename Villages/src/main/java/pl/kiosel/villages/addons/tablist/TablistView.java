package pl.kiosel.villages.addons.tablist;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

final class TablistView {

	@Getter private final String[] cells;
	@Getter private final String header;
	@Getter private final String footer;

	TablistView(String[] cells, String header, String footer) {
		this.cells = cells;
		this.header = header;
		this.footer = footer;
	}

	boolean hasSameContent(TablistView other) {
		return other != null
				&& Arrays.equals(this.cells, other.cells)
				&& Objects.equals(this.header, other.header)
				&& Objects.equals(this.footer, other.footer);
	}
}
