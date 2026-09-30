package pl.kiosel.villages.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.kiosel.villages.data.outpost.Outpost;

public abstract class OutpostEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();

	@Nullable
    private final Outpost outpost;

    public OutpostEvent(@Nullable Outpost outpost, Player player) {
	    super(player);
		this.outpost = outpost;
    }

    public @Nullable Outpost getOutpost() {
        return outpost;
    }

	public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
