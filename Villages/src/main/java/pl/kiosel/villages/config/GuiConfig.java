package pl.kiosel.villages.config;

import org.bukkit.Material;
import pl.kiosel.core.configuration.Config;
import pl.kiosel.core.utils.NumberUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.GUIS;

import java.util.*;

import static pl.kiosel.core.utils.ColorUtils.tl;

/**
 * Typed, reloadable access to guis.yml. Existing name/lore paths remain valid;
 * layout and item metadata can now be changed without touching Java code.
 */
public final class GuiConfig {

	private final AdvancedVillages plugin;
	private final Config file;
	private final Set<String> reportedProblems = new HashSet<>();
	private volatile Map<GUIS, GuiMenuConfig> menus = Collections.emptyMap();

	public GuiConfig(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.file = plugin.getGuiConfig();
		this.reload();
	}

	public synchronized void reload() {
		EnumMap<GUIS, GuiMenuConfig> loaded = new EnumMap<>(GUIS.class);
		for (GUIS type : GUIS.values()) {
			String layoutPath = "layout." + type.getId();
			int rows = NumberUtils.clamp(this.file.getInt(layoutPath + ".rows", type.getDefaultRows()), 1, 6);
			int size = rows * 9;
			int defaultBack = Math.min(type.getDefaultBackSlot(), size - 1);
			int backSlot = this.file.getInt(layoutPath + ".back-slot", defaultBack);
			if (backSlot < 0 || backSlot >= size) {
				this.warnOnce(layoutPath + ".back-slot",
						"Invalid back slot " + backSlot + " for " + type.getId() + "; using " + defaultBack);
				backSlot = defaultBack;
			}

			String title = this.text(type.getTitlePath(), type.getDefaultTitle());
			loaded.put(type, new GuiMenuConfig(type.getId(), title, rows, backSlot));
		}
		this.menus = Collections.unmodifiableMap(loaded);
	}

	public GuiMenuConfig menu(GUIS type) {
		GuiMenuConfig menu = this.menus.get(type);
		if (menu != null) {
			return menu;
		}
		return new GuiMenuConfig(type.getId(), tl(type.getDefaultTitle()), type.getDefaultRows(), 4);
	}

	public GuiItemConfig item(GUIS menu, String path, int defaultSlot, Material defaultMaterial,
	                          String defaultName, List<String> defaultLore) {
		return this.item(menu, path, defaultSlot, defaultMaterial, 1, defaultName, defaultLore, false);
	}

	public GuiItemConfig item(GUIS menu, String path, int defaultSlot, Material defaultMaterial,
	                          int defaultAmount, String defaultName, List<String> defaultLore,
	                          boolean defaultGlow) {
		GuiMenuConfig menuConfig = this.menu(menu);
		int configuredSlot = this.file.getInt(path + ".slot", defaultSlot);
		int slot = menuConfig.validSlot(configuredSlot, defaultSlot);
		if (slot != configuredSlot) {
			this.warnOnce(path + ".slot",
					"Invalid slot " + configuredSlot + " for " + menu.getId() + "; using " + slot);
		}

		Material material = this.material(path + ".material", defaultMaterial);
		int amount = NumberUtils.clamp(this.file.getInt(path + ".amount", defaultAmount), 1,
				Math.max(1, material.getMaxStackSize()));
		String name = this.text(path + ".name", defaultName);
		List<String> lore = this.list(path + ".lore", defaultLore);
		boolean glow = this.file.getBoolean(path + ".glow", defaultGlow);
		boolean enabled = this.file.getBoolean(path + ".enabled", true);
		return new GuiItemConfig(path, slot, material, amount, name, lore, glow, enabled);
	}

	public String text(String path, String fallback) {
		String value = this.file.getString(path);
		return tl(value == null ? fallback : value);
	}

	public List<String> list(String path, List<String> fallback) {
		List<String> configured = this.file.getStringList(path);
		List<String> source = this.file.contains(path) ? configured : fallback;
		if (source == null || source.isEmpty()) {
			return Collections.emptyList();
		}
		List<String> colored = new ArrayList<>(source.size());
		for (String line : source) {
			colored.add(tl(line == null ? "" : line));
		}
		return Collections.unmodifiableList(colored);
	}

	public int integer(String path, int fallback, int minimum, int maximum) {
		return NumberUtils.clamp(this.file.getInt(path, fallback), minimum, maximum);
	}

	private Material material(String path, Material fallback) {
		String raw = this.file.getString(path);
		if (raw == null || raw.trim().isEmpty()) {
			return fallback;
		}
		Material material = Material.matchMaterial(raw.trim().toUpperCase(Locale.ROOT));
		if (material == null || material.isAir()) {
			this.warnOnce(path, "Invalid GUI material '" + raw + "' at " + path
					+ "; using " + fallback.name());
			return fallback;
		}
		return material;
	}

	private void warnOnce(String key, String message) {
		if (this.reportedProblems.add(key)) {
			this.plugin.getLogger().warning(message);
		}
	}
}
