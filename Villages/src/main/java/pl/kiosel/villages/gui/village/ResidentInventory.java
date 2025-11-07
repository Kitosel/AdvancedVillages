package pl.kiosel.villages.gui.village;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.villages.config.GuiConfig;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.gui.VillageMenu;
import pl.kiosel.villages.data.village.Village;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ResidentInventory extends VillageMenu {

	@Override
	public Inventory getInventory(Village village, Player player) {
		Inventory inventory = Bukkit.createInventory(player, GUIS.RESIDENT.getSize(), GUIS.RESIDENT.getName());
		for(int x = 0; x < 9; ++x) { inventory.setItem(x, blank(Item.Blank.WHITE)); }
		inventory.setItem(4, blank(Item.Blank.BACK));

		if (village == null) return inventory;

		List<ItemStack> items = new ArrayList<>();
		for (UUID member : village.getMembers()) {
			OfflinePlayer playerMember = Bukkit.getOfflinePlayer(member);
			if (village.isOwner(member)) {
				items.add(createHead(
						playerMember, GuiConfig.guis_village_members_owner.replace("%PLAYER%", Objects.requireNonNull(playerMember.getName())), replacePlayer(
								GuiConfig.guis_village_members_owner_lore, playerMember))
				);
			} else {
				items.add(createHead(
						playerMember, GuiConfig.guis_village_members_member.replace("%PLAYER%", Objects.requireNonNull(playerMember.getName())), replacePlayer(
								GuiConfig.guis_village_members_member_lore, playerMember))
				);
			}
		}
		int firstSlot = 19;
		if (items.size() > 8)
			firstSlot = 9;

		for (int slot = 0; slot < items.size(); slot++) {
			inventory.setItem(firstSlot+slot, items.get(slot));
		}
		return inventory;
	}
}
