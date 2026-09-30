package pl.kiosel.villages.data.outpost.handler;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import pl.kiosel.rosacore.material.ItemTag;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.api.events.VillageListener;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.outpost.Outpost;
import pl.kiosel.villages.data.outpost.OutpostManager;
import pl.kiosel.villages.data.user.User;

public final class OutpostProtectionListener extends VillageListener {

	private final OutpostManager manager;

	public OutpostProtectionListener(AdvancedVillages plugin) {
		super(plugin);
		this.manager = plugin.getOutpostManager();
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBreak(BlockBreakEvent event) {
		Outpost structure = this.manager.findStructure(event.getBlock().getLocation()).orElse(null);
		if (structure != null) {
			event.setCancelled(true);
			sendLocalized(event.getPlayer(), this.manager.findCore(event.getBlock().getLocation()).isPresent()
					? Lang.OUTPOST_CORE_PROTECTED : Lang.OUTPOST_REGION_PROTECTED);
			return;
		}
		protectOutsider(event.getPlayer(), event.getBlock(), event);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlace(BlockPlaceEvent event) {
		if (ItemTag.has(event.getItemInHand(), "outpostBlock")) return;
		Outpost structure = this.manager.findStructure(event.getBlock().getLocation()).orElse(null);
		if (structure != null) {
			event.setCancelled(true);
			sendLocalized(event.getPlayer(), Lang.OUTPOST_REGION_PROTECTED);
			return;
		}
		protectOutsider(event.getPlayer(), event.getBlock(), event);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		event.blockList().removeIf(block -> this.manager.findAt(block.getLocation()).isPresent());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockExplode(BlockExplodeEvent event) {
		event.blockList().removeIf(block -> this.manager.findAt(block.getLocation()).isPresent());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBurn(BlockBurnEvent event) {
		if (this.manager.findAt(event.getBlock().getLocation()).isPresent()) event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onIgnite(BlockIgniteEvent event) {
		if (this.manager.findAt(event.getBlock().getLocation()).isPresent()) event.setCancelled(true);
	}

	private void protectOutsider(Player player, Block block, org.bukkit.event.Cancellable event) {
		Outpost outpost = this.manager.findAt(block.getLocation()).orElse(null);
		if (outpost == null) return;
		User user = getUser(player);
		if (user != null && outpost.getVillage().isMember(user)) return;
		event.setCancelled(true);
		sendLocalized(player, Lang.OUTPOST_REGION_PROTECTED);
	}
}
