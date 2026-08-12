package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.core.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.logs.VillageLogEntry;
import pl.kiosel.villages.addons.logs.VillageLogType;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

import java.time.format.DateTimeFormatter;
import java.util.*;

public final class VillageLogInventory extends VillageMenu {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

	public VillageLogInventory(AdvancedVillages plugin, VillageGUIManager menus, Village village,
	                           Player player, Gui parent) {
		super(plugin, menus, village, player, GUIS.LOGS, parent, true);
		addBackButton();

		if (!plugin.getLogManager().canView(player, village)) {
			setItem(13, Item.create(Material.BARRIER, messages.text(Lang.VILLAGE_NO_PERMISSION)));
			return;
		}

		List<VillageLogEntry> entries = plugin.getLogManager().getEntries(village);
		if (entries.isEmpty()) {
			setItem(13, Item.create(Material.PAPER, messages.text(Lang.LOGS_GUI_NONE)));
			return;
		}

		int slot = 9;
		for (VillageLogEntry entry : entries) {
			setItem(slot++, this.createEntryItem(entry));
		}
	}

	private ItemStack createEntryItem(VillageLogEntry entry) {
		String key = entry.getType().getConfigKey();
		String actor = entry.getActorName().isBlank()
				? messages.text(Lang.LOGS_SYSTEM)
				: entry.getActorName();
		Map<String, String> details = new LinkedHashMap<>(entry.getDetails());
		details.put("actor", actor);
		this.localizeDetails(entry.getType(), details);

		List<Object> placeholders = new ArrayList<>();
		details.forEach((name, value) -> {
			placeholders.add(name);
			placeholders.add(value);
		});
		String description = messages.textOrDefault(
				"logs.entries." + key,
				entry.getType().name(),
				placeholders.toArray()
		);

		List<String> lore = new ArrayList<>(Arrays.asList(description.split("\n")));
		long groupedCount = parseLong(details.get("count"));
		if (groupedCount > 1L) {
			lore.add(messages.text(Lang.LOGS_GUI_GROUPED, "count", groupedCount));
		}
		lore.add("");
		lore.add(messages.text(Lang.LOGS_GUI_ACTOR, "actor", actor));
		lore.add(messages.text(Lang.LOGS_GUI_DATE, "date", DATE_FORMAT.format(
				entry.getCreatedAt().atZone(plugin.getLogManager().getZoneId()))));

		String name = messages.textOrDefault(
				"logs.types." + key,
				entry.getType().name()
		);
		return Item.create(entry.getType().getIcon(), name, lore);
	}

	private void localizeDetails(VillageLogType type, Map<String, String> details) {
		if (type == VillageLogType.QUEST_COMPLETED) {
			String questId = details.getOrDefault("quest", "");
			details.put("quest", messages.textOrDefault("quests.tasks." + questId + ".name", questId));
		}
		if (type == VillageLogType.SETTING_CHANGED) {
			String setting = details.getOrDefault("setting", "");
			details.put("setting", messages.textOrDefault("logs.settings." + setting, setting));
		}
		if (type == VillageLogType.MEMBER_PERMISSION) {
			String permission = details.getOrDefault("permission", "");
			details.put("permission", messages.textOrDefault(
					"logs.permissions." + permission.toLowerCase().replace('_', '-'), permission));
		}

		String rawValue = details.get("value");
		if (rawValue == null) {
			return;
		}
		if ("true".equalsIgnoreCase(rawValue)) {
			details.put("value", messages.text(Lang.ON));
		} else if ("false".equalsIgnoreCase(rawValue)) {
			details.put("value", messages.text(Lang.OFF));
		} else {
			details.put("value", messages.textOrDefault("logs.values." + rawValue, rawValue));
		}
	}

	private static long parseLong(String value) {
		try {
			return value == null ? 0L : Long.parseLong(value);
		} catch (NumberFormatException ignored) {
			return 0L;
		}
	}
}
