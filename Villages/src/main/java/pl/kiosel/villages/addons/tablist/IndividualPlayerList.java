package pl.kiosel.villages.addons.tablist;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import panda.std.Option;
import pl.kiosel.core.MetaServer;
import pl.kiosel.core.nms.playerlist.PlayerList;
import pl.kiosel.core.nms.playerlist.PlayerListAccessor;
import pl.kiosel.core.nms.playerlist.PlayerListConstants;
import pl.kiosel.core.nms.playerlist.SkinTexture;
import pl.kiosel.core.utils.ColorUtils;
import pl.kiosel.core.utils.MapUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.tablist.utils.NumberRange;
import pl.kiosel.villages.addons.tablist.utils.TablistPage;

import java.util.*;
import java.util.Map.Entry;

public class IndividualPlayerList {

	private final AdvancedVillages plugin;
	private final Player user;
    private final PlayerList playerList;
    private final MetaServer metaServer;

    private final Map<Integer, String> unformattedCells;
    private final int cellCount;
    private final String header;
    private final String footer;

    private final boolean animated;
    private final List<TablistPage> pages;
    private final int pagesCount;

    private final Map<NumberRange, SkinTexture> cellTextures;
    private final int cellPing;

    private int cycle;
    private int currentPage;

    public IndividualPlayerList(AdvancedVillages plugin, Player user, PlayerListAccessor playerListAccessor, MetaServer metaServer,
								Map<Integer, String> unformattedCells, String header, String footer, boolean animated,
								List<TablistPage> pages, Map<NumberRange, SkinTexture> cellTextures,
								int cellPing, boolean fillCells) {
		this.plugin = plugin;
        this.user = user;
        this.metaServer = metaServer;

        this.unformattedCells = new HashMap<>(unformattedCells);
        this.header = header;
        this.footer = footer;
        this.animated = animated;
        this.pages = pages;
        this.pagesCount = pages.size();
        this.cellTextures = cellTextures;
        this.cellPing = cellPing;

        if (!fillCells) {
            Entry<Integer, String> entry = MapUtils.findTheMaximumEntryByKey(unformattedCells);
            if (entry != null) {
                this.cellCount = entry.getKey();
            } else {
                this.cellCount = PlayerListConstants.DEFAULT_CELL_COUNT;
            }
        } else {
            this.cellCount = PlayerListConstants.DEFAULT_CELL_COUNT;
        }

        this.playerList = playerListAccessor.createPlayerList(this.cellCount);
    }

    public void send() {
        Map<Integer, String> unformattedCells = new HashMap<>(this.unformattedCells);
        String header = this.header;
        String footer = this.footer;

        if (this.animated) {
            TablistPage page = this.pages.get(this.currentPage);
            if (page != null) {
                if (page.cells != null)
                    unformattedCells.putAll(page.cells);
                if (page.header != null)
                    header = page.header;
                if (page.footer != null)
                    footer = page.footer;
            }
        }

        String[] preparedCells = this.putVarsPrepareCells(unformattedCells, header, footer);
        String preparedHeader = preparedCells[PlayerListConstants.DEFAULT_CELL_COUNT];
        String preparedFooter = preparedCells[PlayerListConstants.DEFAULT_CELL_COUNT + 1];

        SkinTexture[] preparedCellsTextures = this.putTexturePrepareCells();

        this.metaServer.getPlayer(this.user.getUniqueId()).peek(player ->
				this.playerList.send(player, preparedCells, preparedHeader, preparedFooter, preparedCellsTextures, this.cellPing, Collections.emptySet()));
    }

    void updatePageCycle() {
        if (!this.animated) return;

        this.cycle++;

        int pageCycles = this.pages.get(this.currentPage).cycles;
        if (this.cycle + 1 >= pageCycles) {
            this.cycle = 0;
            this.currentPage++;

            if (this.currentPage >= this.pagesCount)
                this.currentPage = 0;
        }
    }

    private String[] putVarsPrepareCells(Map<Integer, String> tablistPattern, String header, String footer) {
        String[] allCells = new String[PlayerListConstants.DEFAULT_CELL_COUNT + 2]; // Additional two for header/footer
        for (int i = 0; i < this.cellCount; i++) {
            allCells[i] = this.putTop(tablistPattern.getOrDefault(i + 1, ""));
        }

        allCells[PlayerListConstants.DEFAULT_CELL_COUNT] = header;
        allCells[PlayerListConstants.DEFAULT_CELL_COUNT + 1] = footer;

        String mergedCells = Joiner.on("\0").join(allCells).toString();
        return StringUtils.splitPreserveAllTokens(this.putVars(mergedCells), '\0');
    }

    private String putTop(String cell) {
        return plugin.getRankPlaceholdersService().format(this.user, cell, this.user);
    }

    private String putVars(String cell) {
        String formatted = cell;

        Option<Player> playerOption = this.metaServer.getPlayer(this.user.getUniqueId());
        if (playerOption.isEmpty())
            return formatted;

        Player player = playerOption.get();

        formatted = plugin.getTablistPlaceholdersService().format(this.user, formatted, this.user);
        formatted = ColorUtils.color(formatted);
        if (plugin.isPlaceholder())
			formatted = plugin.getPlaceholder().replacePlaceholder(player, formatted);

        return formatted;
    }

    public SkinTexture[] putTexturePrepareCells() {
        SkinTexture[] textures = new SkinTexture[PlayerListConstants.DEFAULT_CELL_COUNT];

        this.cellTextures.forEach((range, texture) -> {
            for (int i = range.getMinRange().intValue(); i <= range.getMaxRange().intValue(); i++) {
                textures[i - 1] = texture;
            }
        });

        return textures;
    }

}
