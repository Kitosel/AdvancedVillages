package pl.kiosel.villages.addons.tablist;

import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class TablistPage {

    private final long durationTicks;
    private final Map<Integer, String> cells;
    private final String header;
    private final String footer;

    public TablistPage(long durationTicks, Map<Integer, String> cells, String header, String footer) {
        this.durationTicks = Math.max(1L, durationTicks);
        this.cells = Collections.unmodifiableMap(new LinkedHashMap<>(cells));
        this.header = header;
        this.footer = footer;
    }

}
