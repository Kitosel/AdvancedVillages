package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.compatibility.ZSound;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.gui.GUIS;
import pl.kiosel.villages.config.gui.GuiItemConfig;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

import java.util.List;

public final class VillageGUI extends VillageMenu {

	public VillageGUI(AdvancedVillages plugin, VillageGUIManager menus, Village village, Player player) {
		super(plugin, menus, village, player, GUIS.VILLAGE, null);

		GuiItemConfig settings =
				item("village-main.settings", 4, Material.NOTE_BLOCK, "&lSettings", List.of("&7Manage Village"));
		GuiItemConfig members =
				item("village-main.members", 10, Material.PLAYER_HEAD, "&cMembers", List.of("&7Manage members"));
		GuiItemConfig bank =
				item("village-main.bank", 16, Material.SUNFLOWER, "&6Bank", List.of("&7Village bank"));
		GuiItemConfig store =
				item("village-main.store", 24, Material.NETHER_STAR, "&3Store", List.of("&7Village store"));
		GuiItemConfig effects =
				item("village-main.effects", 32, Material.SPLASH_POTION,"&dEffects", List.of("&7Add effects for", "&7Members of village"));
		GuiItemConfig upgrade =
				item("village-main.upgrade", 40, Material.DIAMOND,"&bUpgrade", List.of("&7Upgrade village"));

		GuiItemConfig quests =
				item("village-main.quests", 22, Material.WRITABLE_BOOK, "&eVillage quests", List.of("&7Shared daily and weekly quests"));
		GuiItemConfig specialist =
				item("village-main.specialization", 36, Material.KNOWLEDGE_BOOK,
						"&aSpecializations", List.of("&7Choose your personal village bonus"));
		GuiItemConfig diplomacy =
				item("village-main.diplomacy", 20, Material.IRON_SWORD,
						"&bDiplomacy", List.of("&7Alliances and wars"));
		GuiItemConfig development =
				item("village-main.development", 30, Material.AMETHYST_SHARD,
						"&dDevelopment tree", List.of("&7Choose permanent village bonuses"));

		GuiItemConfig exit =
				item("village-main.exit", 44, Material.ARROW, "&cExit", List.of());

		setItem(settings, true, GUIS.SETTINGS);
		setItem(members, true, GUIS.RESIDENT);
		setItem(bank, plugin.getEconomy() != null, GUIS.BANK);
		setItem(store, true, GUIS.STORE);
		setItem(effects, true, GUIS.EFFECTS);
		setItem(upgrade, true, GUIS.UPGRADE);
		setItem(quests, plugin.getQuestManager().isEnabled(), GUIS.QUESTS);

		if (plugin.isDev()) {
			setItem(specialist, plugin.getSpecializationManager().isEnabled(), GUIS.SPECIALIZATIONS);
			setItem(diplomacy, plugin.getDiplomacyManager().isEnabled(), GUIS.DIPLOMACY);
			setItem(development, plugin.getDevelopmentManager().isEnabled(), GUIS.DEVELOPMENT);
		}

		if (exit.isEnabled())
			setButton(exit.getSlot(), exit.createItem(), event -> {
				playSound(ZSound.BLOCK_ANVIL_BREAK, 0.1f, 2.0f);
				event.getGui().exit();
			});
	}

	private void setItem(GuiItemConfig item, boolean enabled, GUIS type) {
		if (item == null) return;
		if (!enabled) return;
		if (!item.isEnabled()) return;
		if (village.getLevel().getLevel() > plugin.getGuiSettings().menu(type).getRequiredLevel()) {
			setButton(item.getSlot(), item.createItem(), event -> openFromMain(type));
		} else {
			setItem(item.getSlot(), Item.create(Material.BARRIER, "&cX " + item.getName(), item.getLore()));
		}
	}

	private GuiItemConfig item(String path, int slot, Material material, String name, List<String> lore) {
		return item(GUIS.VILLAGE, path, slot, material, name, lore);
	}
}
