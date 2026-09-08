package pl.kiosel.villages.addons.firststeps;

import org.bukkit.inventory.ItemStack;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.rosacore.gui.GuiClickEvent;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.config.GuiMenuConfig;
import pl.kiosel.villages.gui.GUIS;
import pl.kiosel.villages.gui.Item;

import java.util.ArrayList;
import java.util.List;

final class TutorialSettingsGUI extends Gui {

	private final AdvancedVillages plugin;
	private final TutorialGUI root;
	private final TutorialSession session;

	TutorialSettingsGUI(AdvancedVillages plugin, TutorialGUI root, TutorialSession session,
						TutorialSetting.Category category) {
		super(root);
		this.plugin = plugin;
		this.root = root;
		this.session = session;

		GuiMenuConfig menu = plugin.getGuiSettings().menu(GUIS.TUTORIAL);
		setRows(menu.getRows());
		setTitle(plugin.getGuiSettings().text("guis.tutorial.titles." + category.getId(),
				category.getFallbackTitle()));
		setDefaultItem(Item.blank(Item.Blank.WHITE));
		playSoundOnClick(true);
		setButton(menu.getBackSlot(), Item.blank(Item.Blank.BACK),
				event -> event.getManager().openGUI(event.getPlayer(), root));

		for (TutorialSetting setting : TutorialSetting.values()) {
			if (setting.getCategory() == category)
				this.addSetting(setting);
		}
	}

	void addSetting(TutorialSetting setting) {
		GuiItemConfig item = this.item(setting);
		setButton(item.getSlot(), this.createItem(item, setting),
				event -> this.changeSetting(event, setting));
	}

	void refresh(TutorialSetting setting) {
		GuiItemConfig item = this.item(setting);
		setItem(item.getSlot(), this.createItem(item, setting));
		this.root.refreshSaveButton();
	}

	private GuiItemConfig item(TutorialSetting setting) {
		return this.plugin.getGuiSettings().item(
				GUIS.TUTORIAL,
				"guis.tutorial.settings." + setting.getId(),
				setting.getSlot(),
				setting.getMaterial(),
				setting.getId(),
				List.of("%value%", "&7Click to change")
		);
	}

	private void changeSetting(GuiClickEvent event, TutorialSetting setting) {
		if (setting.getValueType() == TutorialSetting.ValueType.BOOLEAN) {
			this.session.toggle(setting);
			this.refresh(setting);
			return;
		}
		if (setting.hasOptions()) {
			event.getManager().showGUI(event.getPlayer(),
					new TutorialOptionsGUI(this.plugin, this.session, setting, this));
			return;
		}
		event.getManager().showGUI(event.getPlayer(),
				new TutorialValueGUI(this.plugin, event.getPlayer(), this.session, setting, this));
	}

	private ItemStack createItem(GuiItemConfig item, TutorialSetting setting) {
		Object selected = this.session.get(setting);
		boolean enabled = setting.getValueType() == TutorialSetting.ValueType.BOOLEAN
				&& Boolean.TRUE.equals(selected);
		String value;
		if (setting.getValueType() == TutorialSetting.ValueType.BOOLEAN) {
			value = this.plugin.getGuiSettings().text(
					"guis.tutorial.status." + (enabled ? "enabled" : "disabled"),
					enabled ? "&aEnabled" : "&cDisabled");
		} else {
			value = this.plugin.getGuiSettings().text("guis.tutorial.status.value", "&f%value%")
					.replace("%value%", setting.display(selected));
		}
		String name = replace(item.getName(), setting, value);
		List<String> lore = new ArrayList<>(item.getLore().size());
		for (String line : item.getLore())
			lore.add(replace(line, setting, value));
		return item.createItem(name, lore, enabled);
	}

	private static String replace(String text, TutorialSetting setting, String value) {
		return text.replace("%status%", value)
				.replace("%value%", value)
				.replace("%type%", setting.getValueType().getId());
	}
}
