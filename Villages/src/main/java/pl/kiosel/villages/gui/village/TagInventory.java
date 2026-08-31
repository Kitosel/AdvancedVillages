package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.gui.AnvilGui;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.rosacore.gui.GuiManager;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.logs.VillageLogType;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.config.Settings;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.gui.Item;

public final class TagInventory extends AnvilGui {

	private final AdvancedVillages plugin;
	private final Village village;
	private final Player viewer;

	public TagInventory(AdvancedVillages plugin, Village village, Player player, Gui parent) {
		super(player, parent);
		this.plugin = plugin;
		this.village = village;
		this.viewer = player;

		setTitle(plugin.getGuiSettings().text("guis.tag.title", "Name your village"));
		setInput(Item.create(Material.NAME_TAG,
				plugin.getGuiSettings().text("guis.tag.input-name", "*NAME*")));
		setOutputPrompt(plugin.getGuiSettings().text("guis.tag.output-prompt", "Name your village"));
		setAction(event -> submit());
	}

	@Override
	protected void onClose(GuiManager manager, Player player) {
		if (!this.village.isTag()) {
			this.plugin.getVillageMessages().get(Lang.TAG_NO_SET_VILLAGE).sendPrefixed(player);
		}
	}

	private void submit() {
		String input = getInputText();
		String tag = input == null ? "" : input.trim();
		int maxLength = Settings.VILLAGE_MAX_TAG_LENGTH.getInt();
		if (tag.contains(" ")) {
			message(Lang.TAG_SPACES);
			return;
		}
		if (maxLength > 0 && tag.length() > maxLength) {
			message(Lang.TAG_TOO_LONG);
			return;
		}
		if (!tag.matches("^[a-zA-Z0-9_]+$")) {
			message(Lang.TAG_INVALID_CHARS);
			return;
		}
		boolean occupied = plugin.getVillageManager().tagExists(tag);
		if (occupied) {
			message(Lang.TAG_ALREADY_SET_VILLAGE);
			return;
		}

		village.setTag(tag);
		plugin.getLogManager().record(village, VillageLogType.SETTING_CHANGED, viewer,
				"setting", "tag", "value", tag);
		plugin.getVillageMessages().get(Lang.TAG_NEW_VILLAGE)
				.with("tag", tag).sendPrefixed(viewer);
		if (getParent() != null) {
			plugin.getGuiManager().showGUI(viewer, getParent());
		} else {
			exit();
		}
	}

	private void message(Lang message) {
		plugin.getVillageMessages().get(message).sendPrefixed(viewer);
	}
}
