package pl.kiosel.villages.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.kiosel.villages.Wioski;
import pl.kiosel.villages.api.events.VillageBlockInteractEvent;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.village.Village;
import pl.kiosel.villages.village.VillageManager;

public class InteractListener implements Listener {

	private final Wioski plugin;

	public InteractListener(Wioski plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onClick(PlayerInteractEvent event) {
		Block clickedBlock = event.getClickedBlock();
		if (clickedBlock == null || clickedBlock.getType() != Material.NOTE_BLOCK) return;

		Action action = event.getAction();
		if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) return;

		Player player = event.getPlayer();
		Village village = VillageManager.getVillageByOfflineOwner(player.getName());
		if (village == null || !village.isMember(player)) return;

		if (village.isCentralBlock(clickedBlock.getLocation())) {
			VillageBlockInteractEvent villageBlockInteractEvent = new VillageBlockInteractEvent(village, player, clickedBlock, event.getAction());
			plugin.getServer().getPluginManager().callEvent(villageBlockInteractEvent);
			if (villageBlockInteractEvent.isCancelled()) return;

			plugin.getGui().openGui(village, player, GUIS.VILLAGE);
			event.setCancelled(true);
		}
	}
}