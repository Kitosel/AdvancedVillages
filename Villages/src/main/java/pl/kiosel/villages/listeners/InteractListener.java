package pl.kiosel.villages.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.kiosel.rosacore.listener.RosaListener;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.api.events.VillageBlockInteractEvent;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.gui.GUIS;

public class InteractListener extends RosaListener {

	private final AdvancedVillages plugin;

	public InteractListener(AdvancedVillages plugin) {
		super(plugin);
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onClick(PlayerInteractEvent event) {
		Block clickedBlock = event.getClickedBlock();
		if (clickedBlock == null || !isSameType(clickedBlock.getType(), Material.NOTE_BLOCK)) return;

		Action action = event.getAction();
		if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) return;

		Player player = event.getPlayer();
		User user = plugin.getUserManager().findByUuid(player.getUniqueId()).orElseThrow();
		if (user.getPresentVillage() == null) return;

		Village village = user.getPresentVillage();
		if (village == null || !village.isMember(user)) return;

		if (village.isCentralBlock(clickedBlock.getLocation())) {
			VillageBlockInteractEvent villageBlockInteractEvent = new VillageBlockInteractEvent(village, player, clickedBlock, event.getAction());
			plugin.getServer().getPluginManager().callEvent(villageBlockInteractEvent);
			event.setCancelled(true);
			if (villageBlockInteractEvent.isCancelled()) return;

			plugin.getVillageGui().openGui(village, player, GUIS.VILLAGE);
		}
	}
}