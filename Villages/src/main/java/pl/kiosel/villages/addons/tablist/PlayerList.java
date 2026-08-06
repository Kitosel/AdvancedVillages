package pl.kiosel.villages.addons.tablist;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import panda.std.Option;
import pl.kiosel.core.MetaServer;
import pl.kiosel.core.nms.playerlist.PlayerListAccessor;
import pl.kiosel.core.nms.playerlist.PlayerListConstants;
import pl.kiosel.core.nms.playerlist.SkinTexture;
import pl.kiosel.core.utils.ColorUtils;
import pl.kiosel.core.utils.NumberRange;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.TablistConfiguration;
import pl.kiosel.villages.data.user.User;

import java.util.*;

public final class PlayerList {

	private static final int CELL_LIMIT = PlayerListConstants.DEFAULT_CELL_COUNT;

	private final AdvancedVillages plugin;
	private final User user;
	private final pl.kiosel.core.nms.playerlist.PlayerList playerList;
	private final MetaServer metaServer;

	private final String[] baseCells = new String[CELL_LIMIT];
	private final String header;
	private final String footer;
	private final boolean animated;
	private final List<TablistPage> pages;
	private final SkinTexture[] cellTextures;
	private final int cellPing;
	private final int updateInterval;

	private long pageTicks;
	private int currentPage;
	private String[] lastCells;
	private String lastHeader;
	private String lastFooter;

	public PlayerList(AdvancedVillages plugin, User user, PlayerListAccessor playerListAccessor,
	                  MetaServer metaServer, TablistConfiguration config) {
		this.plugin = plugin;
		this.user = user;
		this.metaServer = metaServer;
		this.header = config.getHeader();
		this.footer = config.getFooter();
		this.pages = config.getPages();
		this.animated = config.isAnimated() && !this.pages.isEmpty();
		this.cellPing = config.getCellsPing();
		this.updateInterval = config.getUpdateInterval();

		Arrays.fill(this.baseCells, "");
		config.getCells().forEach((index, text) -> this.baseCells[index - 1] = text);
		int cellCount = determineCellCount(config.getCells(), this.pages, config.shouldFillCells());
		this.cellTextures = prepareTextures(config.getCellTextures());
		this.playerList = playerListAccessor.createPlayerList(cellCount);
	}

	public void send() {
		Option<Player> playerOption = this.metaServer.getPlayer(this.user.getUUID());
		if (playerOption.isEmpty()) {
			return;
		}

		String[] prepared = this.prepare(playerOption.get());
		String preparedHeader = prepared[CELL_LIMIT];
		String preparedFooter = prepared[CELL_LIMIT + 1];
		String[] preparedCells = Arrays.copyOf(prepared, CELL_LIMIT);

		if (Arrays.equals(this.lastCells, preparedCells)
				&& Objects.equals(this.lastHeader, preparedHeader)
				&& Objects.equals(this.lastFooter, preparedFooter)) {
			return;
		}

		this.playerList.send(
				playerOption.get(),
				preparedCells,
				preparedHeader,
				preparedFooter,
				this.cellTextures,
				this.cellPing,
				Collections.emptySet()
		);
		this.lastCells = preparedCells;
		this.lastHeader = preparedHeader;
		this.lastFooter = preparedFooter;
	}

	public void updatePageCycle() {
		if (!this.animated) {
			return;
		}

		this.pageTicks += this.updateInterval;
		TablistPage page = this.pages.get(this.currentPage);
		if (this.pageTicks >= page.getDurationTicks()) {
			this.pageTicks = 0L;
			this.currentPage = (this.currentPage + 1) % this.pages.size();
		}
	}

	private String[] prepare(Player player) {
		String[] values = new String[CELL_LIMIT + 2];
		Arrays.fill(values, "");
		System.arraycopy(this.baseCells, 0, values, 0, CELL_LIMIT);

		String currentHeader = this.header;
		String currentFooter = this.footer;
		if (this.animated) {
			TablistPage page = this.pages.get(this.currentPage);
			page.getCells().forEach((index, text) -> values[index - 1] = text);
			if (page.getHeader() != null) {
				currentHeader = page.getHeader();
			}
			if (page.getFooter() != null) {
				currentFooter = page.getFooter();
			}
		}

		values[CELL_LIMIT] = currentHeader;
		values[CELL_LIMIT + 1] = currentFooter;

		String formatted = String.join("\0", values);
		formatted = this.plugin.getTablistPlaceholdersService().format(formatted, this.user, player);
		if (this.plugin.isPlaceholder()) {
			formatted = this.plugin.getPlaceholder().replacePlaceholder(player, formatted);
		}
		formatted = ColorUtils.color(formatted);

		return StringUtils.splitPreserveAllTokens(formatted, '\0');
	}

	private static int determineCellCount(Map<Integer, String> cells, List<TablistPage> pages, boolean fillCells) {
		if (fillCells) {
			return CELL_LIMIT;
		}

		int maximum = cells.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
		for (TablistPage page : pages) {
			maximum = Math.max(maximum, page.getCells().keySet().stream().mapToInt(Integer::intValue).max().orElse(0));
		}
		return maximum == 0 ? CELL_LIMIT : maximum;
	}

	private static SkinTexture[] prepareTextures(Map<NumberRange, SkinTexture> configuredTextures) {
		SkinTexture[] textures = new SkinTexture[CELL_LIMIT];
		configuredTextures.forEach((range, texture) -> {
			int minimum = Math.max(1, range.getMinRange().intValue());
			int maximum = Math.min(CELL_LIMIT, range.getMaxRange().intValue());
			for (int index = minimum; index <= maximum; index++) {
				textures[index - 1] = texture;
			}
		});
		return textures;
	}
}
