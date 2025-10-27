package pl.kiosel.villages.gui.inventory;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import pl.kiosel.villages.Wioski;
import pl.kiosel.villages.config.Config;
import pl.kiosel.villages.config.Language;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.common.Item;
import pl.kiosel.villages.village.DataManager;
import pl.kiosel.villages.village.Village;

import java.util.Collections;

public class TagInventory extends Item {

	public AnvilGUI.Builder openInventory(Wioski plugin, Village village) {
		Language lang = plugin.getLang();

		return new AnvilGUI.Builder()
				.onClose(state -> {
					if (village.getTag() == null || village.getTag().isEmpty()) {
						state.getPlayer().sendMessage(lang.getMessage(Lang.TAG_NO_SET_VILLAGE));
					}
				})
				.onClick((slot, state) -> {
					if (slot != AnvilGUI.Slot.OUTPUT) {
						return Collections.emptyList();
					}
					String tag = state.getText().trim();
					int maxTagLength = Config.max_tag_length;
					if (maxTagLength==0)
						maxTagLength = Integer.MAX_VALUE;

					if (tag.contains(" ")) {
						return Collections.singletonList(
								AnvilGUI.ResponseAction.replaceInputText(lang.getMessage(Lang.TAG_SPACES))
						);
					}

					if (tag.length() > maxTagLength) {
						return Collections.singletonList(
								AnvilGUI.ResponseAction.replaceInputText(lang.getMessage(Lang.TAG_TOO_LONG))
						);
					}

					if (!tag.matches("^[a-zA-Z0-9_]+$")) {
						return Collections.singletonList(
								AnvilGUI.ResponseAction.replaceInputText(lang.getMessage(Lang.TAG_INVALID_CHARS))
						);
					}

					DataManager playerDataManager = Wioski.getInstance().getPlayerDataManager();

					for (Village village2 : playerDataManager.getVillages().values()) {
						String existingTag = village2.getTag();
						if (existingTag != null && existingTag.equalsIgnoreCase(tag)) {
							state.getPlayer().sendMessage(lang.getMessage(Lang.TAG_ALREADY_SET_VILLAGE));
							return Collections.singletonList(
									AnvilGUI.ResponseAction.replaceInputText(lang.getMessage(Lang.TAG_TRY_AGAIN))
							);
						}
					}
					village.setTag(tag);
					state.getPlayer().sendMessage(lang.getMessage(Lang.TAG_NEW_VILLAGE).replace("%TAG%", tag));

					return Collections.singletonList(AnvilGUI.ResponseAction.close());
				})
				.text("*NAME*")
				.itemLeft(create(Material.NAME_TAG, "Village"))
				.title(lang.getMessage(Lang.ANVIL_NAME))
				.plugin(plugin);
	}
}
