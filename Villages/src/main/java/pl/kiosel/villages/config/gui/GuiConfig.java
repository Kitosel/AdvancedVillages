package pl.kiosel.villages.config.gui;

import org.bukkit.Material;
import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.rosacore.utils.ColorUtils;
import pl.kiosel.rosacore.utils.NumberUtils;
import pl.kiosel.villages.AdvancedVillages;

import java.util.*;

public final class GuiConfig {

	private final AdvancedVillages plugin;
	private final RosaConfig commonFile;
	private final Set<String> reportedProblems = new HashSet<>();
	private volatile Map<GUIS, GuiMenuConfig> menus = Collections.emptyMap();

	public GuiConfig(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.commonFile = plugin.getGuiCommonConfig();
		this.reload();
	}

	public void reload() {
		EnumMap<GUIS, GuiMenuConfig> loaded = new EnumMap<>(GUIS.class);
		for (GUIS type : GUIS.values()) {
			String layoutPath = "layout." + type.getId();

			int rows = NumberUtils.clamp(this.commonFile.getInt(layoutPath + ".rows", type.getDefaultRows()), 1, 6);
			int size = rows * 9;
			int defaultBack = Math.min(type.getDefaultBackSlot(), size - 1);

			int backSlot = this.commonFile.getInt(layoutPath + ".back-slot", defaultBack);
			if (backSlot < 0 || backSlot >= size) {
				this.warnOnce(layoutPath + ".back-slot", "Invalid back slot " + backSlot + " for " + type.getId() + "; using " + defaultBack);
				backSlot = defaultBack;
			}
			int requiredLevel = 0;
			if (this.commonFile.isSet(layoutPath + ".required-level") && requireVillage(type)) {
				requiredLevel = this.commonFile.getInt(layoutPath + ".required-level");
			}

			String title = this.title(type.getTitlePath(), type.getDefaultTitle());
			plugin.getDebug().debug("Registered gui: " + type.getId());
			loaded.put(type, new GuiMenuConfig(type.getId(), title, rows, backSlot, requiredLevel));
		}
		this.menus = Collections.unmodifiableMap(loaded);
	}

	public GuiMenuConfig menu(GUIS type) {
		GuiMenuConfig menu = this.menus.get(type);
		if (menu != null) {
			return menu;
		}
		return new GuiMenuConfig(type.getId(), ColorUtils.color(type.getDefaultTitle()), type.getDefaultRows(), 4, type.getDefaultRequiredLevel());
	}

	public GuiItemConfig item(GUIS menu, String path, int defaultSlot, Material defaultMaterial,
	                          String defaultName, List<String> defaultLore) {
		return this.item(menu, path, defaultSlot, defaultMaterial, 1, defaultName, defaultLore, false);
	}

	public GuiItemConfig item(GUIS menu, String path, int defaultSlot, Material defaultMaterial,
	                          int defaultAmount, String defaultName, List<String> defaultLore,
	                          boolean defaultGlow) {
		RosaConfig file = getGuiConfig(menu);

		GuiMenuConfig menuConfig = this.menu(menu);
		int configuredSlot = file.getInt(path + ".slot", defaultSlot);
		int slot = menuConfig.validSlot(configuredSlot, defaultSlot);
		if (slot != configuredSlot) {
			this.warnOnce(path + ".slot",
					"Invalid slot " + configuredSlot + " for " + menu.getId() + "; using " + slot);
		}

		Material material = this.material(menu, path + ".material", defaultMaterial);
		int amount = NumberUtils.clamp(file.getInt(path + ".amount", defaultAmount), 1,
				Math.max(1, material.getMaxStackSize()));
		String name = this.text(menu, path + ".name", defaultName);
		List<String> lore = this.list(menu, path + ".lore", defaultLore);

		boolean glow = file.getBoolean(path + ".glow", defaultGlow);
		boolean enabled = file.getBoolean(path + ".enabled", true);

		return new GuiItemConfig(path, slot, material, amount, name, lore, glow, enabled);
	}

	public String blank(String path, String fallback) {
		String value = this.commonFile.getString(path);
		String text;
		if (value == null) {
			text = fallback;
			plugin.getDebug().debug("blank config: " + path);
		} else {
			text = value;
		}
		return ColorUtils.color(text);
	}

	//String text = value == null ? fallback : value;
	//return ColorUtils.color(text);

	public String text(GUIS menu, String path, String fallback) {
		String value = getGuiConfig(menu).getString(path);
		String text;
		if (value == null) {
			text = fallback;
			plugin.getDebug().debug("text config is null: " + path);
		} else {
			text = value;
		}
		return ColorUtils.color(text);
	}

	public String title(String path, String fallback) {
		String value = this.commonFile.getString(path);
		String text;
		if (value == null) {
			text = fallback;
			plugin.getDebug().debug("title is null: " + path);
		} else {
			text = value;
		}
		return ColorUtils.color(text);
	}

	public List<String> list(GUIS menu, String path, List<String> fallback) {
		List<String> configured = getGuiConfig(menu).getStringList(path);
		//List<String> source = getGuiConfig(menu).contains(path) ? configured : fallback;
		List<String> source;
		if (getGuiConfig(menu).contains(path)) {
			source = configured;
		} else {
			source = fallback;
			plugin.getDebug().debug("list config is blank: " + path);
		}

		if (source == null || source.isEmpty()) {
			return Collections.emptyList();
		}

		List<String> colored = new ArrayList<>(source.size());
		for (String line : source) {
			colored.add(ColorUtils.color(line == null ? "" : line));
		}
		return Collections.unmodifiableList(colored);
	}

	public int integer(GUIS menu, String path, int fallback, int minimum, int maximum) {
		return NumberUtils.clamp(getGuiConfig(menu).getInt(path, fallback), minimum, maximum);
	}

	private Material material(GUIS menu, String path, Material fallback) {
		String raw = getGuiConfig(menu).getString(path);
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
			this.plugin.getRosaLogger().warning(message);
		}
	}

	public RosaConfig getGuiConfig(GUIS type) {
		switch (type) {
			//case OUTPOST:
			//case OUTPOST_NAME:
			//case OUTPOST_SETTINGS:
			//case OUTPOST_UPGRADE:
			//	return plugin.getGuiOutpostConfig();
			case TUTORIAL:
				return plugin.getGuiTutorialConfig();
			default:
				return plugin.getGuiVillageConfig();
		}
	}

	public boolean requireVillage(GUIS type) {
		switch (type) {
			case BANK:
			case STORE:
			case RESIDENT:
			case EFFECTS:
			case LOGS:
			case DIPLOMACY:
			case QUESTS:
			case SPECIALIZATIONS:
			case DEVELOPMENT:
				return true;
			default:
				return false;
		}
	}
}
