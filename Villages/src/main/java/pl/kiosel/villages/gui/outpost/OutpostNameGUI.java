package pl.kiosel.villages.gui.outpost;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.gui.AnvilGui;
import pl.kiosel.rosacore.gui.GuiManager;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.config.Settings;
import pl.kiosel.villages.config.gui.GUIS;
import pl.kiosel.villages.data.outpost.Outpost;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.features.logs.VillageLogType;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.manager.VillageUtils;

public class OutpostNameGUI extends AnvilGui {

	private final AdvancedVillages plugin;
	private final Village village;
	private final Outpost outpost;
	private final Player viewer;

	public OutpostNameGUI(AdvancedVillages plugin, Village village, Outpost outpost, Player player) {
		super(player);
		this.plugin = plugin;
		this.village = village;
		this.outpost = outpost;
		this.viewer = player;

		setTitle(plugin.getGuiSettings().text(GUIS.OUTPOST, "outpost.title", "Name a outpost"));
		setInput(Item.create(Material.NAME_TAG, plugin.getGuiSettings().text(GUIS.OUTPOST, "outpost.input-name", "*NAME*")));
		setOutputPrompt(plugin.getGuiSettings().text(GUIS.OUTPOST, "outpost.output-prompt", "Name your outpost"));
		setAction(event -> submit());
		playSoundOnClick(true);
	}

	@Override
	protected void onClose(GuiManager manager, Player player) {
		this.plugin.getVillageMessages().get(Lang.TAG_NO_SET_VILLAGE).sendPrefixed(player);
	}

	private void submit() {
		String input = getInputText();
		String tag = input == null ? "" : input.trim();
		int maxLength = Settings.VILLAGE_MAX_TAG_LENGTH.getInt();

		VillageUtils.ValidTag valid = VillageUtils.validate(tag, maxLength);
		switch (valid) {
			case OCCUPIED:
				setInputText(message(Lang.TAG_TRY_AGAIN));
				break;
			case TOO_LONG:
				setInputText(message(Lang.TAG_TOO_LONG));
				break;
			case EMPTY:
				setInputText(message(Lang.TAG_EMPTY));
				break;
			case HAS_SPACES:
				setInputText(message(Lang.TAG_SPACES));
				break;
			case INVALID_CHARACTERS:
				setInputText(message(Lang.TAG_INVALID_CHARS));
				break;
			case VALID:
				outpost.setName(tag);
				plugin.getLogManager().record(village, VillageLogType.OUTPOST_CREATED, viewer,
						"setting", "name", "value", tag);
				plugin.getVillageMessages().get(Lang.TAG_NEW_VILLAGE)
						.with("tag", tag).sendPrefixed(viewer);
				exit();
				break;
		}
	}

	private String message(Lang message) {
		return plugin.getVillageMessages().get(message).toText();
	}
}
