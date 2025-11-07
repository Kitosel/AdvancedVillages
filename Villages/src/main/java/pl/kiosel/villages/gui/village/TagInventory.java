package pl.kiosel.villages.gui.village;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import pl.kiosel.core.locale.Locale;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.settings.Settings;
import pl.kiosel.villages.manager.VillageDataManager;
import pl.kiosel.villages.data.village.Village;

import java.util.Collections;

public class TagInventory extends Item {

	public AnvilGUI.Builder openInventory(AdvancedVillages plugin, Village village) {
		Locale locale = plugin.getLocale();

		return new AnvilGUI.Builder()
				.onClose(state -> {
					if (village.getTag() == null || village.getTag().isEmpty()) {
						locale.getMessage(Lang.TAG_NO_SET_VILLAGE.getPath()).sendPrefixedMessage(state.getPlayer());
					}
				})
				.onClick((slot, state) -> {
					if (slot != AnvilGUI.Slot.OUTPUT) {
						return Collections.emptyList();
					}
					String tag = state.getText().trim();
					int maxTagLength = Settings.VILLAGE_MAX_TAG_LENGTH.getInt();
					if (maxTagLength==0)
						maxTagLength = Integer.MAX_VALUE;

					if (tag.contains(" ")) {
						return Collections.singletonList(
								AnvilGUI.ResponseAction.replaceInputText(locale.getMessage(Lang.TAG_SPACES.getPath()).toText())
						);
					}

					if (tag.length() > maxTagLength) {
						return Collections.singletonList(
								AnvilGUI.ResponseAction.replaceInputText(locale.getMessage(Lang.TAG_TOO_LONG.getPath()).toText())
						);
					}

					if (!tag.matches("^[a-zA-Z0-9_]+$")) {
						return Collections.singletonList(
								AnvilGUI.ResponseAction.replaceInputText(locale.getMessage(Lang.TAG_INVALID_CHARS.getPath()).toText())
						);
					}

					VillageDataManager playerDataManager = AdvancedVillages.getInstance().getVillageDataManager();

					for (Village village2 : playerDataManager.getVillages().values()) {
						String existingTag = village2.getTag();
						if (existingTag != null && existingTag.equalsIgnoreCase(tag)) {
							locale.getMessage(Lang.TAG_ALREADY_SET_VILLAGE.getPath()).sendPrefixedMessage(state.getPlayer());
							return Collections.singletonList(
									AnvilGUI.ResponseAction.replaceInputText(locale.getMessage(Lang.TAG_TRY_AGAIN.getPath()).toText())
							);
						}
					}
					village.setTag(tag);
					locale.getMessage(Lang.TAG_NEW_VILLAGE.getPath()).processPlaceholder("tag", tag).sendPrefixedMessage(state.getPlayer());

					return Collections.singletonList(AnvilGUI.ResponseAction.close());
				})
				.text("*NAME*")
				.itemLeft(create(Material.NAME_TAG, "Village"))
				.title(locale.getMessage(Lang.ANVIL_NAME.getPath()).toString())
				.plugin(plugin);
	}
}
