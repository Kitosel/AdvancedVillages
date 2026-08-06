package pl.kiosel.villages.gui.village;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import pl.kiosel.villages.config.GuiConfig;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.gui.Item;

import java.util.List;
import java.util.UUID;

public class MemberRemoveInventory extends Item  {

	public Inventory getInventory(Player player, UUID member) {
		Inventory inventory = Bukkit.createInventory(player, GUIS.DELETE_MEMBER.getSize(), GUIS.DELETE_MEMBER.getName());
		OfflinePlayer memberPlayer = Bukkit.getOfflinePlayer(member);

		for(int x = 0; x < 27; x++) {
			inventory.setItem(x, blank(Item.Blank.WHITE));
		}
		inventory.setItem(12, create(Material.LIME_CONCRETE, GuiConfig.yes));
		inventory.setItem(13, createHead(memberPlayer, memberPlayer.getName(), List.of("&7Remove: &6" + memberPlayer.getName())));
		inventory.setItem(14, create(Material.RED_CONCRETE, GuiConfig.no));

		return inventory;
	}
}