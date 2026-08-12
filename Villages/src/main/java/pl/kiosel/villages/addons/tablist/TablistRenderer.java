package pl.kiosel.villages.addons.tablist;

import org.bukkit.entity.Player;
import pl.kiosel.core.nms.playerlist.PlayerListConstants;
import pl.kiosel.core.utils.ColorUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;

import java.util.Arrays;
import java.util.regex.Pattern;

/** Builds one complete tablist view and resolves placeholders only once. */
final class TablistRenderer {

	private static final int CELL_LIMIT = PlayerListConstants.DEFAULT_CELL_COUNT;
	private static final String PART_SEPARATOR = "\u0000";
	private static final Pattern PART_SEPARATOR_PATTERN = Pattern.compile(PART_SEPARATOR, Pattern.LITERAL);

	private final AdvancedVillages plugin;
	private final TablistPlaceholdersService placeholders;

	TablistRenderer(AdvancedVillages plugin, TablistPlaceholdersService placeholders) {
		this.plugin = plugin;
		this.placeholders = placeholders;
	}

	TablistView render(TablistSnapshot snapshot, String[] baseCells,
	                   TablistFrame frame, User user, Player player) {
		String[] parts = new String[CELL_LIMIT + 2];
		System.arraycopy(baseCells, 0, parts, 0, CELL_LIMIT);

		String header = snapshot.getHeader();
		String footer = snapshot.getFooter();
		if (frame != null) {
			frame.getCells().forEach((index, text) -> parts[index - 1] = text);
			if (frame.getHeader() != null) {
				header = frame.getHeader();
			}
			if (frame.getFooter() != null) {
				footer = frame.getFooter();
			}
		}

		parts[CELL_LIMIT] = header;
		parts[CELL_LIMIT + 1] = footer;
		String content = this.placeholders.format(String.join(PART_SEPARATOR, parts), user, player);
		if (this.plugin.isPlaceholder()) {
			content = this.plugin.getPlaceholder().replacePlaceholder(player, content);
		}

		String[] rendered = PART_SEPARATOR_PATTERN.split(ColorUtils.color(content), -1);
		if (rendered.length != parts.length) {
			throw new IllegalStateException("Tablist renderer produced an invalid part count: " + rendered.length);
		}

		return new TablistView(
				Arrays.copyOf(rendered, CELL_LIMIT),
				rendered[CELL_LIMIT],
				rendered[CELL_LIMIT + 1]
		);
	}
}
