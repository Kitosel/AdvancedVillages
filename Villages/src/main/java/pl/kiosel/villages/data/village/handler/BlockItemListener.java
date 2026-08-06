package pl.kiosel.villages.data.village.handler;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.core.dependencies.de.tr7zw.nbtapi.NBT;
import pl.kiosel.core.dependencies.de.tr7zw.nbtapi.iface.ReadableItemNBT;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.data.village.Village;

import java.util.function.Function;

public class BlockItemListener implements Listener {

	private final AdvancedVillages plugin;

	public BlockItemListener(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlace(BlockPlaceEvent event) {
		ItemStack item = event.getItemInHand();
		if (item == null || item.getType() == Material.AIR) return;

		Boolean tag = NBT.get(item, (Function<ReadableItemNBT, Boolean>) nbt -> nbt.getBoolean("noPlace"));
		if (tag)
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerCraft(CraftItemEvent event) {
		if (event.getRecipe() == null) return;

		if (plugin.getCraftingManager().isCustomRecipe(event.getRecipe()))
			return;

		for (ItemStack item : event.getInventory().getMatrix()) {
			if (item == null || item.getType() == Material.AIR) continue;

			Boolean tag = NBT.get(item, (Function<ReadableItemNBT, Boolean>) (nbt) -> nbt.getBoolean("noPlace"));
			String displayName = (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
					? item.getItemMeta().getDisplayName()
					: "";

			if (tag || (displayName.contains(plugin.getLocale().getMessage(Lang.VILLAGE_HEARTH_BLOCK_NAME.getPath()).toString()))
					|| (displayName.contains(plugin.getLocale().getMessage(Lang.VILLAGE_DESTROYER_NAME.getPath()).toString()))) {
				event.setCancelled(true);

				if (event.getWhoClicked() instanceof Player player) {
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.2f, 1f);
				}
				return;
			}
		}
	}
}