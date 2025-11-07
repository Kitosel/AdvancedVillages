package pl.kiosel.villages.gui.village;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.gui.VillageMenu;
import pl.kiosel.villages.data.village.Village;

import java.util.List;

public class StoreInventory extends VillageMenu {

	private final AdvancedVillages plugin;

	public StoreInventory(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	@Override
	public Inventory getInventory(Village village, Player player) {
		Inventory inventory = Bukkit.createInventory(player, GUIS.STORE.getSize(), GUIS.STORE.getName());
		for(int x = 0; x < 9; ++x) { inventory.setItem(x, blank(Blank.WHITE)); }
		inventory.setItem(4, blank(Blank.BACK));

		if (village == null) return inventory;

		ItemStack villageHearth = plugin.getApi().createVillageHearth();
		ItemStack destroyerHearth = plugin.getApi().createDestroyerHearth();

		inventory.setItem(19, addLoreToItemStack(villageHearth, List.of("Buy for 240$")));
		inventory.setItem(21, addLoreToItemStack(destroyerHearth, List.of("Buy for 120$")));
//		inventory.setItem(23, create(Material.));
//		inventory.setItem(25, create(Material.));

		return inventory;
	}
}
