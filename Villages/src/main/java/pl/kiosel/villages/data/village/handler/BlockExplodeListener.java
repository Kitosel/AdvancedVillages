package pl.kiosel.villages.data.village.handler;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockExplodeEvent;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.api.events.VillageListener;
import pl.kiosel.villages.data.village.Village;

import java.util.List;

public class BlockExplodeListener extends VillageListener {

	private final AdvancedVillages plugin;

	public BlockExplodeListener(AdvancedVillages plugin) {
		super(plugin);
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onExplode(BlockExplodeEvent event) {
		Location location = event.getBlock().getLocation();
		if (location.getWorld() == null || plugin.getVillageUtils().isBlacklisted(location.getWorld())) return;

		Village village = plugin.getVillageUtils().getVillageAt(location);
		if (village == null || village.isTnt()) return;

		List<Block> entity = event.blockList();
		if (!(entity instanceof Player)) return;

		event.setCancelled(true);
	}
}
