package pl.kiosel.villages.addons.firststeps;

import org.bukkit.entity.Player;
import pl.kiosel.rosacore.gui.PagedGui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.gui.Item;

import java.util.ArrayList;
import java.util.List;

final class TutorialOptionsGUI extends PagedGui {

	private final AdvancedVillages plugin;
	private final TutorialSession session;
	private final TutorialSetting setting;
	private final TutorialSettingsGUI parent;

	TutorialOptionsGUI(AdvancedVillages plugin, TutorialSession session,
					   TutorialSetting setting, TutorialSettingsGUI parent) {
		super(parent);
		this.plugin = plugin;
		this.session = session;
		this.setting = setting;
		this.parent = parent;

		setTitle(replace(plugin.getGuiSettings().text(
				"guis.tutorial.options.title", "&8Choose %setting%"), ""));
		setFooterButton(1, Item.blank(Item.Blank.BACK),
				event -> event.getManager().showGUI(event.getPlayer(), parent));
		setPreviousPageItem(Item.blank(Item.Blank.PREVIUS_PAGE));
		setNextPageItem(Item.blank(Item.Blank.NEXT_PAGE));
		playSoundOnClick(true);

		List<TutorialSetting.Option> options = setting.options(plugin);
		int selectedIndex = -1;
		for (int index = 0; index < options.size(); index++) {
			TutorialSetting.Option option = options.get(index);
			boolean selected = setting.display(session.get(setting)).equalsIgnoreCase(option.getValue());
			if (selected) selectedIndex = index;
			List<String> lore = this.lore(option, selected);
			String name = replace(plugin.getGuiSettings().text(
					"guis.tutorial.options.name", "&f%value%"), option.getValue());
			if (option.isAvailable()) {
				setButton(index, Item.create(setting.getMaterial(), name, lore, selected),
						event -> this.select(event.getPlayer(), option));
			} else {
				setItem(index, Item.create(setting.getMaterial(), name, lore, false));
			}
		}
		if (selectedIndex >= 0) {
			int contentRows = getRows() - (usesHeader() ? 1 : 0) - (usesFooter() ? 1 : 0);
			setPage(selectedIndex / (contentRows * 9) + 1);
		}
	}

	private void select(Player player, TutorialSetting.Option option) {
		this.session.set(this.setting, option.getValue());
		this.parent.refresh(this.setting);
		this.plugin.getGuiManager().showGUI(player, this.parent);
	}

	private List<String> lore(TutorialSetting.Option option, boolean selected) {
		String state;
		if (!option.isAvailable()) state = "unavailable";
		else state = selected ? "selected" : "available";
		List<String> configured = this.plugin.getGuiSettings().list(
				"guis.tutorial.options." + state,
				java.util.Collections.singletonList(selected ? "&aSelected"
						: option.isAvailable() ? "&eClick to select" : "&cPlugin unavailable"));
		List<String> lore = new ArrayList<>(configured.size());
		for (String line : configured) lore.add(replace(line, option.getValue()));
		return lore;
	}

	private String replace(String text, String value) {
		return text.replace("%setting%", this.setting.getId())
				.replace("%value%", value == null ? "" : value);
	}
}
