package pl.kiosel.villages.api.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import pl.kiosel.villages.village.Village;

public class VillageBlockInteractEvent extends VillageEvent implements Cancellable {

	@Getter @Setter
	private Block block;
	@Getter
	private Action action;

	private boolean cancelled;

	public VillageBlockInteractEvent(Village village, Player player, Block block, Action action) {
		super(village, player);
		this.block = block;
		this.action = action;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
}