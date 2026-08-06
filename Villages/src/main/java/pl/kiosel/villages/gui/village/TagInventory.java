package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.kiosel.core.gui.AnvilGui;
import pl.kiosel.core.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.settings.Settings;

public final class TagInventory extends AnvilGui {

	private final AdvancedVillages plugin;
	private final Village village;
	private final Player viewer;

	public TagInventory(AdvancedVillages plugin, Village village, Player player, Gui parent) {
		super(player, parent);
		this.plugin = plugin;
		this.village = village;
		this.viewer = player;

		setTitle(plugin.getLocale().getMessage(Lang.ANVIL_NAME.getPath()).toString());
		setInput(Item.create(Material.NAME_TAG, "*NAME*"));
		setOutputPrompt(plugin.getLocale().getMessage(Lang.ANVIL_NAME.getPath()).toString());
		setAction(event -> submit());
		setOnClose(event -> {
			if (!village.isTag()) {
				plugin.getLocale().getMessage(Lang.TAG_NO_SET_VILLAGE.getPath()).sendPrefixedMessage(event.player);
			}
		});
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
		boolean occupied = plugin.getVillageManager().getVillages().stream()
				.map(Village::getTag)
				.anyMatch(existing -> existing != null && existing.equalsIgnoreCase(tag));
		if (occupied) {
			message(Lang.TAG_ALREADY_SET_VILLAGE);
			return;
		}

		village.setTag(tag);
		plugin.getLocale().getMessage(Lang.TAG_NEW_VILLAGE.getPath())
				.processPlaceholder("tag", tag).sendPrefixedMessage(viewer);
		if (getParent() != null) {
			plugin.getGuiManager().showGUI(viewer, getParent());
		} else {
			exit();
		}
	}

	private void message(Lang message) {
		plugin.getLocale().getMessage(message.getPath()).sendPrefixedMessage(viewer);
	}
}
