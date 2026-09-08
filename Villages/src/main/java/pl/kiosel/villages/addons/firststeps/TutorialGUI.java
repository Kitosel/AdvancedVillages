package pl.kiosel.villages.addons.firststeps;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.compatibility.RosaSound;
import pl.kiosel.rosacore.compatibility.ZSound;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.config.GuiMenuConfig;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.gui.GUIS;
import pl.kiosel.villages.gui.Item;

import java.util.ArrayList;
import java.util.List;

public final class TutorialGUI extends Gui {

	private final AdvancedVillages plugin;
	private final TutorialSession session;

	public TutorialGUI(AdvancedVillages plugin, boolean onJoin) {
		this.plugin = plugin;
		this.session = new TutorialSession(plugin, onJoin);

		GuiMenuConfig menu = plugin.getGuiSettings().menu(GUIS.TUTORIAL);
		setRows(menu.getRows());
		setTitle(menu.getTitle());
		setDefaultItem(Item.blank(Item.Blank.WHITE));
		playSoundOnClick(true);

		this.addCategory(TutorialSetting.Category.MAIN, 10, Material.COMPARATOR,
				"&eMain settings", List.of("&7Configure the basic plugin behavior."));
		this.addCategory(TutorialSetting.Category.ADDONS, 12, Material.WRITABLE_BOOK,
				"&6Addons", List.of("&7Enable or disable optional modules."));
		this.addCategory(TutorialSetting.Category.FEATURES, 14, Material.EMERALD,
				"&aVillage features", List.of("&7Choose the mechanics available to villages."));

		GuiItemConfig info = this.item("info", 16, Material.KNOWLEDGE_BOOK,
				"&bInformation", List.of("&7Click to see additional information."));
		setButton(info.getSlot(), info.createItem(),
				new RosaSound.SoundHolder(ZSound.ENTITY_PLAYER_LEVELUP, 1f, 2f),
				event -> this.sendInfo(event.getPlayer()));
		this.refreshSaveButton();
	}

	public void refreshSaveButton() {
		GuiItemConfig save = this.item("save", 22, Material.LIME_DYE,
				"&a&lSave and quit", List.of("%changes%", "&7The plugin will reload once."));
		String changes = this.plugin.getGuiSettings().text(
				"guis.tutorial.status." + (this.session.isDirty() ? "unsaved" : "unchanged"),
				this.session.isDirty() ? "&eYou have unsaved changes." : "&7No settings were changed."
		);
		List<String> lore = new ArrayList<>(save.getLore().size());
		for (String line : save.getLore())
			lore.add(line.replace("%changes%", changes));
		setButton(save.getSlot(), save.createItem(save.getName(), lore, this.session.isDirty()), event -> {
			if (!this.session.save()) {
				this.plugin.getVillageMessages().sendPrefixed(event.getPlayer(), Lang.TUTORIAL_SAVE_FAILED);
				return;
			}
			this.plugin.getVillageMessages().sendPrefixed(event.getPlayer(), Lang.TUTORIAL_SAVED);
			this.exit();
		});
	}

	private void addCategory(TutorialSetting.Category category, int slot, Material material,
						 String name, List<String> lore) {
		GuiItemConfig item = this.item("categories." + category.getId(), slot, material, name, lore);
		setButton(item.getSlot(), item.createItem(), event -> event.getManager().openGUI(
				event.getPlayer(), new TutorialSettingsGUI(this.plugin, this, this.session, category)));
	}

	private GuiItemConfig item(String path, int slot, Material material,
						   String name, List<String> lore) {
		return this.plugin.getGuiSettings().item(
				GUIS.TUTORIAL, "guis.tutorial." + path, slot, material, name, lore);
	}

	private void sendInfo(Player player) {
		for (String line : this.plugin.getGuiSettings().list("guis.tutorial.info.lines", List.of(
				"&8!&7------------------------------------&8!",
				"&7This wizard configures the most important options.",
				"&7You can edit every setting later in the config files.",
				"&7Some changes require a complete server restart.",
				"&8!&7------------------------------------&8!"
		))) player.sendMessage(line);
	}
}
