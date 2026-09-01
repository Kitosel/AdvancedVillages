package pl.kiosel.villages.addons.tablist;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public final class TablistFrame {

	private final long durationTicks;
	private final Map<Integer, String> cells;
	@Nullable private final String header;
	@Nullable private final String footer;

	public TablistFrame(long durationTicks, Map<Integer, String> cells,
	                    @Nullable String header, @Nullable String footer) {
		this.durationTicks = Math.max(1L, durationTicks);
		this.cells = Collections.unmodifiableMap(new LinkedHashMap<>(cells));
		this.header = header;
		this.footer = footer;
	}

}
