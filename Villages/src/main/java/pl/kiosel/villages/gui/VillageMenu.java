package pl.kiosel.villages.gui;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import pl.kiosel.core.utils.TimeUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.manager.UpgradeManager;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class VillageMenu extends Item {

	public abstract Inventory getInventory(Village village, Player player);

	public List<String> replaceWith(Village village, List<String> strings) {
		return VillageManager.replaceWithList(village, strings);
	}

	public List<String> replaceWithLevelInfo(List<String> strings, int level) {
		List<String> list = new ArrayList<>();
		for (String s : strings) {
			list.add(s
					.replace("%village_level%", level+"")
					.replace("%village_next_level%", level+1+"")
					.replace("%village_cost%", UpgradeManager.getCostForLevel(level+1)+""));
		}
		return list;
	}

	public List<String> replaceEffects(List<String> strings, String price, String amplifier, String effect) {
		List<String> list = new ArrayList<>();
		for (String s : strings) {
			list.add(s
					.replace("%price%", price)
					.replace("%amplifier%", amplifier)
					.replace("%effect%", effect)
			);
		}
		return list;
	}

	public List<String> replacePlayer(List<String> strings, OfflinePlayer player) {
		String lastOnline = TimeUtils.getStringDate(player.getLastPlayed());
		String nowOnline = AdvancedVillages.getInstance().getLocale().getMessage(Lang.PLAYER_ONLINE.getPath()).toString();
		List<String> list = new ArrayList<>();
		for (String s : strings) {
			list.add(s
					.replace("%PLAYER%", Objects.requireNonNull(player.getName()))
					.replace("%PLAYER_LAST_ONLINE%", player.isOnline() ? nowOnline : lastOnline)
					.replace("%PLAYER%_UUID", player.getUniqueId().toString())
			);
		}
		return list;
	}

	public List<String> replaceCustom(List<String> strings, String target, String replacement) {
		List<String> list = new ArrayList<>();
		for (String s : strings) {
			list.add(s.replace("%" + target + "%", replacement));
		}
		return list;
	}

	protected Material getMaterialUpgrade(int level) {
		switch (level) {
			case 1:
				return Material.COAL;
			case 2:
				return Material.GOLD_INGOT;
			case 3:
				return Material.IRON_INGOT;
			case 4:
				return Material.DIAMOND;
			case 5:
				return Material.NETHERITE_INGOT;
		}
		return Material.APPLE;
	}
}