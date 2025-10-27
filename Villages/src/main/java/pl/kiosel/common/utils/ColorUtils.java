package pl.kiosel.common.utils;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class ColorUtils {

	public static String tl(String message) {
		return color(message);
	}

	public static String color(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public static List<String> listColor(List<String> lore) {
		List<String> colored = new ArrayList<>(lore);
		colored.replaceAll(ColorUtils::tl);
		return colored;
	}
}