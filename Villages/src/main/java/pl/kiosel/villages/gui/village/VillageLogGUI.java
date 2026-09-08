package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.rosacore.utils.NumberUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.features.logs.VillageLogEntry;
import pl.kiosel.villages.data.village.features.logs.VillageLogType;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageRole;
import pl.kiosel.villages.gui.GUIS;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

import java.time.format.DateTimeFormatter;
import java.util.*;

public final class VillageLogGUI extends VillageMenu {

	private final DateTimeFormatter dateFormat;

	public VillageLogGUI(AdvancedVillages plugin, VillageGUIManager menus, Village village,
						 Player player, Gui parent) {
		super(plugin, menus, village, player, GUIS.LOGS, parent, true);
		this.dateFormat = this.readDateFormat();
		addBackButton();

		if (!plugin.getLogManager().canView(player, village)) {
			GuiItemConfig denied = item("no-permission", 13, Material.BARRIER,
					"&cYou do not have permission to view village logs.", List.of());
			if (denied.isEnabled()) setItem(denied.getSlot(), denied.createItem());
			return;
		}

		List<VillageLogEntry> entries = plugin.getLogManager().getEntries(village);
		if (entries.isEmpty()) {
			GuiItemConfig empty = item("empty", 13, Material.PAPER,
					"&7There are no recorded activities yet.", List.of());
			if (empty.isEnabled()) setItem(empty.getSlot(), empty.createItem());
			return;
		}

		int slot = plugin.getGuiSettings().integer("guis.logs.start-slot", 9, 9,
				Math.max(9, menuConfig.getSize() - 1));
		for (VillageLogEntry entry : entries) {
			ItemStack item = this.createEntryItem(entry);
			if (item != null) setItem(slot++, item);
		}
	}

	private ItemStack createEntryItem(VillageLogEntry entry) {
		String key = entry.getType().getConfigKey();
		GuiItemConfig item = item("types." + key, 9, Material.PAPER,
				entry.getType().name(), List.of());
		if (!item.isEnabled()) return null;

		String actor = entry.getActorName().isBlank()
				? guiText("entry.system", "Village system")
				: entry.getActorName();
		Map<String, String> details = new LinkedHashMap<>(entry.getDetails());
		details.put("actor", actor);
		this.localizeDetails(entry.getType(), details);

		List<Object> placeholders = new ArrayList<>();
		details.forEach((name, value) -> {
			placeholders.add(name);
			placeholders.add(value);
		});
		String description = plugin.getLogConfig().text(
				"entries." + key,
				entry.getType().name(),
				placeholders.toArray()
		);

		List<String> lore = new ArrayList<>(Arrays.asList(description.split("\n")));
		long groupedCount = NumberUtils.parseLongOrZero(details.get("count"));
		if (groupedCount > 1L) {
			lore.add(guiText("entry.grouped", "&8Merged actions: &7%count%",
					"count", groupedCount));
		}
		lore.add("");
		lore.add(guiText("entry.actor", "&7Performed by: &f%actor%", "actor", actor));
		lore.add(guiText("entry.date", "&7Date: &f%date%", "date", this.dateFormat.format(
				entry.getCreatedAt().atZone(plugin.getLogManager().getZoneId()))));

		return item.createItem(item.getName(), lore);
	}

	private void localizeDetails(VillageLogType type, Map<String, String> details) {
		if (type == VillageLogType.QUEST_COMPLETED) {
			String questId = details.getOrDefault("quest", "");
			details.put("quest", plugin.getGuiSettings().text(
					"guis.quests.tasks." + questId + ".name", questId));
		}
		if (type == VillageLogType.SETTING_CHANGED) {
			String setting = details.getOrDefault("setting", "");
			details.put("setting", plugin.getLogConfig().text("settings." + setting, setting));
		}
		if (type == VillageLogType.MEMBER_PERMISSION) {
			String permission = details.getOrDefault("permission", "");
			details.put("permission", plugin.getLogConfig().text(
					"permissions." + permission.toLowerCase().replace('_', '-'), permission));
		}
		if (type == VillageLogType.MEMBER_ROLE) {
			String role = details.getOrDefault("role", "");
			details.put("role", plugin.getRoleManager().getRole(role)
					.map(VillageRole::getName)
					.orElse(role));
		}
		if (type == VillageLogType.DEVELOPMENT_UNLOCKED) {
			String node = details.getOrDefault("node", "");
			details.put("node", plugin.getGuiSettings().text(
					"guis.development.nodes." + node + ".name", node));
		}

		String rawValue = details.get("value");
		if (rawValue == null) {
			return;
		}
		if ("true".equalsIgnoreCase(rawValue)) {
			details.put("value", plugin.getLogConfig().text("values.on", "&aON"));
		} else if ("false".equalsIgnoreCase(rawValue)) {
			details.put("value", plugin.getLogConfig().text("values.off", "&cOFF"));
		} else {
			details.put("value", plugin.getLogConfig().text("values." + rawValue, rawValue));
		}
	}

	private GuiItemConfig item(String id, int slot, Material material, String name, List<String> lore) {
		return item(GUIS.LOGS, "guis.logs." + id, slot, material, name, lore);
	}

	private String guiText(String path, String fallback, Object... placeholders) {
		String result = plugin.getGuiSettings().text("guis.logs." + path, fallback);
		for (int index = 0; index + 1 < placeholders.length; index += 2) {
			result = result.replace("%" + placeholders[index] + "%", String.valueOf(placeholders[index + 1]));
		}
		return result;
	}

	private DateTimeFormatter readDateFormat() {
		String pattern = plugin.getGuiConfig().getString("guis.logs.date-format", "dd.MM.yyyy HH:mm");
		try {
			return DateTimeFormatter.ofPattern(pattern);
		} catch (IllegalArgumentException ignored) {
			return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
		}
	}
}
