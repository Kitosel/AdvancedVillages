package pl.kiosel.villages.addons.tablist;

import lombok.Getter;
import pl.kiosel.core.nms.playerlist.PlayerListConstants;
import pl.kiosel.core.nms.playerlist.SkinTexture;
import pl.kiosel.core.utils.NumberRange;

import java.util.*;

/**
 * Complete immutable tablist configuration used by active player sessions.
 */
public final class TablistSnapshot {

	private static final int CELL_LIMIT = PlayerListConstants.DEFAULT_CELL_COUNT;

	@Getter
	private final boolean enabled;
	@Getter
	private final boolean animated;
	@Getter
	private final String header;
	@Getter
	private final String footer;
	@Getter
	private final int cellPing;
	@Getter
	private final int updateInterval;
	private final boolean relationshipColors;
	private final Map<Integer, String> cells;
	@Getter
	private final List<TablistFrame> frames;
	private final SkinTexture[] textures;
	@Getter
	private final int cellCount;

	public TablistSnapshot(boolean enabled, boolean animated, String header, String footer,
	                       int cellPing, boolean fillCells, int updateInterval,
	                       boolean relationshipColors, Map<Integer, String> cells,
	                       List<TablistFrame> frames,
	                       Map<NumberRange, SkinTexture> configuredTextures) {
		this.enabled = enabled;
		this.header = header;
		this.footer = footer;
		this.cellPing = cellPing;
		this.updateInterval = updateInterval;
		this.relationshipColors = relationshipColors;
		this.cells = Collections.unmodifiableMap(new LinkedHashMap<>(cells));
		this.frames = Collections.unmodifiableList(new ArrayList<>(frames));
		this.animated = animated && !this.frames.isEmpty();
		this.textures = createTextures(configuredTextures);
		this.cellCount = determineCellCount(this.cells, this.frames, fillCells);
	}

	public boolean shouldUseRelationshipColors() {
		return this.relationshipColors;
	}

	public String[] createBaseCells() {
		String[] result = new String[CELL_LIMIT];
		Arrays.fill(result, "");
		this.cells.forEach((index, text) -> result[index - 1] = text);
		return result;
	}

	public SkinTexture[] copyTextures() {
		return Arrays.copyOf(this.textures, this.textures.length);
	}

	private static SkinTexture[] createTextures(Map<NumberRange, SkinTexture> configuredTextures) {
		SkinTexture[] result = new SkinTexture[CELL_LIMIT];
		configuredTextures.forEach((range, texture) -> {
			int minimum = Math.max(1, range.getMinRange().intValue());
			int maximum = Math.min(CELL_LIMIT, range.getMaxRange().intValue());
			for (int index = minimum; index <= maximum; index++) {
				result[index - 1] = texture;
			}
		});
		return result;
	}

	private static int determineCellCount(Map<Integer, String> cells,
	                                      List<TablistFrame> frames, boolean fillCells) {
		if (fillCells) {
			return CELL_LIMIT;
		}

		int maximum = cells.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
		for (TablistFrame frame : frames) {
			int frameMaximum = frame.getCells().keySet().stream()
					.mapToInt(Integer::intValue)
					.max()
					.orElse(0);
			maximum = Math.max(maximum, frameMaximum);
		}
		return maximum == 0 ? CELL_LIMIT : maximum;
	}
}
